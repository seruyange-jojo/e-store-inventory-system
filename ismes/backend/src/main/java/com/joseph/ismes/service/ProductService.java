package com.joseph.ismes.service;

import com.joseph.ismes.dto.ProductRequest;
import com.joseph.ismes.dto.ProductResponse;
import com.joseph.ismes.dto.ProductUpdateRequest;
import com.joseph.ismes.entity.Category;
import com.joseph.ismes.entity.Product;
import com.joseph.ismes.exception.ResourceNotFoundException;
import com.joseph.ismes.repository.CategoryRepository;
import com.joseph.ismes.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findByActiveTrue().stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> search(String term) {
        return productRepository
                .findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndProductCodeContainingIgnoreCase(term, term).stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findLowStock() {
        return productRepository.findLowStockProducts().stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = getProductOrThrow(id);
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new IllegalStateException("Product code already exists: " + request.getProductCode());
        }

        Category category = resolveCategory(request.getCategoryId());

        Product product = Product.builder()
                .productCode(request.getProductCode())
                .name(request.getName())
                .category(category)
                .description(request.getDescription())
                .unit(request.getUnit())
                .buyingPrice(request.getBuyingPrice())
                .sellingPrice(request.getSellingPrice())
                .openingStock(request.getOpeningStock())
                .currentStock(request.getOpeningStock()) // opening stock becomes current stock at creation
                .minStockLevel(request.getMinStockLevel())
                .active(true)
                .build();

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = getProductOrThrow(id);

        // If product code is changing, ensure the new one isn't taken by another product.
        if (!product.getProductCode().equals(request.getProductCode())
                && productRepository.existsByProductCode(request.getProductCode())) {
            throw new IllegalStateException("Product code already exists: " + request.getProductCode());
        }

        product.setProductCode(request.getProductCode());
        product.setName(request.getName());
        product.setCategory(resolveCategory(request.getCategoryId()));
        product.setDescription(request.getDescription());
        product.setUnit(request.getUnit());
        product.setBuyingPrice(request.getBuyingPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setMinStockLevel(request.getMinStockLevel());
        // Editing product details preserves both the original opening stock and current stock.

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    public void deactivate(Long id) {
        Product product = getProductOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }
}
