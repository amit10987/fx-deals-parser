package com.progresssoft.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author amit
 *
 */
public interface FileParser {

	/**
	 * @param file
	 * @return
	 */
	public String processFxDealsFile(MultipartFile file);
	
}
