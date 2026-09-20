package com.joseph.ismes.controller;

import com.joseph.ismes.dto.SaleRequest;
import com.joseph.ismes.dto.SaleResponse;
import com.joseph.ismes.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService saleService;

    @GetMapping
    public List<SaleResponse> getRecent() { return saleService.findRecent(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse create(@Valid @RequestBody SaleRequest request) { return saleService.create(request); }
}