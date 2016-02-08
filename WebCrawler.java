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
                System.out.println("Found link: " + htmlTagMatcher.group(1));
                handleFoundLink(htmlTagMatcher.group(1));
              }
            }
          }
        }
        reader.close();
      } catch (Exception e) {
        e.printStackTrace();
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

    public WebCrawlJob(URL url) {
      this.url = url;
    }

    public void run() {
      retrieveAndParseHtml(url);
      printUrlStats();
    }

    /**
     * Checks if URL is valid. If it is, Queue URL
     * @param url the url to check if it is valid
     */
    public void handleFoundLink(String url) {
      if (isValidUrl(url)) {
        try{
          System.out.println("Queuing url: " + url);
          queueWebCrawlTask(new URL(url));
        } catch (MalformedURLException e) {
          System.out.println("ERROR: bad url" + url);
          e.printStackTrace();
        }
      }
    }

    public Boolean shouldKeepCrawling() {
      return (pagesCrawled() < pagesToCrawl()) && (pathsReached() < pathsToReach());
    }
  }
}
