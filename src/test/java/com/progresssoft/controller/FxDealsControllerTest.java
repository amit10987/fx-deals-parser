package com.progresssoft.controller;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.progesssoft.constant.FxDealsConstant;
import com.progresssoft.exception.FileAlreadExistException;
import com.progresssoft.exception.FxDealDuplicateKeyException;
import com.progresssoft.service.FileParser;

/**
 * @author amit
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class FxDealsControllerTest {

	@Mock
	FileParser fileParser;

	@InjectMocks
	FxDealsController controller;

	@Mock
	Model model;
	
	@Mock
	MultipartFile file;
	
	@Mock
	FileAlreadExistException fileAlreadyExistEx;
	
	@Mock
	FxDealDuplicateKeyException duplicateKeyEx;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getUploadFormTest() throws IOException {
		String view = controller.getUploadForm(model);
		Assert.assertEquals(view, FxDealsConstant.FX_DELAS_FILE_UPLOAD_FORM);
	}
	
	@Test
	public void handleFileUploadTest() throws InterruptedException, FileAlreadExistException, FxDealDuplicateKeyException  {
		String view = controller.handleFileUpload(file, model);
		Mockito.when(fileParser.processFxDealsFile(file)).thenReturn(FxDealsConstant.FILE_UPLOAD_SUCCESS_MSG);
		Assert.assertEquals(view, FxDealsConstant.FX_DELAS_FILE_UPLOAD_FORM);
	}
	
	@Test
	public void handleFileAlreadyExistExceptionTest() throws InterruptedException, FileAlreadExistException, FxDealDuplicateKeyException {
		String view = controller.handleFileAlreadyExistException(fileAlreadyExistEx, model);
		Assert.assertEquals(view, FxDealsConstant.FX_DELAS_FILE_UPLOAD_FORM);
	}
	
	@Test
	public void handleFxDealDuplicateKeyExceptionTest() throws InterruptedException, FileAlreadExistException, FxDealDuplicateKeyException {
		String view = controller.handleFxDealDuplicateKeyException(duplicateKeyEx, model);
		Assert.assertEquals(view, FxDealsConstant.FX_DELAS_FILE_UPLOAD_FORM);
	}
}