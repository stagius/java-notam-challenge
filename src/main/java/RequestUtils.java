import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestUtils {

	public static final String RES_FIR = "http://notamweb.aviation-civile.gouv.fr/Script/IHM/Bul_FIR.php?FIR_Langue=FR";
	public static final String RES_NOTAM = "http://notamweb.aviation-civile.gouv.fr/Script/IHM/Bul_Notam.php?NOTAM_Langue=FR";

	public static final String BRES = "bResultat";
	public static final String MODE = "ModeAffichage";
	public static final String NPRINT = "bImpression";
	public static final String FDATE = "FIR_Date_DATE";
	public static final String FHOUR = "FIR_Date_HEURE";
	public static final String FLANG = "FIR_Langue";
	public static final String FSPAN = "FIR_Duree";
	public static final String FRULE = "FIR_CM_REGLE";
	public static final String FGPS = "FIR_CM_GPS";
	public static final String FINFO = "FIR_CM_INFO_COMP";
	public static final String FMIN = "FIR_NivMin";
	public static final String FMAX = "FIR_NivMax";
	public static final String FFIR = "FIR_Tab_Fir[0]";
	public static final String NLANG = "NOTAM_Langue";
	public static final String NMATN = "NOTAM_Mat_Notam";

	public static final String Q_PATTERN = "Q\\) (<\\/font><font class='NOTAM-CORPS'>)(.*)(<\\/font>)";
	public static final String A_PATTERN = "A\\) (<\\/font><font class='NOTAM-CORPS'>)(.*)(<\\/font>)";
	public static final String ID_PATTERN = "([A-Z]+)-([A-Z])(\\d{4})\\/(\\d+)";

	private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

	/**
	 * Contains all Notam IDs.
	 */
	private List<String> notamIds;

	/**
	 * Contains all Notam Q section data.
	 */
	private Map<String, String> notamQData;

	/**
	 * Contains all Notam A section data.
	 */
	private Map<String, String> notamAData;

	public RequestUtils() {
		this.notamIds = new ArrayList<>();
		this.notamQData = new HashMap<>();
		this.notamAData = new HashMap<>();
	}

	/**
	 * Sends 1 POST request to retrieve all FIR data.
	 */
	public void retrieveAllNotam() {
		Map<Object, Object> data = new HashMap<>();
		initializeFirDataRequestBody(data);

		HttpRequest request = createNewRequest(RES_FIR, data);
		HttpResponse<String> response = getResponse(request);
		if (response != null && response.statusCode() == 200) {
			Matcher matchId = Pattern.compile(ID_PATTERN).matcher(response.body());
			while (matchId.find()) {
				if (!notamIds.contains(matchId.group(0))) {
					this.notamIds.add(matchId.group(0));
				}
			}
		}

		if (!notamIds.isEmpty()) {
			NotamCrawler.logger.log(Level.INFO, "{0}", notamIds.size() + " unique IDs found!");
			notamIds.forEach(id -> retrieveSingleNotamData(id));
			NotamCrawler.logger.log(Level.INFO, "{0}", (!notamQData.isEmpty()) ? notamQData.size() + " unique NOTAMs retrieved" : "");
		}

		CSVUtils csvUtils = new CSVUtils();
		List<String> exportData = new ArrayList<>();
		for (String id : notamIds) {
			exportData.add(id);
			exportData.add(notamQData.get(id));
			exportData.add(notamAData.get(id));
			csvUtils.saveCSVFile(exportData);
			exportData.clear();
		}
		csvUtils.closeCSVFile();
	}

	/**
	 * Sends n POST requests to retrieve all FIR data of given NOTAM ID(s).
	 */
	public void retrieveSingleNotamData(String id) {
		Map<Object, Object> data = new HashMap<>();
		Matcher m = Pattern.compile(ID_PATTERN).matcher(id);
		while (m.find()) {
			initializeNotamDataRequestBody(data, m.group(1), m.group(2), m.group(3), m.group(4));

			HttpRequest request = createNewRequest(RES_NOTAM, data);
			HttpResponse<String> response = getResponse(request);
			if (response != null && response.statusCode() == 200) {
				extractQPattern(response, id, m);
				extractAPattern(response, id, m);
//				extractBPattern(response, id, m);
//				extractDPattern(response, id, m);
//				extractEPattern(response, id, m);
//				extractFPattern(response, id, m);
//				extractGPattern(response, id, m);
			}
		}
	}

	private void extractQPattern(HttpResponse<String> response, String id, Matcher m) {
		Matcher mat = Pattern.compile(Q_PATTERN).matcher(response.body());
		while (mat.find()) {
			if (!notamQData.containsKey(id)) {
				this.notamQData.put(id, mat.group(2));
				NotamCrawler.logger.log(Level.INFO, "{0}",
						m.group(1) + " " + m.group(2) + " " + m.group(3) + " " + m.group(4) + " : " + mat.group(2));
			}
		}
	}

	private void extractAPattern(HttpResponse<String> response, String id, Matcher m) {
		Matcher mat = Pattern.compile(A_PATTERN).matcher(response.body());
		while (mat.find()) {
			if (!notamAData.containsKey(id)) {
				this.notamAData.put(id, mat.group(2));
				NotamCrawler.logger.log(Level.INFO, "{0}",
						m.group(1) + " " + m.group(2) + " " + m.group(3) + " " + m.group(4) + " : " + mat.group(2));
			}
		}

	}

	/**
	 * Initializes the data map containing POST request body information.
	 * 
	 * @param data data map
	 */
	private void initializeFirDataRequestBody(Map<Object, Object> data) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		String[] tab = formatter.format(new Date()).split(" ");

		data.put(BRES, "true");
		data.put(MODE, "RESUME");
		data.put(FDATE, tab[0]);
		data.put(FHOUR, tab[1]);
		data.put(FLANG, "FR");
		data.put(FSPAN, "12");
		data.put(FRULE, "1");
		data.put(FGPS, "2");
		data.put(FINFO, "2");
		data.put(FMIN, "0");
		data.put(FMAX, "20");
		data.put(FFIR, "LFBB");
	}

	/**
	 * Initializes the data map containing POST request body information.
	 * 
	 * @param data data map
	 * @param m0   international code
	 * @param m1   category code
	 * @param m2   serial code
	 * @param m3   year
	 */
	private void initializeNotamDataRequestBody(Map<Object, Object> data, String m0, String m1, String m2, String m3) {
		data.put(BRES, "true");
		data.put(NPRINT, "");
		data.put(MODE, "RESUME");
		data.put(NLANG, "FR");
		data.put(NMATN + "[0][0]", m0);
		data.put(NMATN + "[0][1]", m1);
		data.put(NMATN + "[0][2]", m2);
		data.put(NMATN + "[0][3]", m3);
	}

	/**
	 * Sends the request and returns the response.
	 * 
	 * @param request request
	 * @return HttpResponse
	 */
	private HttpResponse<String> getResponse(HttpRequest request) {
		HttpResponse<String> response = null;
		try {
			response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 403) {
				NotamCrawler.logger.log(Level.WARNING, "Request params contain errors");
			}
			if (response.body().contains("MSG_ERREUR")) {
				NotamCrawler.logger.log(Level.WARNING, "Missing or wrong params in the request");
			}
		} catch (IOException | InterruptedException e) {
			NotamCrawler.logger.log(Level.WARNING, "Couldn't send the POST request");
			// Restore interrupted state
			Thread.currentThread().interrupt();
		}
		return response;
	}

	/**
	 * Creates a new instance of HttpRequest
	 * 
	 * @param uri  custom URI
	 * @param data body data map
	 * @return HttpRequest
	 */
	private HttpRequest createNewRequest(String uri, Map<Object, Object> data) {
		return HttpRequest.newBuilder().POST(buildFormDataFromMap(data)).uri(URI.create(uri))
				.setHeader("User-Agent", "Java 11 HttpClient")
				.header("Content-Type", "application/x-www-form-urlencoded").build();
	}

	/**
	 * Builds POST body with given data map.
	 * 
	 * @param data input map
	 * @return HttpRequest.BodyPublisher
	 */
	private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<Object, Object> entry : data.entrySet()) {
			if (builder.length() > 0) {
				builder.append("&");
			}
			builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
			builder.append("=");
			builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
		}
		return HttpRequest.BodyPublishers.ofString(builder.toString());
	}

}
