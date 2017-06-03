package com.progresssoft.util;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import ch.qos.logback.core.net.SyslogOutputStream;

/**
 * @author Amit Kumar
 *
 */
public class FxDealsParserUtil {

	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final String FILE_HEADER = "id,fromCurrency,orderCurrency,toCurrency,dealTime,amount";
	public static void main(String[] args) {
		try (FileWriter fileWriter = new FileWriter("fxDeals.csv")) {
			fileWriter.append(FILE_HEADER);
			fileWriter.append(NEW_LINE_SEPARATOR);
			IntStream.rangeClosed(1, 30000).forEach(createValidRows(fileWriter, "AED"));
			IntStream.rangeClosed(1, 30000).forEach(createValidRows(fileWriter, "INR"));
			IntStream.rangeClosed(60001, 70000).forEach(createValidRows(fileWriter, "USD"));
			IntStream.rangeClosed(70001, 100000).forEach(createInvalidRows(fileWriter));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	private static IntConsumer createInvalidRows(FileWriter fileWriter) {
		return i -> {
			try {
				fileWriter.append(Integer.toString(i));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("");
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("INR");
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("INR139");
				fileWriter.append(NEW_LINE_SEPARATOR);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}
	private static IntConsumer createValidRows(FileWriter fileWriter, String orderCurrency) {
		return i -> {
			try {
				fileWriter.append(Integer.toString(i));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("AED");
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(orderCurrency);
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append("INR");
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(orderCurrency + "139");
				fileWriter.append(NEW_LINE_SEPARATOR);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}

}
