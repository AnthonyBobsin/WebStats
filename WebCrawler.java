import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.concurrent.*;
import java.util.HashMap;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class WebCrawler {
  // private HashMap<URL, HashMap<String, int>> urlsWithStats = new HashMap<URL, HashMap<String, int>>();
  private int pagesCrawled = 0;
  private int pathsReached = 0;
  private int pagesToCrawl = 0;
  private int pathsToReach = 0;

  public WebCrawler(URL url, int pagesToCrawl, int pathsToReach) {
    this.pagesToCrawl = pagesToCrawl;
    this.pathsToReach = pathsToReach;
    queueWebCrawlTask(url);
  }

  public void pushToUrlStats(HashMap stats, URL url) {}

  public void queueWebCrawlTask(URL url) {
    (new Thread(new WebCrawlJob(url))).start();
  }

  public synchronized int pagesCrawled() {
    return pagesCrawled;
  }

  public synchronized void incrementPagesCrawled() {
    pagesCrawled++;
  }

  public synchronized int pathsReached() {
    return pathsReached;
  }

  public synchronized void incrementPathsReached() {
    pathsReached++;
  }

  public synchronized int pagesToCrawl() {
    return pagesToCrawl;
  }

  public synchronized int pathsToReach() {
    return pathsToReach;
  }

  public class HtmlParser {
    private HashMap urlStats = new HashMap<String, Integer>();
    private URL[] links = new URL[10];
    public HttpClient http = new HttpClient();

    public void retrieveAndParseHtml(URL url) {

			HttpURLConnection connection = null;
			try {
				connection = (HttpURLConnection)url.openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Accept","*/*");

				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				while((inputLine = br.readLine()) != null) {
					// System.out.println(inputLine);
					String pattern = "< ?([A-Za-z]+)";
					Pattern r = Pattern.compile(pattern);
					Matcher m = r.matcher(inputLine);

					if (m.find()) {
						//System.out.println("Found value: " + m.group(1));
						//System.out.println("Found value: " + m.group(0));

						if (urlStats.containsKey(m.group(1))) {
							urlStats.put(m.group(1), (Integer)urlStats.get(m.group(1)) + 1);
						} else {
							urlStats.put(m.group(1), 1);
						}
						if (m.group(1).equals("a")) {
							System.out.println("Hello World");
						}
					}
				}
				br.close();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
    }

		public void printUrlStats() {
			for (String key : urlStats.keySet()) {
				//System.out.println(key + " - " + urlStats.get(key));
			}
		}

	}

  public class WebCrawlJob extends HtmlParser implements Runnable {
    private final URL url;

    public WebCrawlJob(URL url) {
      this.url = url;
    }

    public void run() {
      retrieveAndParseHtml(url);
			printUrlStats();
    }

    public Boolean shouldKeepCrawling() {
      return (pagesCrawled() < pagesToCrawl()) && (pathsReached() < pathsToReach());
    }
  }
}
