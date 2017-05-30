package com.progresssoft.controller;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.progesssoft.constant.FxDealsConstant;
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
	 */
	@PostMapping(FxDealsConstant.UPLOAD)
	public String handleFileUpload(@RequestParam("csvFile") MultipartFile file, Model model) {
		String successMsg = fileParser.processFxDealsFile(file);
		model.addAttribute(FxDealsConstant.FILE_UPLOAD_SUCCESS_CODE, successMsg);
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

}