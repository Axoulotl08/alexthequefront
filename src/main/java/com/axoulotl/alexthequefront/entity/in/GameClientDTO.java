package com.axoulotl.alexthequefront.entity.in;

import com.axoulotl.alexthequefront.entity.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameClientDTO {
    private Integer id;
    private String name;
    private ConsoleClientDTO console;
    private Boolean inbox;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long gameTime;
    private Status status;
}
