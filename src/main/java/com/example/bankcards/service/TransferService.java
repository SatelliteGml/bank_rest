package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;

public interface TransferService {
    void transferBetweenUserCards(Long userId, TransferRequest request);
    void transferBetweenCards(TransferRequest request);
}
