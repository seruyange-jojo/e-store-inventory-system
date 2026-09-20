package com.joseph.ismes.controller;

import com.joseph.ismes.dto.ProductRequest;
import com.joseph.ismes.dto.ProductResponse;
import com.joseph.ismes.dto.ProductUpdateRequest;
import com.joseph.ismes.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Both roles can view products
    @GetMapping
    public List<ProductResponse> getAll(@RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return productService.search(search);
        }
        return productService.findAll();
    }

    @GetMapping("/low-stock")
    public List<ProductResponse> getLowStock() {
        return productService.findLowStock();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    // Only ADMIN can create, edit, or deactivate products
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        productService.deactivate(id);
    }
}
