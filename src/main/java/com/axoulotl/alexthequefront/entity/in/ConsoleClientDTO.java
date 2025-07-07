package com.axoulotl.alexthequefront.entity.in;

import com.axoulotl.alexthequefront.entity.enums.Zone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsoleClientDTO {
    private Integer id;
    private String name;
    private LocalDateTime launchDate;
    private String manufacturer;
    private Zone zone;
    private LocalDateTime creationDate;
}