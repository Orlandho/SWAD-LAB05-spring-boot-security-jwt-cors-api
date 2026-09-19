package com.ejemplo.laboratorio05.dto;

public record AuthResponse(
        String token,
        String tipo,
        long expiraEnSegundos
) {
}
