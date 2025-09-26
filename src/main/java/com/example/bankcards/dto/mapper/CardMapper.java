package com.example.bankcards.dto.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.util.EncryptionUtil;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public CardDto toDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setMaskedCardNumber(maskCardNumber(card.getEncryptedCardNumber()));
        dto.setCardHolder(card.getCardHolder());
        dto.setExpirationDate(card.getExpirationDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        dto.setIsBlocked(card.getIsBlocked());
        return dto;
    }

    private String maskCardNumber(String encryptedCardNumber) {
        String decrypted = EncryptionUtil.decrypt(encryptedCardNumber);
        if (decrypted.length() >= 16) {
            return "**** **** **** " + decrypted.substring(decrypted.length() - 4);
        }
        return "**** **** **** ****";
    }
}