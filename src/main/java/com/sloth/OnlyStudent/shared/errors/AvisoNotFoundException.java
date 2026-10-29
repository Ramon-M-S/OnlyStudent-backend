package com.sloth.OnlyStudent.shared.errors;

public class AvisoNotFoundException extends RuntimeException {

    public AvisoNotFoundException() {
        super("Aviso não encontrado.");
    }
}
