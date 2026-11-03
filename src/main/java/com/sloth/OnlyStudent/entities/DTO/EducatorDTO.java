package com.sloth.OnlyStudent.entities.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record EducatorDTO(
		String name,
		@JsonProperty("telephone") @JsonAlias("telefone") String telephone,
		String login,
		String especialidade,
		String password) {

	public EducatorDTO(String name, String telephone, String login, String especialidade) {
		this(name, telephone, login, especialidade, null);
	}
}
