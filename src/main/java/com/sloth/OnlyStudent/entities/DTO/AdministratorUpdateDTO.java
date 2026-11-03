package com.sloth.OnlyStudent.entities.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdministratorUpdateDTO(
        @NotBlank(message = "Nome é obrigatório.") String name,
        @NotBlank(message = "Email é obrigatório.") @Email(message = "Email inválido.") String email,
        @NotBlank(message = "Telefone é obrigatório.") String telephone,
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.") String password) {
}