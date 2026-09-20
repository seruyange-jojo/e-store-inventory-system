package com.joseph.ismes.controller;

import com.joseph.ismes.dto.PurchaseRequest;
import com.joseph.ismes.dto.PurchaseResponse;
import com.joseph.ismes.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;

    @GetMapping
    public List<PurchaseResponse> getRecent() { return purchaseService.findRecent(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse create(@Valid @RequestBody PurchaseRequest request) { return purchaseService.create(request); }
}