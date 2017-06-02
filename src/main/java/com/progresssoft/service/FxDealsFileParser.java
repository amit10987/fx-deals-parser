package com.progresssoft.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.progesssoft.constant.FxDealsConstant;
import com.progresssoft.domain.AbstractDeals;
import com.progresssoft.domain.DealsCount;
import com.progresssoft.domain.FxDealsInvalid;
import com.progresssoft.domain.FxDealsValid;
import com.progresssoft.exception.FileAlreadExistException;
import com.progresssoft.model.FxDeals;
import com.progresssoft.repository.DealsCountRepository;
import com.progresssoft.repository.FxDealsInvalidRepository;
import com.progresssoft.repository.FxDealsValidRepository;

/**
 * @author amit kumar
 *
 *FxDealsFileParser use to parse fxDeals file and save the data
 *
 */
@Component
public class FxDealsFileParser implements FileParser {

	@Autowired
	FxDealsValidRepository fxDealsValidRepo;
	
	@Autowired
	FxDealsInvalidRepository fxDealsInvalidRepo;
	
	@Autowired
	DealsCountRepository dealsCountRepository;

	private static final int PARTITION_SIZE = 10000;
	private static final Logger LOGGER = Logger.getLogger(FxDealsFileParser.class);
	
	/**
	 * valid deals consumer, to save valid deals
	 *  responsible for saving the deals in DB
	 */
	Consumer<List<AbstractDeals>> validDealsConsumer = deals -> {
		deals.stream().map(m -> (FxDealsValid)m).forEach(fxDealsValidRepo::save);
	};
	
	/**
	 * to consume invalid deals, responsible for saving the deals in DB
	 */
	Consumer<List<AbstractDeals>> invalidDealsConsumer = deals -> {
		deals.stream().map(m -> (FxDealsInvalid)m).forEach(fxDealsInvalidRepo::save);
	};
	
	
	/* (non-Javadoc)
	 * @see com.progresssoft.service.FileParser#processFxDealsFile(org.springframework.web.multipart.MultipartFile)
	 */
	@Override
	public String processFxDealsFile(MultipartFile file) throws InterruptedException, FileAlreadExistException {
		LOGGER.debug("processFxDealsFile method started");
		
		String fileName = file.getOriginalFilename();
		//if file with same name already exist then throw exception
		FxDealsValid result = fxDealsValidRepo.findFirstByFileName(fileName);
		if(null != result){
			LOGGER.debug("File already exist");
			throw new FileAlreadExistException();
		}
		Map<Boolean, List<FxDeals>> fxDealsMap = getFxDealsModelFromFileStream(file);
		saveDataParellel(fxDealsMap);
		LOGGER.debug("processFxDealsFile method End");
		return FxDealsConstant.FILE_UPLOAD_SUCCESS_MSG;
	}

	/**
	 * @param fxDealsMap
	 * @throws InterruptedException
	 */
	private void saveDataParellel(Map<Boolean, List<FxDeals>> fxDealsMap) throws InterruptedException {
		LOGGER.debug("inside saveDataParellel method");
		List<Thread> threads = new ArrayList<>();
		processValidDeals(fxDealsMap, threads);
		processInvalidDeals(fxDealsMap, threads);
		processDealsCount(fxDealsMap, threads);
		LOGGER.debug("waiting to complete the process by all the thread");
		for(Thread thread : threads){
			thread.join();
		}
		LOGGER.debug("processing complete :: saveDataParellel method end");
	}

