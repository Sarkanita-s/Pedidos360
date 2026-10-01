package com.pedidos360.backend.controller;

import com.pedidos360.backend.model.Product;
import com.pedidos360.backend.service.ProductService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'Operador', 'Cliente')")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'Operador', 'Cliente')")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product) {

        Product savedProduct = productService.createProduct(product);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedProduct);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product productDetails) {

        return ResponseEntity.ok(
                productService.updateProduct(id, productDetails));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }
}
