package com.sloth.OnlyStudent.entities.DTO;

import java.time.LocalDateTime;

import com.sloth.OnlyStudent.entities.Custo;
import com.sloth.OnlyStudent.entities.Status;

public record ClassroomsDTO(Long codigo, String name, String educator, Status status, Custo custo, double price,
        int totalAlunos, int totalMateriais, LocalDateTime dataExclusaoAgendada) {

    public ClassroomsDTO(Long codigo, String name, String educator, Status status, Custo custo, double price,
            int totalAlunos, int totalMateriais) {
        this(codigo, name, educator, status, custo, price, totalAlunos, totalMateriais, null);
    }
}
