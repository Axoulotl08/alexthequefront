package com.axoulotl.alexthequefront.service;

import com.axoulotl.alexthequefront.entity.in.PaginatedGamesDTO;
import com.axoulotl.alexthequefront.entity.out.SearchDTO;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class SearchApiService {

    private final WebClient webClient;

    public SearchApiService(WebClient webClient){
        this.webClient = webClient;
    }

    public Mono<PaginatedGamesDTO> getGameFromSearch(SearchDTO searchDTO, int page, int size){
        return webClient.post()
                .uri("/search/game?page={page}&size={size}", page, size)
                .bodyValue(searchDTO)
                .retrieve()
                .bodyToMono(PaginatedGamesDTO.class);
    }
}

