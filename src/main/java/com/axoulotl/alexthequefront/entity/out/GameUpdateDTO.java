package com.axoulotl.alexthequefront.entity.out;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameUpdateDTO{
    private LocalDate startDate;
    private LocalDate endDate;
    private Long gameTime;
}
