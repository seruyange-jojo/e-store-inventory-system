package com.joseph.ismes.service;

import com.joseph.ismes.dto.SaleRequest;
import com.joseph.ismes.dto.SaleResponse;
import com.joseph.ismes.entity.*;
import com.joseph.ismes.exception.ResourceNotFoundException;
import com.joseph.ismes.repository.ProductRepository;
import com.joseph.ismes.repository.SaleRepository;
import com.joseph.ismes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SaleResponse> findRecent() {
        return saleRepository.findTop50ByOrderBySaleDateDesc().stream().map(SaleResponse::fromEntity).toList();
    }

    @Transactional
    public SaleResponse create(SaleRequest request) {
        User user = currentUser();
        Sale sale = Sale.builder()
                .receiptNumber("R-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")))
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .paymentMethod(request.getPaymentMethod())
                .totalAmount(BigDecimal.ZERO)
                .createdBy(user)
                .build();
        BigDecimal total = BigDecimal.ZERO;
        for (SaleRequest.SaleItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.getProductId()));
            if (itemRequest.getQuantity() <= 0 || product.getCurrentStock() < itemRequest.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }
            boolean belowCost = itemRequest.getUnitPrice().compareTo(product.getBuyingPrice()) < 0;
            if (belowCost && user.getRole() != Role.ADMIN) {
                throw new org.springframework.security.access.AccessDeniedException("Admin approval is required for below-cost sales");
            }
            BigDecimal subtotal = itemRequest.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            sale.getItems().add(SaleItem.builder().sale(sale).product(product).quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice()).buyingPriceAtSale(product.getBuyingPrice())
                    .subtotal(subtotal).belowCost(belowCost).authorizedBy(belowCost ? user : null).build());
            product.setCurrentStock(product.getCurrentStock() - itemRequest.getQuantity());
            total = total.add(subtotal);
        }
        sale.setTotalAmount(total);
        productRepository.flush();
        return SaleResponse.fromEntity(saleRepository.save(sale));
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}