package com.spring_boots.spring_boots.user.repository;

import com.spring_boots.spring_boots.user.domain.TokenRedis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface TokenRedisRepository extends CrudRepository<TokenRedis,String> {
    Optional<TokenRedis> findByRefreshToken(String accessToken);
}
