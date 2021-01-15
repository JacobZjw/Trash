package parse;

import database.JdbcUtils;
import model.BookModel;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JwZheng
 */
public class Parse implements Runnable {
    private final boolean saveImage;
    private final String url;
    private final List<BookModel> books;


    public Parse(String url, boolean saveImage) {
        this.url = url;
        this.saveImage = saveImage;
        this.books = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            //获取网页Document
            Document doc = Jsoup.parse(getRawHtml());

            //解析网页
            Elements bookElements = doc.select("ul[class=gl-warp clearfix]").select("li[class=gl-item]");

            for (Element e : bookElements) {
                String imgUrl = "http:" + e.getElementsByClass("p-img").first().getElementsByTag("a").first().getElementsByTag("img").first().attr("data-lazy-img");
                String bookID = e.attr("data-sku");
                String bookPrice = e.select("div[class=p-price]").select("strong").select("i").text();
                String bookName = e.select("div[class=p-name]").select("em").text();
                books.add(new BookModel(bookName, bookPrice, bookID, imgUrl));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(url + "解析失败！");
        }

        if (saveImage) {
            JdbcUtils.insertIntoTableWithImage(books);
        } else {
            JdbcUtils.insertIntoTable(books);
        }
    }

    public String getRawHtml() throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36 Edg/87.0.664.66");

        HttpResponse response = httpClient.execute(httpGet);

        String entity = EntityUtils.toString(response.getEntity(), "utf-8");
        EntityUtils.consume(response.getEntity());
        httpGet.abort();

        return entity;
    }
}
