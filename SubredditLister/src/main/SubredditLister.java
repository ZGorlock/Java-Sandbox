/*
 * File:    SubredditLister.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import ch.qos.logback.classic.LoggerContext;
import org.apache.commons.io.FileUtils;
import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

public class SubredditLister {
    
    static {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.htmlunit").setAdditive(false);
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.apache.http").setAdditive(false);
    }
    
    private static final String username = "";
    
    private static final String password = "";
    
    private static final String baseUrl = "https://old.reddit.com";
    
    private static final File out = new File("data", "subreddits.txt");
    
    private static WebClient webClient;
    
    public static void main(String[] args) throws Exception {
        initWebClient();
        login();
        
        final Document subscriptionPage = getSubscriptionPage();
        final List<String> subreddits = getSubreddits(subscriptionPage);
        
        FileUtils.writeLines(out, subreddits);
    }
    
    private static void initWebClient() throws Exception {
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
    }
    
    private static void login() throws Exception {
        final HtmlPage loginPage = webClient.getPage(baseUrl + "/login");
        final HtmlForm loginForm = loginPage.getFirstByXPath("//form[@id='login-form']");
        loginForm.getInputByName("user").setValueAttribute(username);
        loginForm.getInputByName("passwd").setValueAttribute(password);
        loginForm.getElementsByTagName("button").get(0).click();
        Thread.sleep(5000);
    }
    
    private static Document getSubscriptionPage() throws Exception {
        final String html = webClient.getPage(baseUrl + "/subreddits").getWebResponse().getContentAsString();
        return Jsoup.parse(html);
    }
    
    private static List<String> getSubreddits(Document subscriptionPage) throws Exception {
        final Element subscriptionBox = subscriptionPage.selectFirst("div.subscription-box");
        final Elements subscriptions = subscriptionBox.select("span.subscribe-button");
        return subscriptions.stream()
                .map(subscription -> subscription.attr("data-sr_name"))
                .collect(Collectors.toList());
    }
    
}
