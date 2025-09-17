package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardCreateRequest {

    @NotBlank(message = "Card holder is required")
    private String cardHolder;

    @NotNull(message = "Expiration date is required")
    private LocalDate expirationDate;

    private BigDecimal initialBalance = BigDecimal.ZERO;

    @NotNull(message = "User ID is required")
    private Long userId;
}
