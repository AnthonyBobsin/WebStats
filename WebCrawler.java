import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.concurrent.*;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class WebCrawler {
  private ConcurrentHashMap<URL, HashMap> urlsWithStats = new ConcurrentHashMap<URL, HashMap>();
  private ConcurrentHashMap<String, Integer> globalUrlsWithStats = new ConcurrentHashMap<String, Integer>();
  private static int pagesCrawled = 0;
  private static int pagesToCrawl = 0;
  private static int pathsToReach = 0;

  public WebCrawler(URL url, int pagesToCrawl, int pathsToReach) {
    this.pagesToCrawl = pagesToCrawl;
    this.pathsToReach = pathsToReach;
    queueWebCrawlTask(url, 1);
  }

  public void pushToUrlsStats(URL url, HashMap stats) {
    urlsWithStats.put(url, stats);
  }

  public void printUrlsStats() {
    for (URL key : urlsWithStats.keySet()) {
      System.out.println("URL: " + key.toString());
      for (Object htmlTag : urlsWithStats.get(key).keySet()) {
        System.out.println(htmlTag.toString() + " - " + urlsWithStats.get(key).get(htmlTag).toString());
        incrementtotalGlobalUrlsWithStats(htmlTag.toString(), (Integer)(urlsWithStats.get(key).get(htmlTag)));
      }
    }
  }

  public void incrementtotalGlobalUrlsWithStats(String htmlTag, int count) {
    if (globalUrlsWithStats.containsKey(htmlTag)) {
      globalUrlsWithStats.put(htmlTag, globalUrlsWithStats.get(htmlTag) + count);
    } else {
      globalUrlsWithStats.put(htmlTag, count);
    }
  }


  public void printGlobalUrlsWithStats() {
    SortedSet<String> sortGlobalUrlsWithStats = new TreeSet<String>(globalUrlsWithStats.keySet());
    System.out.println("######### GLOBAL STATS #########");
    for (String key : sortGlobalUrlsWithStats) {
      int value = globalUrlsWithStats.get(key);
      System.out.println(key + " - " + value);
    }
  }


  /**
   * Queue a new web crawl task
   * @param url, the url to crawl
   * @param path, the path number deep this url is
   */
  public void queueWebCrawlTask(URL url, int path) {
    (new Thread(new WebCrawlJob(url, path))).start();
  }

  /**
   * Retrieve the number of pages crawled
   */
  public static synchronized int pagesCrawled() {
    return pagesCrawled;
  }

  /**
   * Increment the number of pages crawled
   */
  public static synchronized void incrementPagesCrawled() {
    pagesCrawled++;
  }

  /**
   * Decrement the number of pages crawled
   */
  public static synchronized void decrementPagesCrawled() {
    pagesCrawled--;
  }

  /**
   * Retrieve the number of pages to crawler
   */
  public static synchronized int pagesToCrawl() {
    return pagesToCrawl;
  }

  /**
   * Retrieve the number of paths to reach
   */
  public static synchronized int pathsToReach() {
    return pathsToReach;
  }

  public abstract class HtmlParser {
    private HashMap<String, Integer> urlStats = new HashMap<String, Integer>();

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
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine, htmlTag;

        Pattern regexTagPattern = Pattern.compile("< ?([A-Za-z]+)");
        Pattern regexHrefPattern = Pattern.compile("href=\"((http|https):[^ ]+)\"");
        Matcher htmlTagMatcher;

        while((inputLine = reader.readLine()) != null) {
          htmlTagMatcher = regexTagPattern.matcher(inputLine);

          if (htmlTagMatcher.find()) {
            htmlTag = htmlTagMatcher.group(1);
            incrementCountForHtmlTag(htmlTag);
            if (htmlTag.equals("a")) {
              htmlTagMatcher = regexHrefPattern.matcher(inputLine);
              if (htmlTagMatcher.find()) {
                handleFoundLink(htmlTagMatcher.group(1));
              }
            }
          }
        }
        reader.close();
        pushToUrlsStats(url, urlStats);
      } catch (Exception e) {
        System.out.println("ERROR Bad URL: " + url.toString());
        decrementPagesCrawled();
      } finally {
        if (connection != null) {
          connection.disconnect();
        }
      }
    }

    public void incrementCountForHtmlTag(String htmlTag) {
      if (urlStats.containsKey(htmlTag)) {
        urlStats.put(htmlTag, urlStats.get(htmlTag) + 1);
      } else {
        urlStats.put(htmlTag, 1);
      }
    }

    public void printUrlStats() {
      for (String key : urlStats.keySet()) {
        System.out.println(key + " - " + urlStats.get(key));
      }
    }

    /**
     * Checks if URL is valid.
     * @param url the url to check if it is valid
     */
    public Boolean isValidUrl(String url) {
      Boolean isValidUrl = true;
      URL urlChecker = null;

      try{
        urlChecker = new URL(url);
      } catch (MalformedURLException e) {
        isValidUrl = false;
      }

      return isValidUrl;
    }

    public abstract void handleFoundLink(String url);
  }

  public class WebCrawlJob extends HtmlParser implements Runnable {
    private final URL url;
    private final int pathNumber;

    public WebCrawlJob(URL url, int pathNumber) {
      this.url = url;
      this.pathNumber = pathNumber;
    }

    public void run() {
      retrieveAndParseHtml(url);
      incrementPagesCrawled();
    }

    /**
     * Checks if URL is valid. If it is, Queue URL
     * @param url the url to check if it is valid
     */
    public void handleFoundLink(String url) {
      if (isValidUrl(url) && shouldKeepCrawling()) {
        try{
          System.out.println("Queuing url: " + url);
          queueWebCrawlTask(new URL(url), pathNumber + 1);
        } catch (MalformedURLException e) {
          System.out.println("ERROR Bad URL: " + url);
        }
      }
    }

    /**
     * Tests to see if we should keep crawling
     */
    public Boolean shouldKeepCrawling() {
      return (pagesCrawled() < pagesToCrawl()) && (pathNumber < pathsToReach());
    }
  }
}
