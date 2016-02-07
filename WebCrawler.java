import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.concurrent.*;
import java.util.HashMap;
import java.util.Map;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class WebCrawler {
  private HashMap<URL, HashMap> urlsWithStats = new HashMap<URL, HashMap>();
  private int pagesCrawled = 0;
  private int pathsReached = 0;
  private int pagesToCrawl = 0;
  private int pathsToReach = 0;

  public WebCrawler(URL url, int pagesToCrawl, int pathsToReach) {
    this.pagesToCrawl = pagesToCrawl;
    this.pathsToReach = pathsToReach;
    queueWebCrawlTask(url);
  }

  public void pushToUrlsStats(HashMap stats, URL url) {
    urlsWithStats.put(url, stats);
  }

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
    private HashMap<String, Integer> urlStats = new HashMap<String, Integer>();
    private URL[] links = new URL[10];
    public HttpClient http = new HttpClient();

    /**
     * If tag found in urlStats, increment tag
     * @param keyword the keyword to increment
     */
    public void incrementUrlStats(String keyword) {
      urlStats.put(keyword, urlStats.get(keyword) + 1);
    }

    /**
     * If tag not found in urlStats, create tag
     * @param keyword the keyword to create
     */
    public void addUrlStats(String keyword) {
      urlStats.put(keyword, 1);
    }

    /**
     * Checks if URL is valid. If it is, Queue URL
     * @param url the url to check if it is valid
     */
    public void isURL(String url) {
      Boolean isValidURL = false;
      URL urlChecker = null;

      try{
        urlChecker = new URL(url);
      } catch (MalformedURLException e) {
        isValidURL = false;
      } finally {
        if (!isValidURL) {
          //System.out.println(url);
          //queueWebCrawlTask(checkUrl);
        }
      }
    }

    /**
     * Parse HTML content
     * @param url an absolute URL to parse
     */
    public void retrieveAndParseHtml(URL url) {
      HttpURLConnection connection = null;
      try {
        connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept","*/*");

        BufferedReader bReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;

        Pattern regexTagPattern = Pattern.compile("< ?([A-Za-z]+)");
        Pattern regexHerfPattern = Pattern.compile("href=\"(.*?)\""); //Regex Needs to be better implemented;  Another Regex: "<a\\s+href\\s*=\\s*(\"[^\"]*\"|[^\\s>]*)\\s*>"
        Matcher regexMatcher;
        Matcher regexTagMatcher;

        while((inputLine = bReader.readLine()) != null) {
          regexMatcher = regexTagPattern.matcher(inputLine);
          if (regexMatcher.find()) {
            if (urlStats.containsKey(regexMatcher.group(1))) {
              incrementUrlStats(regexMatcher.group(1));
            } else {
              addUrlStats(regexMatcher.group(1));
            }
            if (regexMatcher.group(1).equals("a")) {
              regexTagMatcher = regexHerfPattern.matcher(inputLine);
              if(regexTagMatcher.find()) {    //If link found
                isURL(regexTagMatcher.group(1));
              }
            }
          }
        }
        bReader.close();
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
        System.out.println(key + " - " + urlStats.get(key));
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
