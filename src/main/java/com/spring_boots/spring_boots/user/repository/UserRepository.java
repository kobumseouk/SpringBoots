package com.spring_boots.spring_boots.user.repository;

import com.spring_boots.spring_boots.user.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users,Long> {
    Optional<Users> findByEmail(String email);
    boolean existsByUserRealId(String userRealId);

    Optional<Users> findByUserRealId(String userRealId);
}
