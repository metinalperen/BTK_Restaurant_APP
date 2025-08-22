package com.example.demo.service;

import com.example.demo.dto.request.ProductRequestDto;
import com.example.demo.dto.response.ProductResponseDto;
import com.example.demo.dto.response.ProductAvailableQuantityDTO;
import com.example.demo.enums.ItemCategory;
import com.example.demo.exception.product.InvalidProductDataException;
import com.example.demo.exception.product.ProductAlreadyExistsException;
import com.example.demo.exception.product.ProductNotFoundException;
import com.example.demo.exception.product.ProductReferencedInOrder;
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.Product;
import com.example.demo.model.ProductIngredient;
import com.example.demo.repository.*;
import com.example.demo.service.ActivityLogService;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;
    private final ProductIngredientRepository productIngredientRepository;

    // Constructor injection - her iki repository burada enjekte edilir
    public ProductService(ProductRepository productRepository,
                          OrderItemRepository orderItemRepository,
                          ActivityLogService activityLogService,
                          UserRepository userRepository,
                          ProductIngredientRepository productIngredientRepository,
                          OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.activityLogService = activityLogService;
        this.userRepository = userRepository;
        this.productIngredientRepository = productIngredientRepository;
    }

    private ProductResponseDto convertToResponseDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setIsActive(product.getIsActive());
        dto.setCategory(product.getCategory() != null ? product.getCategory().name() : null);
        return dto;
    }

    private Product convertToEntity(ProductRequestDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO);
        product.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        if (dto.getCategory() != null) {
            try {
                product.setCategory(ItemCategory.fromString(dto.getCategory()));
            } catch (IllegalArgumentException e) {
                throw new InvalidProductDataException("Invalid category: " + dto.getCategory());
            }
        }
        return product;
    }

    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ProductNotFoundException.forId(id));
        return convertToResponseDto(product);
    }

    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        validateProductRequest(requestDto);

        if (productRepository.existsByNameIgnoreCase(requestDto.getName())) {
            throw new ProductAlreadyExistsException("A product with name '" + requestDto.getName() + "' already exists.");
        }

        Product product = convertToEntity(requestDto);
        Product saved = productRepository.save(product);

        Long actorUserId = resolveActorUserId();
        ObjectNode details = activityLogService.createDetailsNode(
            "Product created: " + saved.getName(),
            "productName", saved.getName(),
            "price", saved.getPrice().toString(),
            "category", saved.getCategory() != null ? saved.getCategory().name() : "N/A"
        );
        activityLogService.logActivity(actorUserId, "CREATE", "PRODUCT", saved.getId(), details);

        return convertToResponseDto(saved);
    }

    public ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto) {
        validateProductRequest(requestDto);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Güncellenecek ürün bulunamadı, ID: " + id));

        // Check if another product with the new name already exists
        if (productRepository.existsByNameIgnoreCaseAndIdNot(requestDto.getName(), id)) {
            throw new ProductAlreadyExistsException("Another product with name '" + requestDto.getName() + "' already exists.");
        }

        product.setName(requestDto.getName());
        product.setDescription(requestDto.getDescription());
        product.setPrice(requestDto.getPrice());
        product.setIsActive(requestDto.getIsActive());

        if (requestDto.getCategory() != null) {
            try {
                product.setCategory(ItemCategory.fromString(requestDto.getCategory()));
            } catch (IllegalArgumentException e) {
                throw new InvalidProductDataException("Invalid category: " + requestDto.getCategory());
            }
        } else {
            product.setCategory(null);
        }

        Product updated = productRepository.save(product);

        Long actorUserId = resolveActorUserId();
        ObjectNode details = activityLogService.createDetailsNode(
            "Product updated: " + updated.getName(),
            "productName", updated.getName(),
            "price", updated.getPrice().toString(),
            "category", updated.getCategory() != null ? updated.getCategory().name() : "N/A"
        );

        activityLogService.logActivity(actorUserId, "UPDATE", "PRODUCT", updated.getId(), details);

        return convertToResponseDto(updated);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Silinecek ürün bulunamadı, ID: " + id));

        if (orderItemRepository.existsByProductId(id)) {
            Long actorUserId = resolveActorUserId();
            ObjectNode details = activityLogService.createDetailsNode(
                "Attempted to delete product but it is in use: " + product.getName(),
                "productName", product.getName(),
                "price", product.getPrice().toString(),
                "category", product.getCategory() != null ? product.getCategory().name() : "N/A"
            );
            activityLogService.logActivity(actorUserId, "DELETE_ATTEMPT", "PRODUCT", id, details);
            throw new ProductReferencedInOrder(id, product.getName());
        }

        Long actorUserId = resolveActorUserId();
        ObjectNode details = activityLogService.createDetailsNode(
            "Product deleted: " + product.getName(),
            "productName", product.getName(),
            "price", product.getPrice().toString(),
            "category", product.getCategory() != null ? product.getCategory().name() : "N/A"
        );

        activityLogService.logActivity(actorUserId, "DELETE", "PRODUCT", id, details);

        productRepository.delete(product);
    }

    // Calculates available quantities for all products based on current stock and required ingredients per unit
    public List<ProductAvailableQuantityDTO> getAvailableQuantitiesForAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductIngredient> allIngredients = productIngredientRepository.findAll();

        // Group ingredients by product ID
        Map<Long, List<ProductIngredient>> byProduct = new HashMap<>();
        for (ProductIngredient pi : allIngredients) {
            Long pid = pi.getProduct() != null ? pi.getProduct().getId() : (pi.getId() != null ? pi.getId().getProductId() : null);
            if (pid == null) continue;
            byProduct.computeIfAbsent(pid, k -> new ArrayList<>()).add(pi);
        }

        // Get active orders and calculate allocated quantities
        List<Order> activeOrders = orderRepository.findByIsCompleted(false);
        Map<Long, Long> allocatedQuantities = new HashMap<>();

        for (Order order : activeOrders) {
            if(order.getItems() != null){
                for (OrderItem item : order.getItems()) {
                    Long productId = item.getProduct().getId();
                    long quantity = item.getQuantity();
                    allocatedQuantities.merge(productId, quantity, Long::sum);
                }
            }
        }

        List<ProductAvailableQuantityDTO> result = new ArrayList<>();
        for (Product p : products) {
            Long pid = p.getId();
            List<ProductIngredient> list = byProduct.get(pid);
            long available = getAvailable(list);


            // Very important check: Existing orders should be considered, as the ingredients are already allocated to those. Simply passing off the amount based on stock is incorrect.
            // Therefore, collect all products that are in active orders and reduce their available quantity by the ordered amount.
            long allocatedQty = allocatedQuantities.getOrDefault(pid, 0L);
            available = Math.max(0L, available - allocatedQty);

            result.add(new ProductAvailableQuantityDTO(pid, available));
        }

        return result;
    }

    private static long getAvailable(List<ProductIngredient> list) {
        long available;

        if (list != null && !list.isEmpty()) {
            long min = Long.MAX_VALUE;
            boolean invalid = false;
            for (ProductIngredient pi : list) {
                BigDecimal required = pi.getQuantityPerUnit();

                if (required == null || required.compareTo(BigDecimal.ZERO) <= 0) { // invalid data
                    invalid = true;
                    break;
                }

                if (pi.getIngredient() == null || pi.getIngredient().getQuantity() == null) {
                    invalid = true;
                    break;
                }

                BigDecimal stockQty = pi.getIngredient().getQuantity();
                BigDecimal minQty = pi.getIngredient().getMinQuantity();

                // Normalize negatives and nulls
                if (stockQty.compareTo(BigDecimal.ZERO) < 0) stockQty = BigDecimal.ZERO;
                if (minQty == null || minQty.compareTo(BigDecimal.ZERO) < 0) minQty = BigDecimal.ZERO;

                // Calculate available quantity considering minQuantity
                BigDecimal usableQty = stockQty.subtract(minQty);

                // If usable quantity is negative or zero, we can't make any
                if (usableQty.compareTo(BigDecimal.ZERO) <= 0) {
                    min = 0L;
                    break;
                }

                long canMake = usableQty.divideToIntegralValue(required).longValue();
                if (canMake < min) min = canMake;
                if (min == 0L) break;
            }
            available = (invalid || min == Long.MAX_VALUE) ? 0L : min;
        } else {
            // No ingredients defined -> cannot produce
            available = 0L;
        }
        return available;
    }

    private void validateProductRequest(ProductRequestDto requestDto) {
        if (requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new InvalidProductDataException("Product name cannot be empty.");
        }
        if (requestDto.getPrice() == null) {
            throw new InvalidProductDataException("Product price is required.");
        }
        if (requestDto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidProductDataException("Product price cannot be negative.");
        }
        if (requestDto.getPrice().scale() > 2) {
            throw new InvalidProductDataException("Product price can have at most 2 decimal places.");
        }
    }

    private Long resolveActorUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        String usernameOrEmail = (principal instanceof UserDetails)
                ? ((UserDetails) principal).getUsername()
                : (principal instanceof String ? (String) principal : null);
        if (usernameOrEmail == null) return null;
        return userRepository.findByEmail(usernameOrEmail).map(u -> u.getId()).orElse(null);
    }
}
