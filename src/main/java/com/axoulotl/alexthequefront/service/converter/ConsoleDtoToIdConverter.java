package com.axoulotl.alexthequefront.service.converter;

import com.axoulotl.alexthequefront.entity.in.ConsoleClientDTO;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

import java.util.List;

public class ConsoleDtoToIdConverter implements Converter<ConsoleClientDTO, Integer> {
    private final List<ConsoleClientDTO> availableConsoles;

    public ConsoleDtoToIdConverter(List<ConsoleClientDTO> availableConsoles) {
        this.availableConsoles = availableConsoles;
    }


    @Override
    public Result<Integer> convertToModel(ConsoleClientDTO fieldValue, ValueContext context) {
        return Result.ok(fieldValue != null ? fieldValue.getId() : null);
    }

    @Override
    public ConsoleClientDTO convertToPresentation(Integer modelValue, ValueContext context) {
        if (modelValue == null) return null;
        return availableConsoles.stream()
                .filter(c -> c.getId().equals(modelValue))
                .findFirst()
                .orElse(null);
    }
}