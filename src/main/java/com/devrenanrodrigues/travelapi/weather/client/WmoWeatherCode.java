package com.devrenanrodrigues.travelapi.weather.client;

public final class WmoWeatherCode {

    private WmoWeatherCode() {
    }

    public static String getDescription(Integer code) {
        if (code == null) {
            return "Tempo estável";
        }
        return switch (code) {
            case 0 -> "Céu limpo";
            case 1 -> "Principalmente limpo";
            case 2 -> "Parcialmente nublado";
            case 3 -> "Nublado";
            case 45, 48 -> "Nevoeiro";
            case 51 -> "Chuvisco leve";
            case 53 -> "Chuvisco moderado";
            case 55 -> "Chuvisco denso";
            case 56, 57 -> "Chuvisco congelante";
            case 61 -> "Chuva fraca";
            case 63 -> "Chuva moderada";
            case 65 -> "Chuva forte";
            case 66, 67 -> "Chuva congelante";
            case 71 -> "Neve fraca";
            case 73 -> "Neve moderada";
            case 75 -> "Neve forte";
            case 77 -> "Grãos de neve";
            case 80 -> "Pancadas de chuva leves";
            case 81 -> "Pancadas de chuva";
            case 82 -> "Pancadas de chuva fortes";
            case 85, 86 -> "Pancadas de neve";
            case 95 -> "Tempestade";
            case 96, 99 -> "Tempestade com granizo";
            default -> "Tempo variável";
        };
    }
}