	/**
	 * @param fxDealsMap
	 * @param threads
	 */
	private void processDealsCount(Map<Boolean, List<FxDeals>> fxDealsMap, List<Thread> threads) {
		LOGGER.debug("Processing deals count");
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				saveCountWithCurrency(fxDealsMap);
			}
			
		});
		thread.start();
		threads.add(thread);
	}

	/**
	 * @param fxDealsMap
	 * @param threads
	 */
	private void processInvalidDeals(Map<Boolean, List<FxDeals>> fxDealsMap, List<Thread> threads) {
		LOGGER.debug("Processing invalid deals");
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
					saveDeals(getInvalidDeals(fxDealsMap), invalidDealsConsumer);
			}
			
		});
		thread.start();
		threads.add(thread);
	}

	/**
	 * @param fxDealsMap
	 * @param threads
	 */
	private void processValidDeals(Map<Boolean, List<FxDeals>> fxDealsMap, List<Thread> threads) {
		LOGGER.debug("Processing valid deals");
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
					saveDeals(getValidDeals(fxDealsMap), validDealsConsumer);
			}
		});
		thread.start();
		threads.add(thread);
	}

	/**
	 * @param fxDealsMap
	 */
	private void saveCountWithCurrency(Map<Boolean, List<FxDeals>> fxDealsMap) {
		List<FxDeals> deals = fxDealsMap.get(true);
		Map<String, Long> currencyDealCountMap = getCurrencyDealCountMap(deals);
		List<DealsCount> alreadySavedDealsCount = dealsCountRepository.findAll();
		Map<String, DealsCount> dealsCountByCurrency = alreadySavedDealsCount.stream().collect(Collectors.toMap(DealsCount::getOrderCurrency, Function.identity()));
		List<DealsCount> dealsCountDocuments = prepareDealsCountDocument(currencyDealCountMap, dealsCountByCurrency);
		dealsCountRepository.save(dealsCountDocuments);
		
	}

	/**
	 * @param currencyDealCountMap
	 * @param dealsCountByCurrency
	 * @return
	 */
	private List<DealsCount> prepareDealsCountDocument(Map<String, Long> currencyDealCountMap,	Map<String, DealsCount> dealsCountByCurrency) {
		List<DealsCount> dealsCountDocuments = new ArrayList<>();
		currencyDealCountMap.forEach((orderCurrency, count) -> {
			if(!dealsCountByCurrency.isEmpty() && dealsCountByCurrency.containsKey(orderCurrency)){
				DealsCount existDeal = dealsCountByCurrency.get(orderCurrency);
				existDeal.setCountOfDeals(existDeal.getCountOfDeals().longValue() + count.longValue());
				dealsCountDocuments.add(existDeal);
			}else{
				DealsCount dc = new DealsCount();
				dc.setOrderCurrency(orderCurrency);
				dc.setCountOfDeals(count);
				dealsCountDocuments.add(dc);
			}
		});
		return dealsCountDocuments;
	}

	/**
	 * @param deals
	 * @return
	 */
	private Map<String, Long> getCurrencyDealCountMap(List<FxDeals> deals) {
		Map<String, Long> countOrderCurrencyMap = deals.stream().collect(Collectors.toMap(FxDeals::getOrderCurrency, deal-> {
			return new Long(1);
		}, (v1, v2) -> {
			return v1.longValue() + v2.longValue();
		}, HashMap::new));
		return countOrderCurrencyMap;
	}

	/**
	 * @param deals
	 * @param dealConsumer
	 */
	private void saveDeals(List<AbstractDeals> deals, Consumer<List<AbstractDeals>> dealConsumer) {
		LOGGER.debug("inside saveDeals method ::::: consumer is :: " + dealConsumer);
		if(deals.isEmpty()){
			return;
		}
		int noOfPartition = deals.size() % PARTITION_SIZE == 0 ? deals.size() / PARTITION_SIZE : (deals.size() / PARTITION_SIZE) + 1;
		ExecutorService executor = Executors.newFixedThreadPool(noOfPartition);
		IntStream.range(0, noOfPartition).forEach(index -> {
			executor.submit(() -> {
				List<AbstractDeals> dealsPartition = deals.subList(index * PARTITION_SIZE, Math.min((index * PARTITION_SIZE) + PARTITION_SIZE, deals.size()));
				dealConsumer.accept(dealsPartition);
			});
		});
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOGGER.debug("Interrupted exception while saving deals" + e);
		}
	}
	
	/**
	 * @param fxDealsMap
	 * @return
	 */
	private List<AbstractDeals> getInvalidDeals(Map<Boolean, List<FxDeals>> fxDealsMap) {
		return fxDealsMap.get(false).stream().map(fxDeals -> {
			FxDealsInvalid inValidFxDeals = new FxDealsInvalid();
			BeanUtils.copyProperties(fxDeals, inValidFxDeals);
			return inValidFxDeals;
		}).collect(Collectors.toList());
	}

	/**
	 * @param fxDealsMap
	 * @return
	 */
	private List<AbstractDeals> getValidDeals(Map<Boolean, List<FxDeals>> fxDealsMap) {
		return fxDealsMap.get(true).stream().map(fxDeals -> {
			FxDealsValid validFxDeals = new FxDealsValid();
			BeanUtils.copyProperties(fxDeals, validFxDeals);
			return validFxDeals;
		}).collect(Collectors.toList());
	}

	/**
	 * @param file
	 * @return
	 */
	private Map<Boolean, List<FxDeals>> getFxDealsModelFromFileStream(MultipartFile file) {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
			return br.lines().skip(1).map(str -> {
				StringTokenizer token = new StringTokenizer(str, FxDealsConstant.COMMA_DELIMITER);
				return getFxDeals(token, file.getOriginalFilename());
			}).collect(Collectors.partitioningBy(FxDeals::isValid));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyMap();
	}

	/**
	 * To populate FxDeals object from stringtokenizer,
	 * this method responsible for validating the fxDeals object field 
	 * 
	 * @param token
	 * @param fileName
	 * @return
	 */
	private FxDeals getFxDeals(StringTokenizer token, String fileName) {
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
	private void setAndValidateAmount(StringTokenizer token, FxDeals fxDeals) {
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
	private void setAndValidateDealTime(StringTokenizer token, FxDeals fxDeals) {
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
	private void setAndValidateToCurrency(StringTokenizer token, FxDeals fxDeals) {
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
	private void setAndValidateOrderCurrency(StringTokenizer token, FxDeals fxDeals) {
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
	private void setAndValidateFromCurrency(StringTokenizer token, FxDeals fxDeals) {
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
	private void setAndValidateId(StringTokenizer token, FxDeals fxDeals) {
		if (token.hasMoreTokens()) {
			fxDeals.setId(token.nextToken());
		} else {
			fxDeals.setValid(false);
		}
	}
}
