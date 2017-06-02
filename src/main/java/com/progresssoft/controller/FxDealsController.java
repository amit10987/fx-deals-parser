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

import com.progesssoft.constant.FxDealsConstant;
import com.progresssoft.exception.FileAlreadExistException;
import com.progresssoft.service.FileParser;

/**
 * @author s730732
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
	 * @throws FileAlreadExistException 
	 */
	@PostMapping(FxDealsConstant.UPLOAD)
	public String handleFileUpload(@RequestParam("csvFile") MultipartFile file, Model model) throws InterruptedException, FileAlreadExistException {
		long startTime = System.currentTimeMillis();
		String successMsg = fileParser.processFxDealsFile(file);
		long endTime = System.currentTimeMillis();
		model.addAttribute(FxDealsConstant.FILE_UPLOAD_SUCCESS_CODE, successMsg + "in " + (endTime - startTime)/1000 +" Sec");
		return FxDealsConstant.FX_DELAS_FILE_UPLOAD_FORM;
	}
	
	/**
	 * @return
	 * @throws IOException
	 */
	@GetMapping(FxDealsConstant.GET_UPLOAD_FORM)
    public String getUploadForm() throws IOException {
        return FxDealsConstant.FX_DELAS_FILE_UPLOAD_FORM;
    }
	
	@ExceptionHandler(FileAlreadExistException.class)
	public String  handleFileAlreadyExistException(FileAlreadExistException ex, Model model){
		model.addAttribute("errorMsg", "File with same name already exist.");
		return FxDealsConstant.FX_DELAS_FILE_UPLOAD_FORM;
	}

}