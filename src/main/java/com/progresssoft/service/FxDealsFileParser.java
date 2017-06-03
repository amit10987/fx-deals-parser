package com.progresssoft.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.progesssoft.constant.FxDealsConstant;
import com.progresssoft.domain.AbstractDeals;
import com.progresssoft.domain.DealsCount;
import com.progresssoft.domain.FxDealsInvalid;
import com.progresssoft.domain.FxDealsValid;
import com.progresssoft.exception.FileAlreadExistException;
import com.progresssoft.exception.FxDealDuplicateKeyException;
import com.progresssoft.model.FxDeals;
import com.progresssoft.repository.DealsCountRepository;
import com.progresssoft.repository.FxDealsInvalidRepository;
import com.progresssoft.repository.FxDealsValidRepository;

/**
 * @author amit kumar
 *
 *         FxDealsFileParser use to parse fxDeals file and save the data
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
	 * valid deals consumer, to save valid deals. Responsible for saving the
	 * valid deals in DB
	 */
	BiConsumer<List<AbstractDeals>, Map<String, RuntimeException>> validDealsConsumer = (deals, exceptionsByDealId) -> {
		deals.stream().map(m -> (FxDealsValid) m).forEach(deal -> {
			try{
				fxDealsValidRepo.insert(deal);
			}catch(RuntimeException ex){
				exceptionsByDealId.put(deal.getId(), ex);
			}
		});
	};
 
	/**
	 * to consume invalid deals, responsible for saving the InValid deals in DB
	 */
	BiConsumer<List<AbstractDeals>, Map<String, RuntimeException>> invalidDealsConsumer = (deals, exceptionsByDealId) -> {
		deals.stream().map(m -> (FxDealsInvalid) m).forEach(deal -> {
			try{
				fxDealsInvalidRepo.insert(deal);
			}catch(RuntimeException ex){
				exceptionsByDealId.put(deal.getId(), ex);
			}
		});
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.progresssoft.service.FileParser#processFxDealsFile(org.
	 * springframework.web.multipart.MultipartFile)
	 */
	@Override
	public String processFxDealsFile(MultipartFile file) throws InterruptedException, FileAlreadExistException, FxDealDuplicateKeyException {
		LOGGER.debug("processFxDealsFile method started");
		
		Map<String, RuntimeException> exceptionsByDealId = new ConcurrentHashMap<>();
		
		// if file with same name already exist then throw exception
		checkIfFileAlreadyExist(file);
		
		Map<Boolean, List<FxDeals>> fxDealsMap = getFxDealsModelFromFileStream(file);
		
		LOGGER.debug("before saveDataParellel");
		saveDataParellel(fxDealsMap, exceptionsByDealId);
		LOGGER.debug("after saveDataParellel");
		
		saveCountWithCurrency(fxDealsMap, exceptionsByDealId);
		
		//process if there is any exception
		LOGGER.debug("before processExceptions");
		processExceptions(exceptionsByDealId);
		LOGGER.debug("after processExceptions");
		return FxDealsConstant.FILE_UPLOAD_SUCCESS_MSG;
	}

	/**
	 * @param file
	 * @throws FileAlreadExistException
	 */
	private void checkIfFileAlreadyExist(MultipartFile file) throws FileAlreadExistException {
		String fileName = file.getOriginalFilename();
		FxDealsValid result = fxDealsValidRepo.findFirstByFileName(fileName);
		if (null != result) {
			LOGGER.debug("File already exist");
			throw new FileAlreadExistException();
		}
	}

	private void processExceptions(Map<String, RuntimeException> exMap) throws FxDealDuplicateKeyException {
		AtomicLong countDuplicateEntry = new AtomicLong(0);
		exMap.values().forEach(ex -> {
			if(ex instanceof DuplicateKeyException){
				countDuplicateEntry.incrementAndGet();
			}
		});
		if(countDuplicateEntry.get() > 0){
			throw new FxDealDuplicateKeyException(countDuplicateEntry.get() + " records not saved due to duplicate entry");
		}
	}

	/**
	 * @param fxDealsMap
	 * @param exceptionsByDealId 
	 * @throws InterruptedException
	 */
	private void saveDataParellel(Map<Boolean, List<FxDeals>> fxDealsMap, Map<String, RuntimeException> exceptionsByDealId) throws InterruptedException {
		LOGGER.debug("inside saveDataParellel method");
		List<Thread> threads = new ArrayList<>();
		processValidDeals(fxDealsMap, threads, exceptionsByDealId);
		processInvalidDeals(fxDealsMap, threads, exceptionsByDealId);
		LOGGER.debug("waiting to complete the process by all the thread");
		for (Thread thread : threads) {
			thread.join();
		}
		LOGGER.debug("processing complete :: saveDataParellel method end");
	}

	/**
	 * @param fxDealsMap
	 * @param threads
	 * @param exceptionsByDealId 
	 */
	private void processInvalidDeals(Map<Boolean, List<FxDeals>> fxDealsMap, List<Thread> threads, Map<String, RuntimeException> exceptionsByDealId) {
		LOGGER.debug("Processing invalid deals");
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				saveDeals(getInvalidDeals(fxDealsMap), invalidDealsConsumer, exceptionsByDealId);
			}
		});
		thread.start();
		threads.add(thread);
	}

	/**
	 * @param fxDealsMap
	 * @param threads
	 * @param exceptionsByDealId 
	 */
	private void processValidDeals(Map<Boolean, List<FxDeals>> fxDealsMap, List<Thread> threads, Map<String, RuntimeException> exceptionsByDealId) {
		LOGGER.debug("Processing valid deals");
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				saveDeals(getValidDeals(fxDealsMap), validDealsConsumer, exceptionsByDealId);
			}
		});
		thread.start();
		threads.add(thread);
	}

	/**
	 * @param fxDealsMap
	 * @param exceptionsByDealId 
	 */
	private void saveCountWithCurrency(Map<Boolean, List<FxDeals>> fxDealsMap, Map<String, RuntimeException> exceptionsByDealId) {
		LOGGER.debug("saveCountWithCurrency method started");
		List<FxDeals> deals = fxDealsMap.get(true);
		Set<String> dealIdNotSaved = exceptionsByDealId.keySet();
		//remove entry those are not saved because of some exception may be duplicate entry
		if(!dealIdNotSaved.isEmpty()){
			deals.removeIf(deal -> dealIdNotSaved.contains(deal.getId()));
		}
		
		//Prepare the map whose key is currency and value is count of the currency
		Map<String, Long> currencyDealCountMap = getCurrencyDealCountMap(deals);
		//find the currency those are saved in DB and update deal count if already exist
		List<DealsCount> alreadySavedDealsCount = dealsCountRepository.findAll();
		Map<String, DealsCount> dealsCountByCurrency = alreadySavedDealsCount.stream().collect(Collectors.toMap(DealsCount::getOrderCurrency, Function.identity()));
		List<DealsCount> dealsCountDocuments = prepareDealsCountDocument(currencyDealCountMap, dealsCountByCurrency);
		dealsCountRepository.save(dealsCountDocuments);
		LOGGER.debug("saveCountWithCurrency method End");
	}

	/**
	 * @param currencyDealCountMap
	 * @param dealsCountByCurrency
	 * @return
	 */
	private List<DealsCount> prepareDealsCountDocument(Map<String, Long> currencyDealCountMap,
			Map<String, DealsCount> dealsCountByCurrency) {
		List<DealsCount> dealsCountDocuments = new ArrayList<>();
		currencyDealCountMap.forEach((orderCurrency, count) -> {
			if (!dealsCountByCurrency.isEmpty() && dealsCountByCurrency.containsKey(orderCurrency)) {
				DealsCount existDeal = dealsCountByCurrency.get(orderCurrency);
				existDeal.setCountOfDeals(existDeal.getCountOfDeals().longValue() + count.longValue());
				dealsCountDocuments.add(existDeal);
			} else {
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
		Map<String, Long> countOrderCurrencyMap = deals.stream()
				.collect(Collectors.toMap(FxDeals::getOrderCurrency, deal -> {
					return new Long(1);
				}, (v1, v2) -> {
					return v1.longValue() + v2.longValue();
				}, HashMap::new));
		return countOrderCurrencyMap;
	}

	/**
	 * @param deals
	 * @param dealConsumer
	 * @param exceptionsByDealId 
	 */
	private void saveDeals(List<AbstractDeals> deals, BiConsumer<List<AbstractDeals>, Map<String, RuntimeException>> dealConsumer, Map<String, RuntimeException> exceptionsByDealId) {
		LOGGER.debug("inside saveDeals method ::::: consumer is :: " + dealConsumer);
		if (deals.isEmpty()) {
			return;
		}
		int noOfPartition = deals.size() % PARTITION_SIZE == 0 ? deals.size() / PARTITION_SIZE : (deals.size() / PARTITION_SIZE) + 1;
		ExecutorService executor = Executors.newFixedThreadPool(noOfPartition);
		IntStream.range(0, noOfPartition).forEach(index -> {
			executor.submit(() -> {
				List<AbstractDeals> dealsPartition = deals.subList(index * PARTITION_SIZE, Math.min((index * PARTITION_SIZE) + PARTITION_SIZE, deals.size()));
				dealConsumer.accept(dealsPartition, exceptionsByDealId);
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
				return FxDealsFileDataValidator.validateAndGetFxDeals(token, file.getOriginalFilename());
			}).collect(Collectors.partitioningBy(FxDeals::isValid));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyMap();
	}
}
