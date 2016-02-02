import java.net.URL;
import java.net.MalformedURLException;

public class WebStats {
  public static void main(String[] args) {
    URL url = null;
    try {
      url = new URL(args[0]);
    } catch (MalformedURLException e) {
      System.out.println("ERROR: Bad URL provided");
      e.printStackTrace();
    }

    if (url != null) {
      HttpClient client = new HttpClient();
      String response = client.getHtml(url);
      // TODO: Collect all href urls from the response, and crawl them concurrently
      System.out.println(response);
    }
  }
}
