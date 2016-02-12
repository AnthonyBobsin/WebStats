import java.net.URL;
import java.net.MalformedURLException;

import java.util.Stack;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class WebStats {

  /**
   * Main Parser Class
   * @param args, Arguments supplied by the users
   */
  public static void main(String[] args) {
    if (args.length == 1) {
      runWebCrawler(args[0], 10, 3);
    } else {
      runWebCrawler(args[4],Integer.parseInt(args[3]), Integer.parseInt(args[1]));
    }
  }

  /**
   * Run Web Crawler with specific arguments or default arguments. Waits for crawling to finish,
   *  then prints the stats found.
   * @param urlArgument, The url the parse
   * @param pagesArgument, The amount of pages to parse
   * @param pathArgument, The path depth
   */
  public static void runWebCrawler(String urlArgument, int pagesArgument, int pathArgument) {
    URL url = null;

    try {
      url = new URL(urlArgument);
    } catch (MalformedURLException e) {
      System.out.println("ERROR: Bad URL provided");
      e.printStackTrace();
    }

    if (url != null) {
      WebCrawler crawler = new WebCrawler(url, pagesArgument, pathArgument);

      boolean threadIsAlive = true;
      while (threadIsAlive) {
        for (Thread webCrawlTask : crawler.webCrawlerThreads) {
          if (webCrawlTask.isAlive()) {
            threadIsAlive = true;
            break;
          }
          threadIsAlive = false;
        }
      };

      crawler.printUrlsStats();
      crawler.printGlobalUrlsWithStats();
    }
  }

}
