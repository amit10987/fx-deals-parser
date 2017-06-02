package com.progresssoft.service;

import org.springframework.web.multipart.MultipartFile;

import com.progresssoft.exception.FileAlreadExistException;

/**
 * @author amit
 *
 */
public interface FileParser {

	/**
	 * @param file
	 * @return
	 * @throws InterruptedException 
	 * @throws FileAlreadExistException 
	 */
	public String processFxDealsFile(MultipartFile file) throws InterruptedException, FileAlreadExistException;
	
}
