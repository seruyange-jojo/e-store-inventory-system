package com.joseph.ismes.controller;

import com.joseph.ismes.dto.PurchaseRequest;
import com.joseph.ismes.dto.PurchaseResponse;
import com.joseph.ismes.service.PurchaseService;
import com.joseph.ismes.entity.Purchase;
import com.joseph.ismes.exception.ResourceNotFoundException;
import com.joseph.ismes.repository.PurchaseRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;
    private final PurchaseRepository purchaseRepository;

    @GetMapping
    public List<PurchaseResponse> getRecent() { return purchaseService.findRecent(); }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PurchaseResponse getById(@PathVariable Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found: " + id));
        return PurchaseResponse.fromEntity(purchase);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse create(@Valid @RequestBody PurchaseRequest request) { return purchaseService.create(request); }
}