package com.progresssoft.controller;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.progresssoft.constant.FxDealsConstant;
import com.progresssoft.exception.FxFileAlreadExistException;
import com.progresssoft.exception.FxFileNotFoundException;
import com.progresssoft.exception.FxDealDuplicateKeyException;
import com.progresssoft.model.FxDealsUploadModel;
import com.progresssoft.service.FileParser;

/**
 * @author amit
 *
 */
@Controller
@RequestMapping(FxDealsConstant.FX_DEALS_PARSER)
public class FxDealsController {
	
	private static final Logger LOGGER = Logger.getLogger(FxDealsController.class);
	
	@Autowired
	FileParser fileParser;
	
	/**
	 * @param file
	 * @param model
	 * @return
	 * @throws InterruptedException 
	 * @throws FxFileAlreadExistException 
	 * @throws FxDealDuplicateKeyException 
	 * @throws FxFileNotFoundException 
	 */
	@PostMapping(FxDealsConstant.UPLOAD)
	public String handleFileUpload(@RequestParam("csvFile") MultipartFile file, Model model) throws InterruptedException, FxFileAlreadExistException, FxDealDuplicateKeyException, FxFileNotFoundException {
		LOGGER.debug("handleFileUpload method in FxDealsController started");
		FxDealsUploadModel dealsModel = new  FxDealsUploadModel();
		if(file.isEmpty()){
			throw new FxFileNotFoundException();
		}
		long startTime = System.currentTimeMillis();
		String successMsg = fileParser.processFxDealsFile(file);
		long endTime = System.currentTimeMillis();
		dealsModel.setUploadMsg(successMsg + "in " + (endTime - startTime)/1000 +" Sec");
		model.addAttribute("msg", dealsModel);
		LOGGER.debug("handleFileUpload method in FxDealsController End");
		return FxDealsConstant.FX_DELAS_FILE_UPLOAD_FORM;
	}
	
	/**
	 * @return
	 * @throws IOException
	 */
	@GetMapping(FxDealsConstant.GET_UPLOAD_FORM)
    public String getUploadForm(Model model) throws IOException {
		FxDealsUploadModel dealsModel = new  FxDealsUploadModel();
		model.addAttribute("msg", dealsModel);
        return FxDealsConstant.FX_DELAS_FILE_UPLOAD_FORM;
    }
	
	/**
	 * @param ex
	 * @param model
	 * @return
	 */
	@ExceptionHandler(FxFileAlreadExistException.class)
	public String  handleFileAlreadyExistException(FxFileAlreadExistException ex, Model model){
		FxDealsUploadModel dealsModel = new  FxDealsUploadModel();
		dealsModel.setErrorMsg("File with same name already exist.");
		model.addAttribute("msg", dealsModel);
		return FxDealsConstant.FX_DELAS_FILE_UPLOAD_FORM;
	}
	
	/**
	 * @param ex
	 * @param model
	 * @return
	 */
	@ExceptionHandler(FxDealDuplicateKeyException.class)
	public String  handleFxDealDuplicateKeyException(FxDealDuplicateKeyException ex, Model model){
		FxDealsUploadModel dealsModel = new  FxDealsUploadModel();
		dealsModel.setErrorMsg(ex.getMessage());
		model.addAttribute("msg", dealsModel);
		return FxDealsConstant.FX_DELAS_FILE_UPLOAD_FORM;
	}
	
	/**
	 * @param ex
	 * @param model
	 * @return
	 */
	@ExceptionHandler(FxFileNotFoundException.class)
	public String  handleFileNotFoundException(FxFileNotFoundException ex, Model model){
		FxDealsUploadModel dealsModel = new  FxDealsUploadModel();
		dealsModel.setErrorMsg("File is empty. Please select a file.");
		model.addAttribute("msg", dealsModel);
		return FxDealsConstant.FX_DELAS_FILE_UPLOAD_FORM;
	}
}