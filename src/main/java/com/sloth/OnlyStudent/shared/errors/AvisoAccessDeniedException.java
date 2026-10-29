package com.sloth.OnlyStudent.shared.errors;

public class AvisoAccessDeniedException extends RuntimeException {

    public AvisoAccessDeniedException() {
        super("Acesso negado.");
    }
}
