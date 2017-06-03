package com.progresssoft.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.progesssoft.constant.FxDealsConstant;
import com.progresssoft.domain.FxDealsValid;
import com.progresssoft.exception.FileAlreadExistException;
import com.progresssoft.exception.FxDealDuplicateKeyException;
import com.progresssoft.repository.DealsCountRepository;
import com.progresssoft.repository.FxDealsInvalidRepository;
import com.progresssoft.repository.FxDealsValidRepository;

@RunWith(MockitoJUnitRunner.class)
public class FxDealsFileParserTest {
	
	@Mock
	FxDealsValidRepository fxDealsValidRepo;

	@Mock
	FxDealsInvalidRepository fxDealsInvalidRepo;

	@Mock
	DealsCountRepository dealsCountRepository;
	
	@InjectMocks
	FxDealsFileParser fxDealsService;
	
	MultipartFile file;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Before
	public void loadFile(){
		Path path = Paths.get("src/test/resources/fxDeals.csv");
		String name = "fxDeals.csv";
		String originalFileName = "fxDeals.csv";
		String contentType = "application/vnd.ms-excel";
		byte[] content = null;
		try {
		    content = Files.readAllBytes(path);
		} catch (final IOException e) {
		}
		file = new MockMultipartFile(name, originalFileName, contentType, content);
	}
	
	/**
	 * File processed successfully
	 * 
	 * @throws InterruptedException
	 * @throws FileAlreadExistException
	 * @throws FxDealDuplicateKeyException
	 */
	@Test
	public void processFxDealsFileTest() throws InterruptedException, FileAlreadExistException, FxDealDuplicateKeyException{
		String result = fxDealsService.processFxDealsFile(file);
		Assert.assertTrue(result.equals(FxDealsConstant.FILE_UPLOAD_SUCCESS_MSG));
	}
	
	/**
	 * 
	 *  Test Scenerio--- if file already exist in DB, Should throw FIleAlreadyExistException
	 *  
	 *  
	 *  
	 * @throws InterruptedException
	 * @throws FileAlreadExistException
	 * @throws FxDealDuplicateKeyException
	 */
	@Test(expected = FileAlreadExistException.class)
	public void processFxDealsFile_throwsFileAlreadyExistException_Test() throws InterruptedException, FileAlreadExistException, FxDealDuplicateKeyException{
		Mockito.when(fxDealsValidRepo.findFirstByFileName(Matchers.anyString())).thenReturn(new FxDealsValid());
		fxDealsService.processFxDealsFile(file);
		
	}
}
