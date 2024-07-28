package com.robedev.photosong_backend.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
                    // Procesar la respuesta JSON y extraer la URL de la canción
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
            return firstResult.getString("preview"); // Aquí asumimos que la URL de la canción está en el campo "link"
        }

        throw new RuntimeException("No song found.");
    }
}
