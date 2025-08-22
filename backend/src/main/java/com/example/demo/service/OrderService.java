package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime; // BigDecimal utils
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import com.example.demo.event.OrderCreatedEvent;
import com.example.demo.event.OrderUpdatedEvent;

import com.example.demo.dto.request.OrderItemRequestDTO;
import com.example.demo.dto.request.OrderRequestDTO;
import com.example.demo.dto.response.OrderItemResponseDTO;
import com.example.demo.dto.response.OrderResponseDTO;
import com.example.demo.enums.StockMovementEnum; // <-- DEĞİŞİKLİK: TX yönetimi
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.diningtable.TableNotFoundException;
import com.example.demo.exception.order.EmptyOrderException;
import com.example.demo.exception.order.InsufficientStockException;
import com.example.demo.exception.order.InvalidQuantityException;
import com.example.demo.exception.order.OrderNotFoundException;
import com.example.demo.exception.order.OrderProcessingException;
import com.example.demo.exception.order.TableNotAvailableException;
import com.example.demo.exception.user.UserNotFoundException;
import com.example.demo.model.DiningTable;
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.Product;
import com.example.demo.model.Stock;
import com.example.demo.model.StockMovement;
import com.example.demo.model.User;
import com.example.demo.repository.DiningTableRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductIngredientRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.StockMovementRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.BDH;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final DiningTableRepository tableRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final ActivityLogService activityLogService;
    private final OrderItemRepository orderItemRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        DiningTableRepository tableRepository,
                        StockMovementRepository stockMovementRepository,
                        ProductIngredientRepository productIngredientRepository,
                        ActivityLogService activityLogService,
                        OrderItemRepository orderItemRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.tableRepository = tableRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productIngredientRepository = productIngredientRepository;
        this.activityLogService = activityLogService;
        this.orderItemRepository = orderItemRepository;
        this.eventPublisher = eventPublisher;
    }

    // ------------------------------------------------------------------------------------
    // CRUD (idari amaçlar; garson akışında esas /upsert-sync kullanılacak)
    // ------------------------------------------------------------------------------------

    @Transactional // <-- DEĞİŞİKLİK: veri bütünlüğü için
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {
        validateOrderRequest(dto);

        User user = userRepository.findById((long) dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: ID = " + dto.getUserId()));
        DiningTable table = tableRepository.findById((long) dto.getTableId())
                .orElseThrow(() -> new TableNotFoundException("Masa bulunamadı: ID = " + dto.getTableId()));

        validateTableAvailability(table);

        Order order = new Order();
        order.setUser(user);
        order.setTable(table);
        order.setCreatedAt(LocalDateTime.now());
        order.setCompleted(false);

        List<OrderItem> items = dto.getItems().stream().map(itemDTO -> {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı: ID = " + itemDTO.getProductId()));
            validateQuantity(itemDTO.getQuantity());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(product.getPrice()); // snapshot
            item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()))); // entity @PrePersist zaten hesaplıyor; güvenlik için yine setliyoruz
            return item;
        }).collect(Collectors.toList());

        order.setItems(items);

        BigDecimal totalPrice = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(totalPrice);

        try {
            Order saved = orderRepository.save(order);

            ObjectNode details = activityLogService.createDetailsNode(
                    "Order created by user " + user.getName() + " for table " + table.getTableNumber(),
                    "userId", user.getId().toString(),
                    "tableId", table.getId().toString(),
                    "totalPrice", totalPrice.toString(),
                    "itemCount", String.valueOf(items.size())
            );
            activityLogService.logActivity(user.getId(), "CREATE", "ORDER", saved.getId(), details);

            // 🔥 REAL-TIME ANALYTICS: Publish event for immediate summary updates
            eventPublisher.publishEvent(new OrderCreatedEvent(this, saved));
            log.info("OrderCreatedEvent published for order ID: {}", saved.getId());

            return buildOrderResponseDTO(saved);
        } catch (Exception e) {
            throw new OrderProcessingException("Sipariş oluşturulurken hata oluştu: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::buildOrderResponseDTO)
                .collect(Collectors.toList());
    }

    // <-- YENİ: Kasiyer için filtreli liste (isCompleted & tableId)
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersFiltered(Boolean isCompleted, Long tableId) {
        // Not: Repository'de özel metot olmadığı için burada filtreliyoruz (performans gerekirse repo'ya metot eklenir)
        List<Order> base = (tableId != null)
                ? orderRepository.findByTableId(tableId)
                : orderRepository.findAll();

        List<Order> filtered = (isCompleted == null)
                ? base
                : base.stream().filter(o -> o.isCompleted() == isCompleted).collect(Collectors.toList());

        // İsteğe bağlı: tarihine göre sıralama (en yeni üstte)
        filtered.sort(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        return filtered.stream().map(this::buildOrderResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Sipariş bulunamadı: ID = " + id));
        return buildOrderResponseDTO(order);
    }

    @Transactional // <-- TX
    public OrderResponseDTO updateOrder(Long id, OrderRequestDTO dto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Sipariş bulunamadı: ID = " + id));

        validateOrderRequest(dto);

        User user = userRepository.findById((long) dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: ID = " + dto.getUserId()));
        DiningTable table = tableRepository.findById((long) dto.getTableId())
                .orElseThrow(() -> new TableNotFoundException("Masa bulunamadı: ID = " + dto.getTableId()));

        if (!order.getTable().getId().equals(table.getId())) {
            validateTableAvailability(table);
        }

        order.setUser(user);
        order.setTable(table);
        order.setUpdatedAt(LocalDateTime.now()); // Use updatedAt instead of createdAt

        // 🔥 CRITICAL FIX: Clear existing items first (mutate in place; don't replace collection)
        order.getItems().clear();
        
        // Create new items
        List<OrderItem> items = dto.getItems().stream().map(itemDTO -> {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı: ID = " + itemDTO.getProductId()));
            validateQuantity(itemDTO.getQuantity());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            return item;
        }).collect(Collectors.toList());

        // Mutate managed collection to avoid orphanRemoval exception
        order.getItems().addAll(items);

        BigDecimal totalPrice = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(totalPrice);

        try {
            Order updated = orderRepository.save(order);

            ObjectNode details = activityLogService.createDetailsNode(
                    "Order updated by user " + user.getName() + " for table " + table.getTableNumber(),
                    "userId", user.getId().toString(),
                    "tableId", table.getId().toString(),
                    "totalPrice", totalPrice.toString(),
                    "itemCount", String.valueOf(items.size())
            );
            activityLogService.logActivity(user.getId(), "UPDATE", "ORDER", updated.getId(), details);

            // 🔥 REAL-TIME ANALYTICS: Publish event for immediate summary updates
            eventPublisher.publishEvent(new OrderUpdatedEvent(this, updated));
            log.info("OrderUpdatedEvent published for order ID: {}", updated.getId());

            return buildOrderResponseDTO(updated);
        } catch (Exception e) {
            throw new OrderProcessingException("Sipariş güncellenirken hata oluştu: " + e.getMessage(), e);
        }
    }

    @Transactional // <-- TX
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Sipariş bulunamadı: ID = " + id);
        }

        Order order = orderRepository.findById(id).orElse(null);
        orderRepository.deleteById(id);

        if (order != null) {
            ObjectNode details = activityLogService.createDetailsNode(
                    "Order deleted for table " + order.getTable().getTableNumber(),
                    "tableId", order.getTable().getId().toString(),
                    "totalPrice", order.getTotalPrice().toString()
            );
            Long actorUserId = (order.getUser() != null) ? order.getUser().getId() : null;
            activityLogService.logActivity(actorUserId, "DELETE", "ORDER", id, details);
        }
    }

    // ------------------------------------------------------------------------------------
    // İş akışı: stok düşümü vs. (bu uç UI'da değil, backend mantığında kullanılmalı)
    // ------------------------------------------------------------------------------------

    @Transactional // <-- TX
    public void processOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Sipariş bulunamadı: ID = " + id));

        // Sipariş zaten tamamlandıysa tekrar işleme alma
        if (order.isCompleted()) {
            throw new OrderProcessingException("Sipariş zaten tamamlanmış: ID = " + id);
        }

        try {
            order.getItems().forEach(item -> {
                Product product = item.getProduct();

                productIngredientRepository.findByProductId(product.getId())
                        .forEach(productIngredient -> {
                            Stock stock = productIngredient.getIngredient();

                            BigDecimal ingredientQuantity = productIngredient.getQuantityPerUnit();
                            BigDecimal requiredQuantity = BDH.mult(ingredientQuantity,item.getQuantity());

                            if (BDH.lessThan(stock.getQuantity(),requiredQuantity)) {
                                throw new InsufficientStockException(
                                        "Yetersiz stok: " + stock.getName() +
                                                ". Gerekli: " + requiredQuantity +
                                                ", Mevcut: " + stock.getQuantity()
                                );
                            }

                            stock.setQuantity(BDH.subtract(stock.getQuantity(),requiredQuantity));

                            StockMovement stockMovement = new StockMovement();
                            stockMovement.setStock(stock);
                            stockMovement.setChange(BDH.mult(ingredientQuantity,-item.getQuantity()));
                            stockMovement.setReason(String.valueOf(StockMovementEnum.ORDER));
                            stockMovement.setTimestamp(LocalDateTime.now());
                            stockMovement.setNote("Sipariş ID: " + order.getId() + ", Ürün: " + product.getName());
                            stockMovementRepository.save(stockMovement);
                        });
            });

            order.setCompleted(true);
            order.setUpdatedAt(LocalDateTime.now());

            orderRepository.save(order);

            ObjectNode details = activityLogService.createDetailsNode(
                    "Order processed successfully for table " + order.getTable().getTableNumber(),
                    "tableId", order.getTable().getId().toString(),
                    "orderId", order.getId().toString(),
                    "itemCount", String.valueOf(order.getItems().size())
            );
            Long actorUserId = (order.getUser() != null) ? order.getUser().getId() : null;
            activityLogService.logActivity(actorUserId, "PROCESS", "ORDER", order.getId(), details);

        } catch (InsufficientStockException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderProcessingException("Sipariş işlenirken hata oluştu: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------------------------
    // Garson/Kasiyer akışı için yardımcı metodlar
    // ------------------------------------------------------------------------------------

    // Garson masaya tıklayınca açık siparişi getir (yoksa boş döner)
    @Transactional(readOnly = true)
    public Optional<OrderResponseDTO> getOpenOrderByTable(Long tableId) {
        return orderRepository.findByTableId(tableId).stream()
                .filter(o -> !o.isCompleted()) // isCompleted=false
                .max(Comparator.comparing(Order::getCreatedAt))
                .map(this::buildOrderResponseDTO);
    }

    // Tam senkron upsert: aynı endpoint ile oluştur/güncelle
    @Transactional // <-- DEĞİŞİKLİK: TX ve atomiklik
    public OrderResponseDTO upsertOrderSync(OrderRequestDTO dto, Authentication auth) {
        // 1) Masa
        Long tableId = (long) dto.getTableId();
        DiningTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new TableNotFoundException("Masa bulunamadı: ID = " + dto.getTableId()));

        // 2) Garsonu Authentication'dan çöz (request DTO'daki userId'yi YOK SAYIYORUZ)
        User waiter = resolveUserFromAuth(auth);

        // 3) Açık sipariş var mı? (isCompleted=false)
        Order order = orderRepository.findByTableId(table.getId()).stream()
                .filter(o -> !o.isCompleted())
                .max(Comparator.comparing(Order::getCreatedAt))
                .orElseGet(() -> {
                    Order o = new Order();
                    o.setUser(waiter);
                    o.setTable(table);
                    o.setCreatedAt(LocalDateTime.now());
                    o.setCompleted(false);
                    o.setTotalPrice(BigDecimal.ZERO);
                    return o;
                });

        if (order.isCompleted()) {
            throw new OrderProcessingException("Tamamlanmış sipariş güncellenemez (orderId=" + order.getId() + ")");
        }

        // 4) Mevcut kalemler productId -> item
        List<OrderItem> currentItems = (order.getItems() != null) ? order.getItems() : new java.util.ArrayList<>();
        var currentByProductId = currentItems.stream()
                .filter(oi -> oi.getProduct() != null)
                .collect(Collectors.toMap(oi -> oi.getProduct().getId(), Function.identity(), (a,b)->a));

        // 5) Gelen PID seti (tam senkron için gerekli)
        var incomingProductIds = dto.getItems().stream()
                .map(OrderItemRequestDTO::getProductId)
                .map(Long::valueOf)
                .collect(Collectors.toSet());

        // 6) Yeni state'yi kur (qty==0 gelenler listeye alınmaz → sil anlamına gelir)
        java.util.List<OrderItem> newState = new java.util.ArrayList<>();

        for (OrderItemRequestDTO itemDTO : dto.getItems()) {
            long pid = itemDTO.getProductId();
            int qty = Math.max(0, itemDTO.getQuantity()); // 0 → sil

            if (qty == 0) {
                continue; // tam senkron: listede tutma
            }

            OrderItem item = currentByProductId.get(pid);
            if (item == null) {
                Product product = productRepository.findById(pid)
                        .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı: ID = " + pid));
                if (product.getPrice() == null) {
                    throw new OrderProcessingException("Ürün fiyatı tanımsız: ID = " + pid);
                }
                item = new OrderItem();
                item.setOrder(order);
                item.setProduct(product);
                item.setUnitPrice(product.getPrice()); // snapshot
            }

            item.setQuantity(qty);
            item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(qty))); // entity lifecycle da hesaplıyor
            newState.add(item);
        }

        // 7) Listede olmayan mevcut kalemleri sil (tam senkron)
        var toDelete = currentItems.stream()
                .filter(oi -> oi.getProduct() != null && !incomingProductIds.contains(oi.getProduct().getId()))
                .collect(Collectors.toList());

        // orphanRemoval=true ise setItems(newState) kullanmak yerine koleksiyonu yerinde güncelle
        toDelete.forEach(oi -> oi.setOrder(null));
        if (!toDelete.isEmpty()) {
            orderItemRepository.deleteAll(toDelete);
        }

        // Mutate managed collection in-place to avoid replacing Hibernate's collection
        order.getItems().clear();
        order.getItems().addAll(newState);

        // 8) Toplamı yeniden hesapla
        BigDecimal total = newState.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(total);

        // 9) Kaydet & log
        Order saved = orderRepository.save(order);

        try {
            ObjectNode details = activityLogService.createDetailsNode(
                    "Order upsert-sync by " + waiter.getName() + " for table " + table.getTableNumber(),
                    "userId", waiter.getId().toString(),
                    "tableId", table.getId().toString(),
                    "totalPrice", total.toString(),
                    "itemCount", String.valueOf(newState.size())
            );
            activityLogService.logActivity(waiter.getId(), "UPSERT_SYNC", "ORDER", saved.getId(), details);

            // 🔥 REAL-TIME ANALYTICS: Publish event for upsert operations
            if (order.getId() == null) {
                // New order created
                eventPublisher.publishEvent(new OrderCreatedEvent(this, saved));
                log.info("OrderCreatedEvent published for upsert order ID: {}", saved.getId());
            } else {
                // Existing order updated
                eventPublisher.publishEvent(new OrderUpdatedEvent(this, saved));
                log.info("OrderUpdatedEvent published for upsert order ID: {}", saved.getId());
            }

        } catch (Exception ignore) {}

        return buildOrderResponseDTO(saved);
    }

    // ------------------------------------------------------------------------------------
    // Yardımcılar
    // ------------------------------------------------------------------------------------

    private User resolveUserFromAuth(Authentication auth) {
        if (auth == null) {
            throw new UserNotFoundException("Kimlik doğrulama yok");
        }
        String email = auth.getName(); // genelde username=email
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: email=" + email));
    }

    private OrderResponseDTO buildOrderResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setWaiterName(order.getUser().getName());
        dto.setTableId(order.getTable().getId());
        dto.setTableNumber(order.getTable().getTableNumber());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setCompleted(order.isCompleted());
        
        // Debug logging
        log.info("Building DTO for order {}: isCompleted={}, DTO completed={}", 
                order.getId(), order.isCompleted(), dto.isCompleted());

        List<OrderItemResponseDTO> itemResponses = (order.getItems() == null ? List.<OrderItem>of() : order.getItems())
                .stream()
                .map(item -> {
                    OrderItemResponseDTO res = new OrderItemResponseDTO();
                    res.setProductId(item.getProduct().getId());
                    res.setProductName(item.getProduct().getName());
                    res.setQuantity(item.getQuantity());
                    res.setUnitPrice(item.getUnitPrice());
                    res.setTotalPrice(item.getTotalPrice());
                    res.setNote(item.getNote());
                    return res;
                })
                .collect(Collectors.toList());

        dto.setItems(itemResponses);
        return dto;
    }

    // ---- Validasyonlar (create/update uçları için; upsert-sync bunları bilerek KULLANMIYOR) ----

    private void validateOrderRequest(OrderRequestDTO dto) {
        if (dto == null) {
            throw new OrderProcessingException("Sipariş bilgisi boş olamaz");
        }
        if (dto.getUserId() <= 0) {
            throw new UserNotFoundException("Geçerli bir kullanıcı ID'si belirtilmelidir");
        }
        if (dto.getTableId() <= 0) {
            throw new TableNotFoundException("Geçerli bir masa ID'si belirtilmelidir");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new EmptyOrderException("Sipariş en az bir ürün içermelidir");
        }
        for (OrderItemRequestDTO item : dto.getItems()) {
            if (item.getProductId() <= 0) {
                throw new ResourceNotFoundException("Geçerli bir ürün ID'si belirtilmelidir");
            }
            if (item.getQuantity() <= 0) { // upsert-sync'te 0 silme anlamına geldiği için burada sadece create/update için kontrol ediyoruz.
                throw new InvalidQuantityException("Ürün miktarı sıfırdan büyük olmalıdır");
            }
            if (item.getQuantity() > 100) {
                throw new InvalidQuantityException("Bir üründen en fazla 100 adet sipariş edilebilir");
            }
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidQuantityException("Ürün miktarı sıfırdan büyük olmalıdır");
        }
    }

    private void validateTableAvailability(DiningTable table) {
        if (table == null) {
            throw new TableNotFoundException("Masa bulunamadı");
        }
        if (table.getStatus() != null && "OCCUPIED".equals(table.getStatus().toString())) {
            throw new TableNotAvailableException("Masa şu anda dolu: " + table.getId());
        }
        if (table.getCapacity() != null && table.getCapacity() <= 0) {
            throw new TableNotAvailableException("Masa kapasitesi geçersiz: " + table.getId());
        }
    }

    // Raporlama
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::buildOrderResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyOrders(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: email = " + email));
        return getOrdersByUserId(user.getId());
    }

    /**
     * Aktif sipariş bulunan masa ID'lerini döner
     */
    @Transactional(readOnly = true)
    public List<Long> getActiveTableIds() {
        log.info("Aktif sipariş bulunan masa ID'leri getiriliyor");
        List<Order> activeOrders = orderRepository.findByIsCompleted(false);
        
        return activeOrders.stream()
                .filter(order -> order.getTable() != null)
                .map(order -> order.getTable().getId())
                .distinct()
                .collect(Collectors.toList());
    }
}
