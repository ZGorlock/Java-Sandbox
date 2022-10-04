/*
 * File:    WebReader.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.util.function.BiFunction;
import java.util.function.Function;

import main.model.BaseModel;
import main.model.site.amazon.AmazonItem;
import main.model.site.chrome.ChromeAddon;
import main.model.site.firefox.FirefoxAddon;
import main.model.site.github.GithubLatestRelease;
import main.model.site.github.GithubRepo;
import main.model.site.reddit.RedditSubreddit;
import main.model.site.target.TargetItem;
import main.model.site.walmart.WalmartItem;

public class WebReader {
    
    public static void main(String[] args) throws Exception {
//        amazonItemTest();
//        walmartItemTest();
//        targetItemTest();

//        redditSubredditTest();

//        firefoxAddonTest();
//        chromeAddonTest();
        
        githubRepoTest();
        githubLatestReleaseTest();
    }
    
    private static final BiFunction<Function<String, ? extends BaseModel>, String, BaseModel> baseLoader = (Function<String, ? extends BaseModel> generator, String url) -> {
        final BaseModel result = generator.apply(url);
        System.out.println(result + System.lineSeparator());
        return result;
    };
    
    private static void githubLatestReleaseTest() throws Exception {
        final Function<String, GithubLatestRelease> loader1 = (String url) -> (GithubLatestRelease) baseLoader.apply(GithubLatestRelease::new, url);
        final BiFunction<String, String, GithubLatestRelease> loader2 = (String author, String name) -> (GithubLatestRelease) baseLoader.apply(e -> new GithubLatestRelease(author, name), name);
        
        loader1.apply("https://github.com/yt-dlp/yt-dlp/releases");
        loader1.apply("https://github.com/yt-dlp/yt-dlp/releases/tag/2022.10.04");
        loader1.apply("https://github.com/ZGorlock/Java-Commons/");
        loader1.apply("https://github.com/cmdr2/stable-diffusion-ui/blob/main/media/config-v4.jpg");
        loader2.apply("cmdr2", "stable-diffusion-ui");
    }
    
    private static void githubRepoTest() throws Exception {
        final Function<String, GithubRepo> loader1 = (String url) -> (GithubRepo) baseLoader.apply(GithubRepo::new, url);
        final BiFunction<String, String, GithubRepo> loader2 = (String author, String name) -> (GithubRepo) baseLoader.apply(e -> new GithubRepo(author, name), name);
        
        loader1.apply("https://github.com/yt-dlp/yt-dlp");
        loader1.apply("https://github.com/yt-dlp/yt-dlp/issues");
        loader2.apply("cmdr2", "stable-diffusion-ui");
        loader1.apply("https://github.com/ZGorlock/Java-Commons/");
        loader1.apply("https://github.com/ZGorlock/Java-Commons/blob/main/src/commons/access/CmdLine.java");
        loader2.apply("ZGorlock", "Java-Commons");
    }
    
    private static void chromeAddonTest() throws Exception {
        final Function<String, ChromeAddon> loader = (String urlOrId) -> (ChromeAddon) baseLoader.apply(ChromeAddon::new, urlOrId);
        
        loader.apply("https://chrome.google.com/webstore/detail/cjpalhdlnbpafiamejdnhcphjbkeiagm/");
        loader.apply("cjpalhdlnbpafiamejdnhcphjbkeiagm");
        loader.apply("https://chrome.google.com/webstore/detail/mpbjkejclgfgadiemmefgebjfooflfhl?hl=en");
    }
    
    private static void firefoxAddonTest() throws Exception {
        final Function<String, FirefoxAddon> loader = (String urlOrId) -> (FirefoxAddon) baseLoader.apply(FirefoxAddon::new, urlOrId);
        
        loader.apply("https://addons.mozilla.org/en-US/firefox/addon/ublock-origin/");
        loader.apply("ublock-origin");
        loader.apply("https://addons.mozilla.org/en-US/firefox/addon/buster-captcha-solver/");
    }
    
    private static void redditSubredditTest() throws Exception {
        final Function<String, RedditSubreddit> loader = (String urlOrSubName) -> (RedditSubreddit) baseLoader.apply(RedditSubreddit::new, urlOrSubName);
        
        loader.apply("https://www.reddit.com/r/PassageWays/top/?t=month");
        loader.apply("PassageWays");
        loader.apply("https://www.reddit.com/r/actualfreakouts/");
        loader.apply("https://www.reddit.com/r/memes/");
    }
    
    private static void targetItemTest() throws Exception {
        final Function<String, TargetItem> loader = (String urlOrId) -> (TargetItem) baseLoader.apply(TargetItem::new, urlOrId);
        
        loader.apply("https://www.target.com/p/computer-desk-brushed-maple-sauder/-/A-51958292");
        loader.apply("-/A-51958292");
        loader.apply("https://www.target.com/p/3pk-stainless-steel-dinner-knives-room-essentials-8482/-/A-79467291#lnk=sametab");
        loader.apply("https://www.target.com/p/-/A-79467291");
    }
    
    private static void walmartItemTest() throws Exception {
        final Function<String, WalmartItem> loader = (String urlOrId) -> (WalmartItem) baseLoader.apply(WalmartItem::new, urlOrId);
        
        loader.apply("https://www.walmart.com/ip/Sony-77-Class-XR77A80J-BRAVIA-XR-OLED-4K-Ultra-HD-Smart-Google-TV-with-Dolby-Vision-HDR-A80J-Series-2021-Model/569988528");
        loader.apply("569988528");
        loader.apply("https://www.walmart.com/ip/Logitech-C920-Webcam-HD-Pro/776703861?th=1");
        loader.apply("https://www.walmart.com/ip/776703861");
    }
    
    private static void amazonItemTest() throws Exception {
        final Function<String, AmazonItem> loader = (String urlOrId) -> (AmazonItem) baseLoader.apply(AmazonItem::new, urlOrId);
        
        loader.apply("https://www.amazon.com/Seagate-Barracuda-Internal-Drive-Performance/dp/B01IA9H22Q/");
        loader.apply("B01IA9H22Q");
        loader.apply("https://www.amazon.com/Toms-Maine-Fluoride-Free-Antiplaque-Toothpaste/dp/B00JUJ1E0W?pd_rd_w=YkkHQ&content-id=amzn1.sym.deffa092-2e99-4e9f-b814-0d71c40b24af&pf_rd_p=deffa092-2e99-4e9f-b814-0d71c40b24af&pf_rd_r=F2DQ2PQ1J51TBDW7E84G&pd_rd_wg=oX9E1&pd_rd_r=4c1529c6-487a-4ae1-9ec8-e8ffe8d5d878&pd_rd_i=B00JUJ1E0W&ref_=pd_bap_d_rp_1_t&th=1");
        loader.apply("https://www.amazon.com/dp/B09MF26PV4?ref=ppx_yo2ov_dt_b_product_details&th=1");
        loader.apply("B07G568VWT");
        loader.apply("https://www.amazon.com/Microwave-Dishwasher-Lightweight-anti-fallen-Non-Toxic/dp/B07QVQ15Z3");
    }
    
}
