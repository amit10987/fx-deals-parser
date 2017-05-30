package com.progresssoft.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.progesssoft.constant.FxDealsConstant;
import com.progresssoft.model.FxDeals;

/**
 * @author amit kumar
 *
 */
@Component
public class FxDealsFileParser implements FileParser{

	@Override
	public String processFxDealsFile(MultipartFile file) {
		List<FxDeals> fxDeals = getFxDealsModelFromFileStream(file);
		return null;
	}

	private List<FxDeals> getFxDealsModelFromFileStream(MultipartFile file) {
		try(BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))){
			return br.lines().skip(1).map(str->{
				StringTokenizer token = new StringTokenizer(str, FxDealsConstant.COMMA_DELIMITER);
				return getFxDeals(token);
			}).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	private FxDeals getFxDeals(StringTokenizer token) {
		FxDeals fxDeals = new FxDeals();
		fxDeals.setValid(true);
		setAndValidateId(token, fxDeals);
		setAndValidateFromCurrency(token, fxDeals);
		fxDeals.setFromCurrency(token.nextToken());
		fxDeals.setOrderCurrency(token.nextToken());
		fxDeals.setToCurrency(token.nextToken());
		fxDeals.setDealTime(token.nextToken());
		fxDeals.setAmount(token.nextToken());
		return null;
	}

	private void setAndValidateFromCurrency(StringTokenizer token,
			FxDeals fxDeals) {
		if (token.hasMoreTokens()) {
			fxDeals.setId(token.nextToken());
			if(fxDeals.isValid()){
				//validate currency
			}
		} else {
			fxDeals.setValid(false);
		}
	}

	private void setAndValidateId(StringTokenizer token, FxDeals fxDeals) {
		if (token.hasMoreTokens()) {
			fxDeals.setId(token.nextToken());
		} else {
			fxDeals.setValid(false);
		}
	}

}
