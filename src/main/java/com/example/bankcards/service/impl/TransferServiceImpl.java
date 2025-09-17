package com.example.bankcards.service.impl;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public void transferBetweenUserCards(Long userId, TransferRequest request) {
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + request.getFromCardId()));

        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + request.getToCardId()));

        // Проверяем, что обе карты принадлежат пользователю
        if (!fromCard.getUser().getId().equals(userId) || !toCard.getUser().getId().equals(userId)) {
            throw new SecurityException("User can only transfer between their own cards");
        }

        performTransferChecks(fromCard, toCard, request.getAmount());
        performTransfer(fromCard, toCard, request.getAmount(), request.getDescription());
    }

    @Override
    @Transactional
    public void transferBetweenCards(TransferRequest request) {
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + request.getFromCardId()));

        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + request.getToCardId()));

        performTransferChecks(fromCard, toCard, request.getAmount());
        performTransfer(fromCard, toCard, request.getAmount(), request.getDescription());
    }

    // ✅ Проверки на активность и баланс
    private void performTransferChecks(Card fromCard, Card toCard, BigDecimal amount) {
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Source card is not active");
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Destination card is not active");
        }
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds on source card");
        }
    }

    // ✅ Логика перевода и сохранение транзакции
    private void performTransfer(Card fromCard, Card toCard, BigDecimal amount, String description) {
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transaction transaction = new Transaction();
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setDescription(description != null ? description : "Transfer between cards");

        transactionRepository.save(transaction);
    }
}
