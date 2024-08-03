package com.robedev.photosong_backend.services;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class DeezerService {

    private final WebClient webClient;

    public DeezerService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.deezer.com").build();
    }

    private String encodeURIComponent(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public Mono<String> findSongOnDeezer(String artist, String songTitle) {
        String apiUrl = String.format("/search?q=artist:\"%s\" track:\"%s\"", encodeURIComponent(artist), encodeURIComponent(songTitle));

        return this.webClient.get()
                .uri(apiUrl)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    String songUrl = extractSongUrl(response);
                    return Mono.just(songUrl);
                })
                .onErrorResume(e -> Mono.error(new RuntimeException("Error fetching song. Please try again later.")));
    }

    private String extractSongUrl(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray dataArray = jsonObject.getJSONArray("data");

        if (dataArray.length() > 0) {
            JSONObject firstResult = dataArray.getJSONObject(0);
            return firstResult.getString("preview");
        }

        throw new RuntimeException("No song found.");
    }

    public Mono<List<Map<String, String>>> findMultipleSongsOnDeezer(List<Map<String, String>> songs) {
        Flux<Map<String, String>> songFlux = Flux.fromIterable(songs)
                .flatMap(this::fetchSongData)
                .filter(songData -> !songData.get("preview").isEmpty() && !songData.get("cover").isEmpty()); // Filtra canciones incompletas

        return songFlux.collectList();
    }

    private Mono<Map<String, String>> fetchSongData(Map<String, String> song) {
        String songTitle = song.get("title");
        String artistName = song.get("artist");

        String apiUrl = String.format("/search?q=artist:\"%s\" track:\"%s\"", encodeURIComponent(artistName), encodeURIComponent(songTitle));

        return this.webClient.get()
                .uri(apiUrl)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> extractSongData(response, songTitle, artistName))
                .onErrorResume(e -> Mono.just(Map.of(
                        "title", songTitle,
                        "artist", artistName,
                        "preview", "",
                        "cover", ""
                ))); // Devuelve un mapa básico en caso de error
    }

    private Map<String, String> extractSongData(String jsonResponse, String songTitle, String artistName) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray dataArray = jsonObject.getJSONArray("data");

        if (dataArray.length() > 0) {
            JSONObject firstResult = dataArray.getJSONObject(0);
            String previewUrl = firstResult.optString("preview", "");
            String coverUrl = firstResult.optJSONObject("album").optString("cover_medium", "");

            return Map.of(
                    "title", songTitle,
                    "artist", artistName,
                    "preview", previewUrl,
                    "cover", coverUrl
            );
        }

        // Si no se encuentra la canción, devuelve el título original con valores vacíos para los demás campos
        return Map.of(
                "title", songTitle,
                "artist", artistName,
                "preview", "",
                "cover", ""
        );
    }}
