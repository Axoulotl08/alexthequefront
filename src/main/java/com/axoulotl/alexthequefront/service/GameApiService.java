package com.axoulotl.alexthequefront.service;

import com.axoulotl.alexthequefront.entity.in.GameClientDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class GameApiService {

    private final WebClient webClient;

    public GameApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<GameClientDTO> getAllGames() {
        return webClient.get()
                .uri("/game")
                .retrieve()
                .bodyToFlux(GameClientDTO.class);
    }

    public Mono<GameClientDTO> addGame(GameClientDTO gameDTO) {
        return webClient.post()
                .uri("/game")
                .bodyValue(gameDTO)
                .retrieve()
                .bodyToMono(GameClientDTO.class);
    }


}