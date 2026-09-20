package com.joseph.ismes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.joseph.ismes.entity.Product;
import com.joseph.ismes.exception.GlobalExceptionHandler;
import com.joseph.ismes.repository.CategoryRepository;
import com.joseph.ismes.repository.ProductRepository;
import com.joseph.ismes.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {
    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        ProductService service = new ProductService(productRepository, categoryRepository);
        mvc = MockMvcBuilders.standaloneSetup(new ProductController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void updateWithoutOpeningStockPreservesExistingStock() throws Exception {
        Product product = existingProduct();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mvc.perform(put("/api/products/1").contentType(MediaType.APPLICATION_JSON)
                        .content(validDetails().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated product"))
                .andExpect(jsonPath("$.openingStock").value(20))
                .andExpect(jsonPath("$.currentStock").value(7));

        assertThat(product.getOpeningStock()).isEqualTo(20);
        assertThat(product.getCurrentStock()).isEqualTo(7);
    }

    @Test
    void updateCannotOverwriteStockThroughExtraJsonFields() throws Exception {
        Product product = existingProduct();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ObjectNode request = validDetails().put("openingStock", 999).put("currentStock", 999);

        mvc.perform(put("/api/products/1").contentType(MediaType.APPLICATION_JSON).content(request.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openingStock").value(20))
                .andExpect(jsonPath("$.currentStock").value(7));
    }

    @Test
    void creationInitializesCurrentStockFromOpeningStock() throws Exception {
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content(validDetails().put("openingStock", 20).toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.openingStock").value(20))
                .andExpect(jsonPath("$.currentStock").value(20));
    }

    @Test
    void creationStillRequiresOpeningStock() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content(validDetails().toString()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productRepository, categoryRepository);
    }

    @ParameterizedTest
    @MethodSource("invalidDetails")
    void invalidProductDetailsReturnBadRequestBeforeDatabaseAccess(String field, String value) throws Exception {
        ObjectNode request = validDetails().put("openingStock", 20);
        if (value == null) {
            request.putNull(field);
        } else {
            request.put(field, value);
        }

        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(request.toString()))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/products/1").contentType(MediaType.APPLICATION_JSON).content(request.toString()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productRepository, categoryRepository);
    }

    @Test
    void missingProductReturnsNotFound() throws Exception {
        mvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found: 999"));
    }

    @Test
    void missingCategoryReturnsNotFound() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content(validDetails().put("openingStock", 20).put("categoryId", 999).toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found: 999"));
    }

    private static Stream<Arguments> invalidDetails() {
        return Stream.of(
                Arguments.of("productCode", "X".repeat(51)),
                Arguments.of("name", "X".repeat(151)),
                Arguments.of("unit", "X".repeat(21)),
                Arguments.of("unit", " "),
                Arguments.of("unit", null),
                Arguments.of("buyingPrice", "1000000000000.00"),
                Arguments.of("sellingPrice", "1000000000000.00"),
                Arguments.of("buyingPrice", "1.001"),
                Arguments.of("sellingPrice", "1.001"),
                Arguments.of("buyingPrice", "-0.01"),
                Arguments.of("sellingPrice", null),
                Arguments.of("minStockLevel", "-1"),
                Arguments.of("categoryId", "0")
        );
    }

    private ObjectNode validDetails() {
        return objectMapper.createObjectNode()
                .put("productCode", "TEST-001")
                .put("name", "Updated product")
                .put("unit", "pcs")
                .put("buyingPrice", new BigDecimal("10.00"))
                .put("sellingPrice", new BigDecimal("15.00"))
                .put("minStockLevel", 5);
    }

    private Product existingProduct() {
        return Product.builder()
                .id(1L)
                .productCode("TEST-001")
                .name("Original product")
                .buyingPrice(new BigDecimal("10.00"))
                .sellingPrice(new BigDecimal("15.00"))
                .openingStock(20)
                .currentStock(7)
                .minStockLevel(5)
                .active(true)
                .build();
    }
}
