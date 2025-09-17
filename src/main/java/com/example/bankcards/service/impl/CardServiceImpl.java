package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.mapper.CardMapper;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getUserCards(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return cardRepository.findByUser(user, pageable)
                .map(cardMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getUserCardsByUsername(String username, Pageable pageable) {
        return cardRepository.findByUsername(username, pageable)
                .map(cardMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardDto> getAllCards() {
        return cardRepository.findAll().stream()
                .map(cardMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CardDto getCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + id));
        return cardMapper.toDto(card);
    }

    @Override
    @Transactional(readOnly = true)
    public CardDto getUserCardById(Long userId, Long cardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Card card = cardRepository.findByIdAndUser(cardId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        return cardMapper.toDto(card);
    }

    @Override
    @Transactional
    public CardDto createCard(CardCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        String cardNumber = CardNumberGenerator.generate();
        String encryptedCardNumber = EncryptionUtil.encrypt(cardNumber);
        String cvv = CardNumberGenerator.generateCVV();

        // Check if card number already exists (extremely unlikely but possible)
        if (cardRepository.existsByEncryptedCardNumber(encryptedCardNumber)) {
            throw new IllegalStateException("Generated card number already exists");
        }

        Card card = new Card();
        card.setEncryptedCardNumber(encryptedCardNumber);
        card.setCardHolder(request.getCardHolder());
        card.setExpirationDate(request.getExpirationDate());
        card.setBalance(request.getInitialBalance());
        card.setStatus(CardStatus.ACTIVE);
        card.setUser(user);
        card.setCvv(EncryptionUtil.encrypt(cvv));

        Card savedCard = cardRepository.save(card);
        return cardMapper.toDto(savedCard);
    }

    @Override
    @Transactional
    public CardDto updateCardStatus(Long id, CardStatus status) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + id));

        card.setStatus(status);
        Card updatedCard = cardRepository.save(card);
        return cardMapper.toDto(updatedCard);
    }

    @Override
    @Transactional
    public void deleteCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + id));
        cardRepository.delete(card);
    }

    @Override
    @Transactional
    public void checkExpiredCards() {
        LocalDate today = LocalDate.now();
        List<Card> expiredCards = cardRepository.findByExpirationDateBefore(today);

        for (Card card : expiredCards) {
            if (card.getStatus() != CardStatus.EXPIRED) {
                card.setStatus(CardStatus.EXPIRED);
                cardRepository.save(card);
            }
        }
    }
}