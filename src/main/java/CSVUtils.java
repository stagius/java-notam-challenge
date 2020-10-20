import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;

public class CSVUtils {

	private static final char DEFAULT_SEPARATOR = ',';

	private FileWriter writer;

	public CSVUtils() {
		try {
			this.writer = new FileWriter("./output.csv");
		} catch (IOException e) {
			NotamCrawler.logger.log(Level.WARNING, "{0}", "Couldn't open the file!");
		}
	}

	/**
	 * Save the CSV file.
	 * 
	 * @param data list of data entries
	 */
	public void saveCSVFile(List<String> data) {
		writeLine(this.writer, data, DEFAULT_SEPARATOR, ' ');
		NotamCrawler.logger.log(Level.INFO, "{0}", "CSV Exported");
	}

	/**
	 * Close the CSV file.
	 */
	public void closeCSVFile() {
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			NotamCrawler.logger.log(Level.WARNING, "{0}", "Couldn't close the CSV file!");
		}
	}

	/**
	 * Fixing special characters in the CSV entry.
	 * 
	 * @param value entry
	 * @return String
	 */
	private static String followCVSformat(String value) {
		String result = value;
		if (result.contains("\"")) {
			result = result.replace("\"", "\"\"");
		}
		return result;
	}

	/**
	 * 
	 * @param w           writer reference
	 * @param values      list of entries
	 * @param separators  separator character
	 * @param customQuote quote
	 * @throws IOException
	 */
	public static void writeLine(Writer w, List<String> values, char separators, char customQuote) {
		boolean first = true;
		if (separators == ' ') {
			separators = DEFAULT_SEPARATOR;
		}
		StringBuilder sb = new StringBuilder();
		for (String value : values) {
			if (!first) {
				sb.append(separators);
			}
			if (customQuote == ' ') {
				sb.append(followCVSformat(value));
			} else {
				sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
			}
			first = false;
		}
		sb.append("\n");
		try {
			w.append(sb.toString());
		} catch (IOException e) {
			NotamCrawler.logger.log(Level.WARNING, "{0}", "Couldn't write an entry in the CSV file!");
		}
	}
}
