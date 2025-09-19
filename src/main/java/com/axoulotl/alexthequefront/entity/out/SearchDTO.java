package com.axoulotl.alexthequefront.entity.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchDTO {
    private Integer consoleId;
    private String name;
    private LocalDate startedDate;
    private Integer statusId;
}
