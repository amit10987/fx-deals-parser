package com.progresssoft.service;

import org.springframework.web.multipart.MultipartFile;

import com.progresssoft.exception.FileAlreadExistException;
import com.progresssoft.exception.FxDealDuplicateKeyException;

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
	 * @throws FxDealDuplicateKeyException 
	 */
	public String processFxDealsFile(MultipartFile file) throws InterruptedException, FileAlreadExistException, FxDealDuplicateKeyException;
	
}
