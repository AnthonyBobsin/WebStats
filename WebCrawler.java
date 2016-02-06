import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.concurrent.*;
import java.util.HashMap;

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
    // private HashMap urlStats = new HashMap<String, int>();
    private URL[] links = new URL[10];
    public HttpClient http = new HttpClient();

    public void retrieveAndParseHtml(URL url) {

    }
  }

  public class WebCrawlJob extends HtmlParser implements Runnable {
    private final URL url;

    public WebCrawlJob(URL url) {
      this.url = url;
    }

    public void run() {
      System.out.println("hello world");
    }

    public Boolean shouldKeepCrawling() {
      return (pagesCrawled() < pagesToCrawl()) && (pathsReached() < pathsToReach());
    }
  }
}
