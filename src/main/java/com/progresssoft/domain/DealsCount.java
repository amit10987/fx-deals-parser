package com.progresssoft.domain;

import java.math.BigInteger;

import org.springframework.data.annotation.Id;

public class DealsCount{

	@Id
	private BigInteger id;
	private String orderCurrency;
	private Long countOfDeals;
	
	public String getOrderCurrency() {
		return orderCurrency;
	}
	public void setOrderCurrency(String orderCurrency) {
		this.orderCurrency = orderCurrency;
	}
	public Long getCountOfDeals() {
		return countOfDeals;
	}
	public void setCountOfDeals(Long countOfDeals) {
		this.countOfDeals = countOfDeals;
	}
	public BigInteger getId() {
		return id;
	}
	public void setId(BigInteger id) {
		this.id = id;
	}
}