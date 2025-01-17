package com.robedev.photosong_backend.controllers;
import com.robedev.photosong_backend.services.DeezerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/")
@CrossOrigin(origins = {"http://localhost:4200", "https://photosong.vercel.app"})
public class DeezerController {

    @Autowired
    private DeezerService deezerService;

    @GetMapping("/api/deezer/song")
    public Mono<ResponseEntity<String>> getSong(@RequestParam String artist, @RequestParam String songTitle) {
        return deezerService.findSongOnDeezer(artist, songTitle)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }

    @PostMapping("/api/deezer/playlist")
    public Mono<ResponseEntity<List<Map<String, String>>>> getPlaylist(@RequestBody List<Map<String, String>> songs) {
        return deezerService.findMultipleSongsOnDeezer(songs)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(null)));
    }
}
