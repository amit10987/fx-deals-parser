package com.progresssoft.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;




import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;




import com.progresssoft.model.FxDeals;

@Controller
@RequestMapping("fxdealsparser")
public class FxDealsController {

	@PostMapping("/upload")
	public String handleFileUpload(@RequestParam("csvFile") MultipartFile file, RedirectAttributes redirectAttributes) {
		System.out.println(file);
		try(BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))){
			List<FxDeals> strs = br.lines().skip(1).map(str->{
				StringTokenizer token = new StringTokenizer(str, ",");
				return new FxDeals(token.nextToken(), token.nextToken(), token.nextToken(), token.nextToken(), token.nextToken(), token.nextToken());
			}).collect(Collectors.toList());
			System.out.println(strs);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "redirect:/fxdealsparser/";
	}
	
	@GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {
        return "fxDealsFileUploadForm";
    }

}