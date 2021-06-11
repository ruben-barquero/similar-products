package com.rbb.similarproducts.exceptions;

public class NotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public NotFoundException(final String errorMessage, final Throwable cause) {
		super(errorMessage, cause);
	}
	
}
