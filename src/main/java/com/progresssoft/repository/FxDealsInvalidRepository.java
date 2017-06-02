package com.progresssoft.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.progresssoft.domain.FxDealsInvalid;

public interface FxDealsInvalidRepository  extends MongoRepository<FxDealsInvalid, String>{

}
