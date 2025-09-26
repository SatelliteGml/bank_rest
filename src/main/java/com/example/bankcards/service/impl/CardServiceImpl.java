package com.example.bankcards.service.impl;

import com.example.bankcards.dto.BlockCardResponse;
import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.mapper.CardMapper;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.UserRole;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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

    @Override
    @Transactional
    public BlockCardResponse blockCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        checkCardPermission(card, userId, "block");
        validateCardForBlocking(card);

        card.setIsBlocked(true);
        if (card.getStatus() == CardStatus.ACTIVE) {
            card.setStatus(CardStatus.BLOCKED);
        }

        cardRepository.save(card);

        return new BlockCardResponse("The card is successfully blocked");
    }


    @Override
    @Transactional
    public BlockCardResponse unblockCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        checkCardPermission(card, userId, "unblock");
        validateCardForUnblocking(card);

        card.setIsBlocked(false);
        if (isCardNotExpired(card)) {
            card.setStatus(CardStatus.ACTIVE);
        } else {
            card.setStatus(CardStatus.EXPIRED);
        }
        cardRepository.save(card);

        return new BlockCardResponse("The card is successfully un-blocked");
    }

    private void checkCardPermission(Card card, Long userId, String action) {
        User currentUser = getUserById(userId);

        if (currentUser.getRole() != UserRole.ADMIN && !card.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "No permission to " + action + " this card. Card owner id: " +
                            card.getUser().getId() + ", current user id: " + userId
            );
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private void validateCardForBlocking(Card card) {
        if (!isCardNotExpired(card)) {
            throw new IllegalStateException("Cannot block expired card with id: " + card.getId());
        }

        if (card.getIsBlocked()) {
            throw new IllegalStateException("Card is already blocked with id: " + card.getId());
        }
    }

    private void validateCardForUnblocking(Card card) {
        if (!isCardNotExpired(card)) {
            System.out.println("Warning: Unblocking expired card id: " + card.getId());
        }

        if (!card.getIsBlocked()) {
            throw new IllegalStateException("Card is not blocked with id: " + card.getId());
        }
    }

    private boolean isCardNotExpired(Card card) {
        return card.getExpirationDate().isAfter(LocalDate.now()) ||
                card.getExpirationDate().isEqual(LocalDate.now());
    }
}