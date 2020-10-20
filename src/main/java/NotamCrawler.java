import java.util.logging.Logger;

public class NotamCrawler {

	public static Logger logger = null;

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
		logger = Logger.getLogger(RequestUtils.class.getName());
	}
	
	public static void main(String[] args) {
		RequestUtils req = new RequestUtils();
		if (args.length == 0) {
			req.retrieveAllNotam();
		} else {
			req.retrieveSingleNotamData(args[0]);
		}
	}

}
