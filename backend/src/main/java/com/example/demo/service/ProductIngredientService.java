// PASTE THIS entire content into ProductIngredientService.java

package com.example.demo.service;

import com.example.demo.dto.response.StockResponseDTO;
import com.example.demo.dto.response.ProductResponseDto;
import com.example.demo.dto.request.ProductIngredientRequestDTO;
import com.example.demo.dto.response.ProductIngredientResponseDTO;
import com.example.demo.model.ProductIngredient;
import com.example.demo.model.ProductIngredientId;
import com.example.demo.model.Product;
import com.example.demo.model.Stock;
import com.example.demo.repository.ProductIngredientRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.StockRepository;
import com.example.demo.exception.productingredient.ProductIngredientResourceNotFoundException;
import com.example.demo.exception.productingredient.DuplicateProductIngredientException;
import com.example.demo.validation.ProductIngredientValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductIngredientService {

    private final ProductIngredientRepository productIngredientRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final ProductIngredientValidator validator;

    @Autowired
    public ProductIngredientService(ProductIngredientRepository productIngredientRepository,
                                    ProductRepository productRepository,
                                    StockRepository stockRepository,
                                    ProductIngredientValidator validator) {
        this.productIngredientRepository = productIngredientRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.validator = validator;
    }

    @Transactional
    public ProductIngredientResponseDTO createProductIngredient(ProductIngredientRequestDTO requestDTO) {
        Product product = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new ProductIngredientResourceNotFoundException("Product not found with ID: " + requestDTO.getProductId()));
        Stock stock = stockRepository.findById(requestDTO.getIngredientId())
                .orElseThrow(() -> new ProductIngredientResourceNotFoundException("Stock not found with ID: " + requestDTO.getIngredientId()));

        if (productIngredientRepository.findByProductIdAndStockId(product.getId(), stock.getId()).isPresent()) {
            throw new DuplicateProductIngredientException("ProductIngredient already exists for Product ID: " + product.getId() + " and Stock ID: " + stock.getId());
        }

        validator.validateQuantity(requestDTO.getQuantityPerUnit());

        ProductIngredient productIngredient = new ProductIngredient();
        productIngredient.setProduct(product);
        productIngredient.setIngredient(stock);
        productIngredient.setQuantityPerUnit(requestDTO.getQuantityPerUnit());

        ProductIngredient savedProductIngredient = productIngredientRepository.save(productIngredient);

        return mapToResponseDTO(savedProductIngredient);
    }

    @Transactional(readOnly = true)
    public ProductIngredientResponseDTO getProductIngredientById(ProductIngredientId id) {
        ProductIngredient productIngredient = productIngredientRepository.findById(id)
                .orElseThrow(() -> new ProductIngredientResourceNotFoundException("ProductIngredient not found with ID: " + id));
        return mapToResponseDTO(productIngredient);
    }

    @Transactional(readOnly = true)
    public ProductIngredientResponseDTO getProductIngredientByIds(Long productId, Long stockId) {
        ProductIngredientId id = new ProductIngredientId(productId, stockId);
        return getProductIngredientById(id);
    }

    @Transactional(readOnly = true)
    public List<ProductIngredientResponseDTO> getAllProductIngredients() {
        return productIngredientRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductIngredientResponseDTO> getProductIngredientsByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductIngredientResourceNotFoundException("Product not found with ID: " + productId);
        }
        return productIngredientRepository.findByProductId(productId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductIngredientResponseDTO updateProductIngredient(ProductIngredientId id, ProductIngredientRequestDTO requestDTO) {
        ProductIngredient existingProductIngredient = productIngredientRepository.findById(id)
                .orElseThrow(() -> new ProductIngredientResourceNotFoundException("ProductIngredient not found with ID: " + id));

        existingProductIngredient.setQuantityPerUnit(requestDTO.getQuantityPerUnit());

        ProductIngredient updatedProductIngredient = productIngredientRepository.save(existingProductIngredient);
        return mapToResponseDTO(updatedProductIngredient);
    }

    @Transactional
    public ProductIngredientResponseDTO updateProductIngredientByIds(Long productId, Long stockId, ProductIngredientRequestDTO requestDTO) {
        ProductIngredientId id = new ProductIngredientId(productId, stockId);
        return updateProductIngredient(id, requestDTO);
    }

    @Transactional
    public void deleteProductIngredient(ProductIngredientId id) {
        if (!productIngredientRepository.existsById(id)) {
            throw new ProductIngredientResourceNotFoundException("ProductIngredient not found with ID: " + id);
        }
        productIngredientRepository.deleteById(id);
    }

    @Transactional
    public void deleteProductIngredientByIds(Long productId, Long stockId) {
        ProductIngredientId id = new ProductIngredientId(productId, stockId);
        deleteProductIngredient(id);
    }

    private ProductIngredientResponseDTO mapToResponseDTO(ProductIngredient productIngredient) {
        ProductResponseDto productDTO = new ProductResponseDto();
        productDTO.setId(productIngredient.getProduct().getId());
        productDTO.setName(productIngredient.getProduct().getName());
        productDTO.setDescription(productIngredient.getProduct().getDescription());
        productDTO.setPrice(productIngredient.getProduct().getPrice());
        productDTO.setIsActive(productIngredient.getProduct().getIsActive());
        if (productIngredient.getProduct().getCategory() != null) {
            productDTO.setCategory(productIngredient.getProduct().getCategory().name());
        }

        StockResponseDTO ingredientDTO = new StockResponseDTO();
        ingredientDTO.setId(productIngredient.getIngredient().getId());
        ingredientDTO.setName(productIngredient.getIngredient().getName());
        ingredientDTO.setUnit(productIngredient.getIngredient().getUnit());
        ingredientDTO.setStockQuantity(productIngredient.getIngredient().getQuantity());

        ProductIngredientResponseDTO responseDTO = new ProductIngredientResponseDTO();
        responseDTO.setId(productIngredient.getId());
        responseDTO.setProduct(productDTO);
        responseDTO.setIngredient(ingredientDTO);
        responseDTO.setQuantityPerUnit(productIngredient.getQuantityPerUnit());

        return responseDTO;
    }
}