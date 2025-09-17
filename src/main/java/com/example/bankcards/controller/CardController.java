package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final TransferService transferService;

    @GetMapping("/my")
    public ResponseEntity<Page<CardDto>> getMyCards(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<CardDto> cards = cardService.getUserCardsByUsername(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id) {
        CardDto card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<CardDto>> getAllCards() {
        List<CardDto> cards = cardService.getAllCards();
        return ResponseEntity.ok(cards);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CardCreateRequest request) {
        CardDto card = cardService.createCard(request);
        return ResponseEntity.ok(card);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<CardDto> updateCardStatus(
            @PathVariable Long id,
            @RequestParam CardStatus status) {

        CardDto card = cardService.updateCardStatus(id, status);
        return ResponseEntity.ok(card);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transferBetweenMyCards(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransferRequest request) {

        Long userId = 1L;

        transferService.transferBetweenUserCards(userId, request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/transfer")
    public ResponseEntity<Void> transferBetweenCards(
            @Valid @RequestBody TransferRequest request) {

        transferService.transferBetweenCards(request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}