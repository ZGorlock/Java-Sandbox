/*
 * File:    MarketApi.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import commons.access.Filesystem;
import commons.access.Project;
import commons.lambda.function.checked.CheckedFunction;
import commons.lambda.function.checked.CheckedSupplier;
import commons.lambda.stream.collector.MapCollectors;
import commons.object.string.StringUtility;
import main.entity.Entity;
import main.entity.EodData;
import main.entity.Exchange;
import main.entity.Ticker;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("JavadocLinkAsPlainText")
public class MarketApi {
    
    //Constants
    
    public static final File API_KEY_FILE = new File(Project.RESOURCES_DIR, "apiKey");
    
    private static final String API_KEY;
    
    static {
        try {
            API_KEY = Filesystem.readFileToString(API_KEY_FILE).strip();
            if (API_KEY.isEmpty()) {
                throw new KeyException();
            }
        } catch (Exception e) {
            throw new RuntimeException("Must supply an EODHD API key" + API_KEY_FILE.getAbsolutePath(), e);
        }
    }
    
    private static final String REQUEST_BASE = "https://eodhistoricaldata.com/api";
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private static final File CACHE_DIR = new File(Project.DATA_DIR, "api");
    
    
    //Enums
    
    public enum Endpoint {
        
        //Values
        
        EXCHANGES_LIST,
        EXCHANGE_SYMBOL_LIST,
        EOD;
        
        
        //Fields
        
        public final String name;
        
        
        //Constructors
        
        Endpoint() {
            this.name = name().toLowerCase().replace("_", "-");
        }
        
        
        //Methods
        
        public File getCacheFile(String... parameters) {
            return new File(CACHE_DIR, Stream.concat(Stream.of(name), Arrays.stream(parameters))
                    .filter(e -> !StringUtility.isNullOrBlank(e))
                    .collect(Collectors.joining("_", "", ".json")));
        }
        
        @SuppressWarnings("unchecked")
        public List<Map<String, Object>> getCache(String... parameters) {
            return Optional.of(getCacheFile(parameters)).map(Filesystem::readFileToString)
                    .map((CheckedFunction<String, List<Map<String, Object>>>) e ->
                            (JSONArray) new JSONParser().parse(e))
                    .orElse(null);
        }
        
        public List<Map<String, Object>> saveCache(List<Map<String, Object>> data, String... parameters) {
            Optional.ofNullable(data).ifPresent(e -> Filesystem.writeStringToFile(
                    getCacheFile(parameters),
                    OBJECT_MAPPER.valueToTree(data).toPrettyString()));
            return data;
        }
        
    }
    
    
    //Static Methods
    
    /**
     * https://eodhistoricaldata.com/financial-apis/exchanges-api-list-of-tickers-and-trading-hours/
     */
    public static Map<String, Exchange> listExchanges() throws Exception {
        return Optional.ofNullable(Endpoint.EXCHANGES_LIST.getCache())
                .orElseGet((CheckedSupplier<List<Map<String, Object>>>) () ->
                        Optional.ofNullable(ApiHandler.callApi(
                                        Endpoint.EXCHANGES_LIST.name, new HashMap<>(Map.of())))
                                .map(Endpoint.EXCHANGES_LIST::saveCache)
                                .orElse(List.of()))
                .stream()
                .map(Exchange::new)
                .sorted()
                .collect(MapCollectors.toLinkedHashMap(
                        Entity::getIdentifier, e -> e));
    }
    
    /**
     * https://eodhistoricaldata.com/financial-apis/exchanges-api-list-of-tickers-and-trading-hours/
     */
    public static Map<String, Ticker> listTickers(String exchangeCode, boolean delisted) throws Exception {
        return Optional.ofNullable(Endpoint.EXCHANGE_SYMBOL_LIST.getCache(
                        exchangeCode, (delisted ? "delisted" : null)))
                .orElseGet((CheckedSupplier<List<Map<String, Object>>>) () ->
                        Optional.ofNullable(ApiHandler.callApi(
                                        String.join("/", Endpoint.EXCHANGE_SYMBOL_LIST.name, exchangeCode),
                                        new HashMap<>(delisted ? Map.of("delisted", "1") : Map.of())))
                                .map(e -> Endpoint.EXCHANGE_SYMBOL_LIST.saveCache(
                                        e, exchangeCode, (delisted ? "delisted" : null)))
                                .orElse(List.of()))
                .stream()
                .map(Ticker::new)
                .sorted()
                .collect(MapCollectors.toLinkedHashMap(
                        Entity::getIdentifier, e -> e));
    }
    
    public static Map<String, Ticker> listTickers(String exchangeCode) throws Exception {
        return listTickers(exchangeCode, false);
    }
    
    public static Map<String, Ticker> listTickers() throws Exception {
        return listTickers("US");
    }
    
    /**
     * https://eodhistoricaldata.com/financial-apis/api-for-historical-data-and-volumes/
     */
    public static Map<String, EodData> eodData(String exchangeCode, String tickerCode) throws Exception {
        return Optional.ofNullable(Endpoint.EOD.getCache(
                        exchangeCode, tickerCode, Entity.dateString.apply(new Date())))
                .orElseGet((CheckedSupplier<List<Map<String, Object>>>) () ->
                        Optional.ofNullable(ApiHandler.callApi(
                                        String.join("/", Endpoint.EOD.name, (tickerCode + '.' + exchangeCode)),
                                        new HashMap<>(Map.of())))
                                .map(e -> Endpoint.EOD.saveCache(
                                        e, exchangeCode, tickerCode, Entity.dateString.apply(new Date())))
                                .orElse(List.of()))
                .stream()
                .map(EodData::new)
                .sorted(Comparator.reverseOrder())
                .collect(MapCollectors.toLinkedHashMap(
                        Entity::getIdentifier, e -> e));
    }
    
    public static Map<String, EodData> eodData(String tickerCode) throws Exception {
        return eodData("US", tickerCode);
    }
    
    //Inner Classes
    
    private static class ApiHandler {
        
        //Static Fields
        
        private static final CloseableHttpClient httpClient = HttpClients.createDefault();
        
        
        //Static Methods
        
        public static List<Map<String, Object>> callApi(String endpoint, Map<String, String> parameters) throws Exception {
            final HttpGet request = buildApiRequest(endpoint, new HashMap<>(parameters));
            
            try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
                return parseResponse(EntityUtils.toString(httpResponse.getEntity()).strip());
            }
        }
        
        public static List<Map<String, Object>> callApi(String endpoint) throws Exception {
            return callApi(endpoint, new HashMap<>());
        }
        
        private static HttpGet buildApiRequest(String endpoint, Map<String, String> parameters) throws Exception {
            parameters.putIfAbsent("fmt", "json");
            parameters.putIfAbsent("api_token", API_KEY);
            
            final HttpGet request = new HttpGet(buildApiUrl(endpoint, parameters));
            request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");
            return request;
        }
        
        private static String buildApiUrl(String endpoint, Map<String, String> parameters) throws Exception {
            return String.join("/", REQUEST_BASE, endpoint).replaceAll("/*$", "/") +
                    buildApiParameterString(parameters);
        }
        
        private static String buildApiParameterString(Map<String, String> parameters) throws Exception {
            return parameters.entrySet().stream()
                    .map(parameter -> String.join("=",
                            URLEncoder.encode(parameter.getKey(), StandardCharsets.UTF_8),
                            URLEncoder.encode(parameter.getValue(), StandardCharsets.UTF_8)))
                    .collect(Collectors.joining("&", "?", ""));
        }
        
        @SuppressWarnings("unchecked")
        private static List<Map<String, Object>> parseResponse(String response) throws Exception {
            return (JSONArray) new JSONParser().parse(response);
        }
        
    }
    
}
