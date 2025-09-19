package com.axoulotl.alexthequefront.service.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

public class GameTimeConverter implements Converter<String, Long> {


    @Override
    public Result<Long> convertToModel(String presentationValue, ValueContext valueContext) {
        if (presentationValue == null || presentationValue.trim().isEmpty()) {
            return Result.ok(null); // Gère les champs vides comme null
        }
        String trimmedValue = presentationValue.trim();

        if (!trimmedValue.matches("^\\d+:\\d{2}$")) { // Regex: at least one digit, then ':', then exactly two digits
            return Result.error("Format must be HH:mm (e.g., 01:30 or 5:00)");
        }
        try {
            String[] parts = presentationValue.split(":");
            if (parts.length != 2) {
                return Result.error("Format must be HH:mm");
            }
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            if (hours < 0 || minutes < 0 || minutes >= 60) {
                return Result.error("Invalid time values");
            }
            // Convertit en minutes totales
            return Result.ok((long) (hours * 60L + minutes));
        } catch (NumberFormatException e) {
            return Result.error("Invalid number format. Use HH:mm");
        } catch (Exception e) {
            return Result.error("Error parsing time: " + e.getMessage());
        }
    }

    @Override
    public String convertToPresentation(Long modelValue, ValueContext valueContext) {
        if (modelValue == null) {
            return "";
        }

        long totalMinutes = modelValue;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes); // Formate en HH:mm (avec zéro si <10)
    }
}
