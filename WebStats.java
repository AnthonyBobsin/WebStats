import java.net.URL;
import java.net.MalformedURLException;

import java.util.Stack;

public class WebStats {
  private int pagesCrawled = 0;
  private int pathsReached = 0;
  private Stack urlsToCrawl = new Stack<String>();

  public static void main(String[] args) {
    URL url = null;
    try {
      url = new URL(args[5]);
    } catch (MalformedURLException e) {
      System.out.println("ERROR: Bad URL provided");
      e.printStackTrace();
    }

    if (url != null) {
      int numberOfPathsToCrawl = args[3];
      int numberOfPagesToCrawl = args[1];
      HttpClient client = new HttpClient();
      pushToUrlsToCrawl(url);

      while (this.pathsReached() < numberOfPathsToCrawl &&
          this.pagesCrawled() < numberOfPagesToCrawl &&
          this.urlsToCrawlCount() > 0) {
        String urlToCrawl = popFromUrlsToCrawl();

        String response = client.getHtml(url);
        // TODO: Collect all href urls from the response, and crawl them concurrently
        System.out.println(response);
      }
    }
  }

  public synchronized void incrementPagesCrawled() {
    pagesCrawled++;
  }

  public synchronized int pagesCrawled() {
    return pagesCrawled;
  }

  public synchronized void incrementPathsReached() {
    pathsReached++;
  }

  public synchronized int pathsReached() {
    return pathsReached;
  }

  public synchronized void pushToUrlsToCrawl(String url) {
    urlsToCrawl.push(url);
  }

  public synchronized String popFromUrlsToCrawl() {
    urlsToCrawl.pop();
  }

  public synchronized int urlsToCrawlCount() {
    urlsToCrawl.elementCount;
  }
}
