package com.example.demo.controller;

import com.example.demo.dto.request.ProductIngredientRequestDTO;
import com.example.demo.dto.response.ProductIngredientResponseDTO;
import com.example.demo.service.ProductIngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Product Stock Management",
        description = "API for managing product-ingredient relationships (CRUD operations)."
)
@RestController
@RequestMapping("/api/product-ingredients")
@CrossOrigin(origins = "*")
public class ProductIngredientController {

    private final ProductIngredientService productIngredientService;

    @Autowired
    public ProductIngredientController(ProductIngredientService productIngredientService) {
        this.productIngredientService = productIngredientService;
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "Create a product ingredient",
            description = "Assigns an ingredient to a product."
    )
    public ResponseEntity<ProductIngredientResponseDTO> createProductIngredient(
            @Valid @RequestBody ProductIngredientRequestDTO requestDTO) {
        ProductIngredientResponseDTO response = productIngredientService.createProductIngredient(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{productId}/{stockId}")
    @Operation(
            summary = "Get product ingredient by composite ID",
            description = "Retrieves a specific product-ingredient relationship by product ID and stock ID."
    )
    public ResponseEntity<ProductIngredientResponseDTO> getProductIngredientById(
            @Parameter(description = "ID of the product", required = true)
            @PathVariable Long productId,
            @Parameter(description = "ID of the stock/ingredient", required = true)
            @PathVariable Long stockId) {
        ProductIngredientResponseDTO response = productIngredientService.getProductIngredientByIds(productId, stockId);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    @Operation(
            summary = "Get all product ingredients",
            description = "Retrieves a list of all product-ingredient relationships."
    )
    public ResponseEntity<List<ProductIngredientResponseDTO>> getAllProductIngredients() {
        List<ProductIngredientResponseDTO> responses = productIngredientService.getAllProductIngredients();
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/product/{productId}")
    @Operation(
            summary = "Get ingredients by product ID",
            description = "Retrieves all ingredients for a specific product."
    )
    public ResponseEntity<List<ProductIngredientResponseDTO>> getProductIngredientsByProductId(
            @Parameter(description = "ID of the product to retrieve ingredients for", required = true)
            @PathVariable Long productId) {
        List<ProductIngredientResponseDTO> responses = productIngredientService.getProductIngredientsByProductId(productId);
        return ResponseEntity.ok(responses);
    }


    @PutMapping("/{productId}/{stockId}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "Update a product ingredient",
            description = "Updates an existing product-ingredient relationship."
    )
    public ResponseEntity<ProductIngredientResponseDTO> updateProductIngredient(
            @Parameter(description = "ID of the product", required = true)
            @PathVariable Long productId,
            @Parameter(description = "ID of the stock/ingredient", required = true)
            @PathVariable Long stockId,
            @Valid @RequestBody ProductIngredientRequestDTO requestDTO) {
        ProductIngredientResponseDTO response = productIngredientService.updateProductIngredientByIds(productId, stockId, requestDTO);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{productId}/{stockId}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "Delete a product ingredient",
            description = "Deletes a product-ingredient relationship by product ID and stock ID."
    )
    public ResponseEntity<Void> deleteProductIngredient(
            @Parameter(description = "ID of the product", required = true)
            @PathVariable Long productId,
            @Parameter(description = "ID of the stock/ingredient", required = true)
            @PathVariable Long stockId) {
        productIngredientService.deleteProductIngredientByIds(productId, stockId);
        return ResponseEntity.noContent().build();
    }
}