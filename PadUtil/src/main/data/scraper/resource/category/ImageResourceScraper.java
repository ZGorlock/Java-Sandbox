/*
 * File:    ImageResourceScraper.java
 * Package: main.data.scraper.resource.category
 * Author:  Zachary Gill
 */

package main.data.scraper.resource.category;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

import commons.lambda.function.checked.CheckedFunction;
import main.data.scraper.resource.ResourceScraper;

public class ImageResourceScraper extends ResourceScraper<BufferedImage> {
    
    //Constants
    
    public static final Pattern URL_PATTERN_IMAGE = Pattern.compile("(?<url>(?<host>" + Pattern.quote(URL_ROOT_RESOURCE) + ")" + "(?<location>(?<path>[\\w\\-/]*)/(?<name>[\\w\\-]+\\.\\w+)))");
    
    public static final Pattern URL_EXTRACTOR_PATTERN_IMAGE = Pattern.compile("(?<=\")" + URL_PATTERN_IMAGE.pattern() + "(?=\")");
    
    public static final String CONTENT_TYPE_IMAGE = "png";
    
    
    //Methods
    
    @Override
    public Optional<List<String>> tryEnumerateUrls() {
        return super.tryEnumerateUrls()
                .map(urlList -> urlList.stream()
                        .filter(url -> !url.matches("(?i).*/None\\.[^.]+"))
                        .collect(Collectors.toList()));
    }
    
    @Override
    public Optional<BufferedImage> tryScrapeData(String url) {
        return Optional.of(url)
                .stream().flatMap(baseUrl -> Stream.of(baseUrl.replaceAll("\\.[^.]+$", ".png"), baseUrl))
                .distinct().map(super::tryScrapeData)
                .filter(Optional::isPresent).map(Optional::get)
                .findFirst();
    }
    
    @Override
    protected Optional<BufferedImage> tryDownloadData(String url) {
        return Optional.of(url)
                .map(URI::create).map((CheckedFunction<URI, URL>) URI::toURL)
                .map((CheckedFunction<URL, BufferedImage>) ImageIO::read);
    }
    
    @Override
    protected Optional<Boolean> trySaveData(File file, BufferedImage data) {
        return Optional.ofNullable(data)
                .flatMap(fileData -> Optional.ofNullable(file)
                        .map((CheckedFunction<File, Boolean>) outputFile -> ImageIO.write(fileData, getContentType(), outputFile)));
    }
    
    
    //Getters
    
    @Override
    protected String getTypeName(boolean plural) {
        return "image" + (plural ? "s" : "");
    }
    
    @Override
    protected Pattern getUrlPattern() {
        return URL_PATTERN_IMAGE;
    }
    
    @Override
    protected Pattern getUrlExtractorPattern() {
        return URL_EXTRACTOR_PATTERN_IMAGE;
    }
    
    @Override
    protected String getContentType() {
        return CONTENT_TYPE_IMAGE;
    }
    
}
