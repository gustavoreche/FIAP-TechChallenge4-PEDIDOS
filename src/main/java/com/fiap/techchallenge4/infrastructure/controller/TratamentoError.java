package com.fiap.techchallenge4.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratamentoError {

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(IllegalArgumentException.class)
	public String trataParametroInvalido(IllegalArgumentException ex) {
		return ex.getMessage();
	}

}
