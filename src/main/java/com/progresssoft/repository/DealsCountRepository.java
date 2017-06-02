package com.progresssoft.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.progresssoft.domain.DealsCount;

/**
 * @author amit
 *
 */
public interface DealsCountRepository  extends MongoRepository<DealsCount, Long> {

}
