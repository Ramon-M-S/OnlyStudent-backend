package com.sloth.OnlyStudent.entities.DTO;

import jakarta.validation.constraints.NotBlank;

public record AvisoRequestDTO(@NotBlank(message = "A mensagem não pode estar vazia.") String mensagem) {
}
