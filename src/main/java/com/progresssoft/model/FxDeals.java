package com.progresssoft.model;

public class FxDeals {
	
	private String id;
	private String fromCurrency;
	private String orderCurrency;
	private String toCurrency;
	private String dealTime;
	private String amount;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public FxDeals(String id, String fromCurrency, String orderCurrency,
			String toCurrency, String dealTime, String amount) {
		super();
		this.id = id;
		this.fromCurrency = fromCurrency;
		this.orderCurrency = orderCurrency;
		this.toCurrency = toCurrency;
		this.dealTime = dealTime;
		this.amount = amount;
	}
	public String getFromCurrency() {
		return fromCurrency;
	}
	public void setFromCurrency(String fromCurrency) {
		this.fromCurrency = fromCurrency;
	}
	public String getOrderCurrency() {
		return orderCurrency;
	}
	public void setOrderCurrency(String orderCurrency) {
		this.orderCurrency = orderCurrency;
	}
	public String getToCurrency() {
		return toCurrency;
	}
	public void setToCurrency(String toCurrency) {
		this.toCurrency = toCurrency;
	}
	public String getDealTime() {
		return dealTime;
	}
	public void setDealTime(String dealTime) {
		this.dealTime = dealTime;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	
}
