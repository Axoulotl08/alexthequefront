package com.axoulotl.alexthequefront.service;

import com.axoulotl.alexthequefront.entity.in.GameClientDTO;
import com.axoulotl.alexthequefront.entity.in.PaginatedGamesDTO;
import com.axoulotl.alexthequefront.entity.out.GameDTO;
import com.axoulotl.alexthequefront.entity.out.GameUpdateDTO;
import com.axoulotl.alexthequefront.error.AlexthequeError;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
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

    public Mono<GameDTO> addGame(GameDTO gameDTO) {
        return webClient.post()
                .uri("/game")
                .bodyValue(gameDTO)
                .retrieve()
                .bodyToMono(GameDTO.class);
    }

    public Mono<GameClientDTO> getGame(Integer id){
        return webClient.get()
                .uri("/game/{id}", id)
                .retrieve()
                .bodyToMono(GameClientDTO.class);
    }

    public Mono<GameClientDTO> updateDate(GameUpdateDTO gameUpdateDTO, Integer id){
        return webClient.patch()
                .uri("/game/{id}", id)
                .bodyValue(gameUpdateDTO)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.bodyToMono(AlexthequeError.class)
                        .flatMap(error -> Mono.error(new RuntimeException(error.getMessage()))))
                .bodyToMono(GameClientDTO.class);
    }

}