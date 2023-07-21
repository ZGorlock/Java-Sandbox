/*
 * File:    WebDownloadUtil.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import commons.access.Filesystem;
import commons.access.Internet;
import commons.lambda.function.unchecked.UncheckedFunction;
import commons.lambda.stream.mapper.Mappers;
import org.jsoup.nodes.Node;

public final class WebDownloadUtil {
    
    public static String read(String url) {
        return Optional.ofNullable(url)
//                .filter(e -> e.matches("^.+\\.html(?:[?#].*)?$"))
                .map(Internet::getHtml).map(Node::toString)
                .orElse(null);
    }
    
    public static File download(String url, File download) {
        return Optional.ofNullable(url)
                .map(Mappers.forEach(e -> {
                    try {
                        Thread.sleep(10000);
                    } catch (Exception ex) {
                    }
                }))
                .filter(file -> Optional.ofNullable(download)
                        .map(Filesystem::isEmpty)
                        .orElse(true))
                .map(Mappers.forEach(System.out::println))
                .map(e -> Internet.downloadFile(e, download))
                .orElse(null);
    }
    
    public static File download(String url) {
        return Optional.ofNullable(url)
                .filter(e -> !e.isBlank())
                .map(WebDownloadUtil::getFileName)
                .map(fileName -> new File("tmp/", fileName))
                .map(file -> download(url, file))
                .orElse(null);
    }
    
    public static List<File> download(List<String> urlList, File downloadDir) {
        return Optional.ofNullable(downloadDir)
//                .filter(Filesystem::isEmpty).filter(Filesystem::createDirectory)
                .map(outputDir -> Optional.ofNullable(urlList)
                        .stream().flatMap(Collection::stream)
                        .map(urlEntry -> Optional.ofNullable(urlEntry)
                                .filter(url -> !url.isBlank())
                                .map(WebDownloadUtil::getFileName)
                                .map(fileName -> new File(outputDir, fileName))
                                .map(file -> download(urlEntry, file))
                                .orElse(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .orElse(null);
    }
    
    public static List<File> download(File urlList, File downloadDir) {
        return Optional.ofNullable(urlList)
                .map(Filesystem::readLines)
                .map(lines -> download(lines, downloadDir))
                .orElse(null);
    }
    
    public static String getFileName(String url) {
        return Optional.ofNullable(url)
                .map(URI::create)
                .map((UncheckedFunction<URI, URL>) URI::toURL)
                .map(URL::getFile)
                .orElseGet(() -> UUID.randomUUID().toString());
    }
    
    public static String getBaseFileName(String url) {
        return Optional.ofNullable(url)
                .map(WebDownloadUtil::getFileName)
                .map(e -> e.replaceAll("^.+/([^/]+)$", "$1"))
                .orElseGet(() -> UUID.randomUUID().toString());
    }
    
}
