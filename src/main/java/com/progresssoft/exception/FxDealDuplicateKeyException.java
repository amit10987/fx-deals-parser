package com.progresssoft.exception;

public class FxDealDuplicateKeyException extends Exception {
	
	private String code;
	private String message;
	
	public FxDealDuplicateKeyException(){
		super();
	}
	
	public FxDealDuplicateKeyException(String code, String message) {
		super(message);
		this.code = code;
		this.message = message;
	}
	
	public FxDealDuplicateKeyException(String message) {
		super(message);
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
