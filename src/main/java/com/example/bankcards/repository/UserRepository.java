package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    List<User> findByRole(UserRole role);
    List<User> findByActiveTrue();
}
