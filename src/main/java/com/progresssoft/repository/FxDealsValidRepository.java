package com.progresssoft.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.progresssoft.domain.FxDealsValid;

public interface FxDealsValidRepository  extends MongoRepository<FxDealsValid, String>{

	FxDealsValid findFirstByFileName(String fileName);
}
