package com.example.bankcards.repository;

import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t WHERE t.fromCard.user = :user OR t.toCard.user = :user")
    Page<Transaction> findByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.fromCard.id = :cardId OR t.toCard.id = :cardId")
    List<Transaction> findByCardId(@Param("cardId") Long cardId);

    List<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
