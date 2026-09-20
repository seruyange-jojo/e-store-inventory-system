package com.joseph.ismes.service;

import com.joseph.ismes.dto.PurchaseRequest;
import com.joseph.ismes.dto.PurchaseResponse;
import com.joseph.ismes.entity.*;
import com.joseph.ismes.exception.ResourceNotFoundException;
import com.joseph.ismes.repository.ProductRepository;
import com.joseph.ismes.repository.PurchaseRepository;
import com.joseph.ismes.repository.SupplierRepository;
import com.joseph.ismes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PurchaseResponse> findRecent() {
        return purchaseRepository.findTop50ByOrderByPurchaseDateDescCreatedAtDesc().stream()
                .map(PurchaseResponse::fromEntity)
                .toList();
    }

    @Transactional
    public PurchaseResponse create(PurchaseRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.getSupplierId()));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Purchase purchase = Purchase.builder()
                .supplier(supplier)
                .purchaseDate(request.getPurchaseDate())
                .referenceNumber(request.getReferenceNumber())
                .notes(request.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .createdBy(user)
                .build();
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseRequest.PurchaseItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getQuantity() <= 0 || itemRequest.getUnitCost().signum() < 0) {
                throw new IllegalArgumentException("Purchase quantity and cost must be valid");
            }
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.getProductId()));
            BigDecimal subtotal = itemRequest.getUnitCost().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            purchase.getItems().add(PurchaseItem.builder().purchase(purchase).product(product)
                    .quantity(itemRequest.getQuantity()).unitCost(itemRequest.getUnitCost()).subtotal(subtotal).build());
            product.setCurrentStock(product.getCurrentStock() + itemRequest.getQuantity());
            product.setBuyingPrice(itemRequest.getUnitCost());
            total = total.add(subtotal);
        }
        purchase.setTotalAmount(total);
        productRepository.flush();
        return PurchaseResponse.fromEntity(purchaseRepository.save(purchase));
    }
}