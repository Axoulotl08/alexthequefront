package com.axoulotl.alexthequefront.service;

import com.axoulotl.alexthequefront.entity.in.GameClientDTO;
import com.axoulotl.alexthequefront.entity.in.PaginatedGamesDTO;
import com.axoulotl.alexthequefront.entity.out.GameDTO;
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

    public Mono<PaginatedGamesDTO> getAllGames(int page, int size) {
        return webClient.get()
                .uri("/game?page={page}&size={size}", page, size)
                .retrieve()
                .bodyToMono(PaginatedGamesDTO.class);
    }

    public Mono<GameClientDTO> addGame(GameDTO gameDTO) {
        return webClient.post()
                .uri("/game")
                .bodyValue(gameDTO)
                .retrieve()
                .bodyToMono(GameClientDTO.class);
    }


}