package com.project.urlshortener.controller;

import com.project.urlshortener.dto.UrlRequest;
import com.project.urlshortener.dto.UrlResponse;
import com.project.urlshortener.dto.UrlStats;
import com.project.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity; // Import ResponseEntity
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/api/url/create")
    public UrlResponse createShortCode(@RequestBody UrlRequest request) {
        return urlService.createShortCode(request);
    }

    // New method to handle favicon.ico specifically
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> handleFavicon() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Or return an actual favicon.
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        HttpHeaders headers = urlService.redirect(shortCode);

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/api/url/{shortCode}/stats")
    public UrlStats stats(@PathVariable String shortCode) {
        return urlService.getStats(shortCode);
    }
}