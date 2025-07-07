package com.axoulotl.alexthequefront.entity.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsoleDTO {
    private String name;
    private String manufacturer;
    private LocalDateTime launchDate;
    private Integer zone;
}
