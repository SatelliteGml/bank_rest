package com.example.bankcards.service;

import com.example.bankcards.dto.BlockCardResponse;
import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CardService {
    Page<CardDto> getUserCards(Long userId, Pageable pageable);
    Page<CardDto> getUserCardsByUsername(String username, Pageable pageable);
    List<CardDto> getAllCards();
    CardDto getCardById(Long id);
    CardDto getUserCardById(Long userId, Long cardId);
    CardDto createCard(CardCreateRequest request);
    CardDto updateCardStatus(Long id, CardStatus status);
    void deleteCard(Long id);
    void checkExpiredCards();
    BlockCardResponse blockCard(Long cardId, Long userId);
    BlockCardResponse unblockCard(Long cardId, Long userId);
}