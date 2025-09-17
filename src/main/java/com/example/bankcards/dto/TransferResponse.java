package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    private String status;
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
    private String description;
    private LocalDateTime timestamp;
}
