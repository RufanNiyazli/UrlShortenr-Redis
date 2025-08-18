package com.project.urlshortener.jobs;

import com.project.urlshortener.entity.Url;
import com.project.urlshortener.repository.UrlRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ClickFlush {
    private final StringRedisTemplate redisTemplate;
    private final UrlRepository repository;
    private final UrlRepository urlRepository;

    @Value("${app.click.flush-interval-seconds}")
    private int flushIntervalSec;

    @Scheduled(fixedDelayString = "${app.click.flush-interval-seconds}")
    @Transactional
    public void flushCLick() {
        Set<String> keys = redisTemplate.keys("cc:*");
        if (keys == null || keys.isEmpty()) return;
        Map<String, Long> deltas = new HashMap<>();
        for (String key : keys) {
            String val = redisTemplate.opsForValue().get(key);
            assert val != null;
            long delta = Long.parseLong(val);
            String shortCode = key.substring("cc:".length());
            deltas.put(shortCode, delta);
            redisTemplate.delete(key);


        }
        if (deltas.isEmpty()) return;
        List<Url> urls = repository.findAll().stream()
                .filter(u -> deltas.containsKey(u.getShortCode()))
                .peek(u -> u.setClickCount(u.getClickCount() + deltas.get(u.getShortCode())))

                .toList();
        if (urls.isEmpty()) {
            repository.saveAll(urls);
        }

    }

}
