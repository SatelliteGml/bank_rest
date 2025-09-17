package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByUser(User user, Pageable pageable);
    List<Card> findByUser(User user);
    List<Card> findByStatus(CardStatus status);
    List<Card> findByExpirationDateBefore(LocalDate date);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    List<Card> findActiveCardsByUserId(@Param("userId") Long userId);

    Optional<Card> findByIdAndUser(Long id, User user);

    @Query("SELECT c FROM Card c WHERE c.user.username = :username")
    Page<Card> findByUsername(@Param("username") String username, Pageable pageable);

    boolean existsByEncryptedCardNumber(String encryptedCardNumber);
}
