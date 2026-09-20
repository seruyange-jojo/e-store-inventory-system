package com.joseph.ismes.controller;

import com.joseph.ismes.dto.ExpenseRequest;
import com.joseph.ismes.dto.ExpenseResponse;
import com.joseph.ismes.entity.Expense;
import com.joseph.ismes.entity.ExpenseCategory;
import com.joseph.ismes.entity.User;
import com.joseph.ismes.exception.ResourceNotFoundException;
import com.joseph.ismes.repository.ExpenseCategoryRepository;
import com.joseph.ismes.repository.ExpenseRepository;
import com.joseph.ismes.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAll() {
        return expenseRepository.findTop50ByOrderByExpenseDateDescCreatedAtDesc().stream()
                .map(ExpenseResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ExpenseResponse getById(@PathVariable Long id) {
        return ExpenseResponse.fromEntity(expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id)));
    }

    @GetMapping("/categories")
    public List<ExpenseCategory> getCategories() {
        return categoryRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody ExpenseRequest request) {
        ExpenseCategory category = request.getCategoryId() == null ? null : categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense category not found: " + request.getCategoryId()));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Expense expense = Expense.builder()
                .category(category)
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate() == null ? LocalDate.now() : request.getExpenseDate())
                .createdBy(user)
                .build();
        return ExpenseResponse.fromEntity(expenseRepository.save(expense));
    }
}