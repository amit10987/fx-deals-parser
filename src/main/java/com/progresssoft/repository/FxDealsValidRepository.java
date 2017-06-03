package com.progresssoft.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.progresssoft.domain.FxDealsValid;

/**
 * @author amit
 *
 */
public interface FxDealsValidRepository  extends MongoRepository<FxDealsValid, String>{

	/**
	 * @param fileName
	 * @return
	 */
	FxDealsValid findFirstByFileName(String fileName);
}
