package com.axoulotl.alexthequefront.entity.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameDTO {
    private String name;
    private Integer console;
    private Boolean inbox;
}
