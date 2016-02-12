import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.concurrent.*;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class WebCrawler {
  // Links Url to HashMaps containing html tag and value
  private ConcurrentHashMap<URL, HashMap> urlsWithStats = new ConcurrentHashMap<URL, HashMap>();
  // Counts all htmltags
  private ConcurrentHashMap<String, Integer> globalUrlsWithStats = new ConcurrentHashMap<String, Integer>();
  //Amount of pages crawled
  private static int pagesCrawled = 0;
  //Amount of pages to crawl
  private static int pagesToCrawl = 0;
  //Amount of paths to reach
  private static int pathsToReach = 0;
  // List of all web crawl threads spawned
  public static ConcurrentLinkedQueue<Thread> webCrawlerThreads = new ConcurrentLinkedQueue<Thread>();

  /**
   * WebCrawler Constructor
   * @param url, The url the queue
   * @param pagesToCrawl,  Amount of pages the crawler should crawl
   * @param pathstoReach, The depth of the crawl
   */
  public WebCrawler(URL url, int pagesToCrawl, int pathsToReach) {
    this.pagesToCrawl = pagesToCrawl;
    this.pathsToReach = pathsToReach;
    queueWebCrawlTask(url, 1);
  }

  /**
   * Push URL with Hashpmap to link them to URls
   * @param url, Url to push
   * @param stats, Hashmap to push
   */
  public void pushToUrlsStats(URL url, HashMap stats) {
    urlsWithStats.put(url, stats);
  }

  /**
   * Print all the Urls with the tags
   */
  public void printUrlsStats() {
    for (URL key : urlsWithStats.keySet()) {
      System.out.println("\nURL: " + key.toString());
      for (Object htmlTag : urlsWithStats.get(key).keySet()) {
        System.out.println(htmlTag.toString() + " - " + urlsWithStats.get(key).get(htmlTag).toString());
        incrementtotalGlobalUrlsWithStats(htmlTag.toString(), (Integer)(urlsWithStats.get(key).get(htmlTag)));
      }
    }
  }

  /**
   * Increment globalUrlsWithStats if tag found
   * @param htmlTag, htmlTag to increment
   * @param count, Number of times to increment the htmlTag
   */
  public void incrementtotalGlobalUrlsWithStats(String htmlTag, int count) {
    if (globalUrlsWithStats.containsKey(htmlTag)) {
      globalUrlsWithStats.put(htmlTag, globalUrlsWithStats.get(htmlTag) + count);
    } else {
      globalUrlsWithStats.put(htmlTag, count);
    }
  }

  /**
   * Organize and order htmlTags
   */
  public void printGlobalUrlsWithStats() {
    SortedSet<String> sortGlobalUrlsWithStats = new TreeSet<String>(globalUrlsWithStats.keySet());
    System.out.println("\n######### GLOBAL STATS #########");
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
    System.out.println("Queuing url: " + url);
    Thread webCrawlTask = new Thread(new WebCrawlJob(url, path));
    webCrawlTask.start();
    webCrawlerThreads.add(webCrawlTask);
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
    //Html tag count tracker
    private HashMap<String, Integer> urlStats = new HashMap<String, Integer>();

    /**
     * Parse HTML content
     * @param url, an absolute URL to parse
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
    /**
     * Increment urlStats if tag found; if not, add tag to urlStats
     * @param htmlTag, Html tag that needs to be added or incremented
     */
    public void incrementCountForHtmlTag(String htmlTag) {
      if (urlStats.containsKey(htmlTag)) {
        urlStats.put(htmlTag, urlStats.get(htmlTag) + 1);
      } else {
        urlStats.put(htmlTag, 1);
      }
    }

    /**
     * Checks if URL is valid.
     * @param url, The url to check if it is valid
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

  /**
   * WebCrawlJob that implements Runnable; calls the run function
   */
  public class WebCrawlJob extends HtmlParser implements Runnable {
    private final URL url;
    private final int pathNumber;

    /**
     * WebCrawlJob Constructor
     * @param url, Url to queue
     * @param pathNumber, how far the
     */
    public WebCrawlJob(URL url, int pathNumber) {
      this.url = url;
      this.pathNumber = pathNumber;
    }

    /**
     * Run html Parser
     */
    public void run() {
      retrieveAndParseHtml(url);
      incrementPagesCrawled();
    }

    /**
     * Checks if URL is valid. If it is, Queue URL
     * @param url, the url to check if it is valid
     */
    public void handleFoundLink(String url) {
      if (isValidUrl(url) && shouldKeepCrawling()) {
        try{
          queueWebCrawlTask(new URL(url), pathNumber + 1);
        } catch (MalformedURLException e) {
          System.out.println("ERROR Bad URL: " + url);
        }
      }
    }

    /**
     * Tests to see if the program should keep crawling
     */
    public Boolean shouldKeepCrawling() {
      return (pagesCrawled() < pagesToCrawl()) && (pathNumber < pathsToReach());
    }
  }
}
