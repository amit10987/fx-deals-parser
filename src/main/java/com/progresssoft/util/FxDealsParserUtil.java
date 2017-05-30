package com.progresssoft.util;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.IntStream;

/**
 * @author Amit Kumar
 *
 */
public class FxDealsParserUtil {

	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final String FILE_HEADER = "id,fromCurrency,orderCurrency,toCurrency,dealTime,amount";

	public static void main(String[] args) {
		try (FileWriter fileWriter = new FileWriter("fxDelas.csv")) {
			fileWriter.append(FILE_HEADER);
			fileWriter.append(NEW_LINE_SEPARATOR);
			IntStream.range(1, 100000).forEach(i -> {
				try {
					fileWriter.append(Integer.toString(i));
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append("AED");
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append("INR");
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append("INR");
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append("INR 139");
					fileWriter.append(NEW_LINE_SEPARATOR);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
