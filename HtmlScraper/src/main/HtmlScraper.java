/*
 * File:    HtmlScraper.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.IOException;

import ch.qos.logback.classic.LoggerContext;
import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;

public class HtmlScraper {
    
    static {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.htmlunit").setAdditive(false);
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.apache.http").setAdditive(false);
    }
    
    public static void main(String[] args) throws Exception {
        Document d = getHtml("https://www.google.com/search?num=1&q=define+run");
        int x = 5;
    }
    
    /**
     * Downloads an html from a url and returns the retrieved Document.
     *
     * @param url The url address to download the html from.
     * @return The retrieved Document or null if there was an error.
     */
    public static Document getHtml(String url) throws IOException {
//        WebDriver driver = new ChromeDriver();
//        driver.get(url);
//        return Jsoup.parse(driver.getPageSource());
        
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setDownloadImages(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        HtmlPage page = webClient.getPage(url);
        webClient.waitForBackgroundJavaScript(30 * 1000);
        
        return Jsoup.parse(page.asXml());

//        return Jsoup.connect(url)
//                .ignoreContentType(true)
//                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.80 Safari/537.36")
//                .referrer("http://www.google.com")
//                .timeout(12000)
//                .followRedirects(true)
//                .execute()
//                .parse();
    }
    
}
