package com.joseph.ismes.controller;

import com.joseph.ismes.dto.SaleRequest;
import com.joseph.ismes.dto.SaleResponse;
import com.joseph.ismes.dto.SaleDetailResponse;
import com.joseph.ismes.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.joseph.ismes.entity.Sale;
import com.joseph.ismes.entity.SaleItem;
import com.joseph.ismes.exception.ResourceNotFoundException;
import com.joseph.ismes.repository.SaleRepository;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService saleService;
    private final SaleRepository saleRepository;

    @GetMapping
    public List<SaleResponse> getRecent() { return saleService.findRecent(); }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public SaleDetailResponse getById(@PathVariable Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + id));
        return SaleDetailResponse.fromEntity(sale);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse create(@Valid @RequestBody SaleRequest request) { return saleService.create(request); }
}