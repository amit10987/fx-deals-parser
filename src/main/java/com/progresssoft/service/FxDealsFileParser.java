package com.progresssoft.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.objenesis.instantiator.sun.MagicInstantiator;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.progesssoft.constant.FxDealsConstant;
import com.progresssoft.controller.FxDealsController;
import com.progresssoft.model.FxDeals;
import com.progresssoft.repository.FxDealsRepository;

/**
 * @author amit kumar
 *
 */
@Component
public class FxDealsFileParser implements FileParser {

	@Autowired
	FxDealsRepository fxRepo;

	private static final int PARTITION_SIZE = 10000;

	private static final Logger LOGGER = Logger.getLogger(FxDealsFileParser.class);

	
	@Override
	public String processFxDealsFile(MultipartFile file) {
		long parsingStartTime = System.currentTimeMillis();
		Map<Boolean, List<FxDeals>> fxDealsMap = getFxDealsModelFromFileStream(file);
		List<FxDeals> validDeals = fxDealsMap.get(true);
		List<FxDeals> inValidDeals = fxDealsMap.get(false);
		long parsingEndTime = System.currentTimeMillis();
		LOGGER.info("parsing time" + (parsingEndTime - parsingStartTime));
		long saveStartTime = System.currentTimeMillis();
		
		int noOfPartition = validDeals.size() % PARTITION_SIZE == 0 ? validDeals.size() / PARTITION_SIZE : (validDeals.size() / PARTITION_SIZE) + 1;
		ExecutorService executor = Executors.newFixedThreadPool(noOfPartition);
		CountDownLatch latch = new CountDownLatch(noOfPartition);
		IntStream.range(0, noOfPartition).forEach(index -> {
			executor.submit(() -> {
				List<FxDeals> fxDealsPartition = validDeals.subList(index * PARTITION_SIZE, Math.min((index * PARTITION_SIZE) + PARTITION_SIZE, validDeals.size()));
				fxRepo.save(fxDealsPartition);
				latch.countDown();
			});
			
		});
		executor.shutdown();
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long saveEndTime = System.currentTimeMillis();
		LOGGER.info("save time" + (saveEndTime - saveStartTime));
		return FxDealsConstant.FILE_UPLOAD_SUCCESS_MSG;
	}

	private Map<Boolean, List<FxDeals>> getFxDealsModelFromFileStream(MultipartFile file) {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
			return br.lines().skip(1).map(str -> {
				StringTokenizer token = new StringTokenizer(str, FxDealsConstant.COMMA_DELIMITER);
				return getFxDeals(token);
			}).collect(Collectors.partitioningBy(FxDeals::isValid));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyMap();
	}

	private FxDeals getFxDeals(StringTokenizer token) {
		FxDeals fxDeals = new FxDeals();
		fxDeals.setValid(true);
		setAndValidateId(token, fxDeals);
		setAndValidateFromCurrency(token, fxDeals);
		setAndValidateOrderCurrency(token, fxDeals);
		setAndValidateToCurrency(token, fxDeals);
		setAndValidateDealTime(token, fxDeals);
		setAndValidateAmount(token, fxDeals);
		return fxDeals;
	}

	private void setAndValidateAmount(StringTokenizer token, FxDeals fxDeals) {
		if (token.hasMoreTokens()) {
			fxDeals.setAmount(token.nextToken());
			if (fxDeals.isValid()) {
				// validate currency
			}
		} else {
			fxDeals.setValid(false);
		}
	}

	private void setAndValidateDealTime(StringTokenizer token, FxDeals fxDeals) {
		if (token.hasMoreTokens()) {
			fxDeals.setDealTime(token.nextToken());
			if (fxDeals.isValid()) {
				// validate currency
			}
		} else {
			fxDeals.setValid(false);
		}
	}

	private void setAndValidateToCurrency(StringTokenizer token, FxDeals fxDeals) {
		if (token.hasMoreTokens()) {
			fxDeals.setToCurrency(token.nextToken());
			if (fxDeals.isValid()) {
				// validate currency
			}
		} else {
			fxDeals.setValid(false);
		}

	}

	private void setAndValidateOrderCurrency(StringTokenizer token, FxDeals fxDeals) {
		if (token.hasMoreTokens()) {
			fxDeals.setOrderCurrency(token.nextToken());
			if (fxDeals.isValid()) {
				// validate currency
			}
		} else {
			fxDeals.setValid(false);
		}
	}

	private void setAndValidateFromCurrency(StringTokenizer token, FxDeals fxDeals) {
		if (token.hasMoreTokens()) {
			fxDeals.setFromCurrency(token.nextToken());
			if (fxDeals.isValid()) {
				// validate currency
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
