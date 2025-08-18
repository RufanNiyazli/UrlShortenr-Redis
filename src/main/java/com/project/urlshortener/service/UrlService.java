package com.project.urlshortener.service;

import com.project.urlshortener.dto.UrlRequest;
import com.project.urlshortener.dto.UrlResponse;
import com.project.urlshortener.dto.UrlStats;
import com.project.urlshortener.entity.Url;
import com.project.urlshortener.repository.UrlRepository;
import com.project.urlshortener.util.RedisKeys;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger; // Logger üçün import
import org.slf4j.LoggerFactory; // LoggerFactory üçün import

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlService {
    // Logger obyektini yaratdıq
    private static final Logger log = LoggerFactory.getLogger(UrlService.class);

    private final UrlRepository urlRepository;
    private final StringRedisTemplate redisTemplate;
    @Value("${app.short.base-url}")
    private String baseUrL;
    private static final Duration URL_CACHE_TTL = Duration.ofHours(12);

    @Transactional
    public UrlResponse createShortCode(UrlRequest request) {
        String shortCode;
        // Gələn sorğu haqqında məlumatı loqlara yazırıq
        log.info("Yeni URL yaratma sorğusu: originalUrl={}, customCode={}", request.originalUrl(), request.customCode());

        if (request.customCode() != null && !request.customCode().isBlank()) {
            shortCode = request.customCode().trim();
            log.info("Kastom shortCode istifadə olunur: {}", shortCode);
            if (urlRepository.existsByShortCode(shortCode)) {
                log.warn("Bu kastom shortCode artıq mövcuddur: {}", shortCode);
                throw new RuntimeException("This short code already exists in the database!");
            }
        } else {
            do {
                shortCode = RandomStringUtils.secure().nextAlphanumeric(6);
                log.debug("Təsadüfi shortCode yaradılır: {}", shortCode);
            } while (urlRepository.existsByShortCode(shortCode));
            log.info("Təsadüfi shortCode seçildi: {}", shortCode);
        }

        Url url = Url.builder()
                .originalUrl(request.originalUrl())
                .createdAt(LocalDateTime.now())
                .clickCount(0L)
                .shortCode(shortCode)
                .build();
        urlRepository.save(url);
        log.info("URL verilənlər bazasına qeyd edildi: originalUrl={}, shortCode={}", url.getOriginalUrl(), url.getShortCode());

        String shortUrl = baseUrL + "/" + shortCode;
        redisTemplate.opsForValue().set(RedisKeys.codeToUlr(shortCode), request.originalUrl(), URL_CACHE_TTL);
        log.info("URL Redis keşinə əlavə edildi: key={}, originalUrl={}", RedisKeys.codeToUlr(shortCode), request.originalUrl());

        return new UrlResponse(request.originalUrl(), shortUrl, shortCode);
    }

    @Transactional(readOnly = true)
    public HttpHeaders redirect(String shortCode) {
        log.info("Yönləndirmə sorğusu üçün shortCode: {}", shortCode); // Gələn shortCode-u loqlaşdır
        String key = RedisKeys.codeToUlr(shortCode);
        String originalUrl = redisTemplate.opsForValue().get(key);

        if (originalUrl == null) {
            log.info("Redis keşində tapılmadı, verilənlər bazasından axtarılır: shortCode={}", shortCode);
            Optional<Url> urlOptional = urlRepository.findByShortCode(shortCode);
            if (urlOptional.isPresent()) {
                Url url = urlOptional.get();
                originalUrl = url.getOriginalUrl();
                log.info("URL verilənlər bazasında tapıldı: originalUrl={}", originalUrl);
                redisTemplate.opsForValue().set(key, originalUrl, URL_CACHE_TTL);
                log.info("URL Redis keşinə əlavə edildi: key={}, originalUrl={}", key, originalUrl);
            } else {
                log.error("URL verilənlər bazasında tapılmadı: shortCode={}", shortCode); // Tapılmadığı halı loqlara yazırıq
                throw new RuntimeException("URL tapılmadı!");
            }
        } else {
            log.info("URL Redis keşində tapıldı: originalUrl={}", originalUrl);
        }

        redisTemplate.opsForValue().increment(RedisKeys.clickCounter(shortCode));
        log.info("Klik sayğacı artırıldı: shortCode={}", shortCode);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setLocation(URI.create(originalUrl));
        return headers;
    }


    @Transactional(readOnly = true)
    public UrlStats getStats(String shortCode) {
        log.info("Statistika sorğusu üçün shortCode: {}", shortCode); // Gələn shortCode-u loqlaşdır
        Optional<Url> urlOptional = urlRepository.findByShortCode(shortCode);
        if (urlOptional.isPresent()) {
            Url url = urlOptional.get();
            Long dbClicks = Optional.ofNullable(url.getClickCount()).orElse(0L);
            Long delta = getCurrentDelta(shortCode);
            Long total = dbClicks + delta;
            log.info("Statistika tapıldı: shortCode={}, originalUrl={}, totalClicks={}", shortCode, url.getOriginalUrl(), total);
            return new UrlStats(url.getOriginalUrl(), shortCode, total);
        } else {
            log.error("Statistika üçün URL tapılmadı: shortCode={}", shortCode); // Tapılmadığı halı loqlara yazırıq
            throw new RuntimeException("URL tapılmadı!");
        }
    }

    private Long getCurrentDelta(String shortCode) {
        String v = redisTemplate.opsForValue().get(RedisKeys.clickCounter(shortCode));
        log.info("Redis-dən cari delta alındı: shortCode={}, delta={}", shortCode, v); // Delta dəyərini loqlara yazırıq
        return v == null ? 0L : Long.parseLong(v);
    }
}
