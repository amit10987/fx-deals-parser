package com.progresssoft.service;

import java.util.StringTokenizer;

import com.progresssoft.model.FxDeals;

/**
 * @author amit
 *
 */
public class FxDealsFileDataValidator {

	/**
	 * To populate FxDeals object from stringtokenizer,
	 * this method responsible for validating the fxDeals object field 
	 * 
	 * @param token
	 * @param fileName
	 * @return
	 */
	public static FxDeals validateAndGetFxDeals(StringTokenizer token, String fileName){
		FxDeals fxDeals = new FxDeals();
		fxDeals.setValid(true);
		fxDeals.setFileName(fileName);
		setAndValidateId(token, fxDeals);
		setAndValidateFromCurrency(token, fxDeals);
		setAndValidateOrderCurrency(token, fxDeals);
		setAndValidateToCurrency(token, fxDeals);
		setAndValidateDealTime(token, fxDeals);
		setAndValidateAmount(token, fxDeals);
		return fxDeals;
	}
	
	/**
	 * @param token
	 * @param fxDeals
	 */
	private static void setAndValidateAmount(StringTokenizer token, FxDeals fxDeals) {
		if (token.hasMoreTokens()) {
			fxDeals.setAmount(token.nextToken());
		} else {
			fxDeals.setValid(false);
		}
	}

	/**
	 * @param token
	 * @param fxDeals
	 */
	private static void setAndValidateDealTime(StringTokenizer token, FxDeals fxDeals) {
		if (token.hasMoreTokens()) {
			fxDeals.setDealTime(token.nextToken());
		} else {
			fxDeals.setValid(false);
		}
	}

	/**
	 * @param token
	 * @param fxDeals
	 */
	private static void setAndValidateToCurrency(StringTokenizer token, FxDeals fxDeals) {
		if (token.hasMoreTokens()) {
			fxDeals.setToCurrency(token.nextToken());
		} else {
			fxDeals.setValid(false);
		}
	}

	/**
	 * @param token
	 * @param fxDeals
	 */
	private static void setAndValidateOrderCurrency(StringTokenizer token, FxDeals fxDeals) {
		if (token.hasMoreTokens()) {
			fxDeals.setOrderCurrency(token.nextToken());
		} else {
			fxDeals.setValid(false);
		}
	}

	/**
	 * @param token
	 * @param fxDeals
	 */
	private static void setAndValidateFromCurrency(StringTokenizer token, FxDeals fxDeals) {
		if (token.hasMoreTokens()) {
			fxDeals.setFromCurrency(token.nextToken());
		} else {
			fxDeals.setValid(false);
		}
	}

	/**
	 * @param token
	 * @param fxDeals
	 */
	private static void setAndValidateId(StringTokenizer token, FxDeals fxDeals) {
		if (token.hasMoreTokens()) {
			fxDeals.setId(token.nextToken());
		} else {
			fxDeals.setValid(false);
		}
	}
	
}
