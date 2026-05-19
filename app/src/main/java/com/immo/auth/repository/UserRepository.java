package com.immo.auth.repository;

import com.immo.auth.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
    List<User> findByRoleIn(List<com.immo.auth.entity.Role> roles);
    boolean existsByEmailIgnoreCase(String email);
}
