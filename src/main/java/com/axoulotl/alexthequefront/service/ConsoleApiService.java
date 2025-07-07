package com.axoulotl.alexthequefront.service;

import com.axoulotl.alexthequefront.entity.in.ConsoleClientDTO;
import com.axoulotl.alexthequefront.entity.out.ConsoleDTO;
import reactor.core.publisher.Flux;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ConsoleApiService {

    private final WebClient webClient;

    public ConsoleApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<ConsoleClientDTO> getAllConsoles() {
        return webClient.get()
                .uri("/console")
                .retrieve()
                .bodyToFlux(ConsoleClientDTO.class);
    }

    public Mono<ConsoleClientDTO> addConsole(ConsoleDTO consoleDTO) {
        return webClient.post()
                .uri("/console")
                .bodyValue(consoleDTO)
                .retrieve()
                .bodyToMono(ConsoleClientDTO.class);
    }
}