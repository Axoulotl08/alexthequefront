package com.axoulotl.alexthequefront.entity.in;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedGamesDTO {
    private Long nbGames;
    private Integer totalPages;
    private Integer currentPage;
    private List<GameClientDTO> games;
}
