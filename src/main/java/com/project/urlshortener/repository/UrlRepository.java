package com.project.urlshortener.repository;

import com.project.urlshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    boolean existsByShortCode(String shortCode);

    Optional<Url> findByShortCode(String shortCode);
}
