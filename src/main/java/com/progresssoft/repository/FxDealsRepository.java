package com.progresssoft.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.progresssoft.model.FxDeals;

public interface FxDealsRepository  extends MongoRepository<FxDeals, String>{

}
