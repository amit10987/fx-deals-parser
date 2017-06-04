package com.progresssoft.exception;

/**
 * @author amit
 *
 */
@SuppressWarnings("serial")
public class FxFileNotFoundException extends Exception {
	
	private String code;
	private String message;
	
	public FxFileNotFoundException(){
		super();
	}
	
	public FxFileNotFoundException(String code, String message) {
		super(message);
		this.code = code;
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
