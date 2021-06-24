/*
 * File:    Entomic.java
 * Package: Entomic
 * Author:  Zachary Gill
 */

package Entomic;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Entomic {
    
    static final String testJson = "[{\"name\":\"Spain\",\"topLevelDomain\":[\".es\"],\"alpha2Code\":\"ES\",\"alpha3Code\":\"ESP\",\"callingCodes\":[\"34\"],\"capital\":\"Madrid\",\"altSpellings\":[\"ES\",\"Kingdom of Spain\",\"Reino de España\"],\"region\":\"Europe\",\"subregion\":\"Southern Europe\",\"population\":46438422,\"latlng\":[40.0,-4.0],\"demonym\":\"Spanish\",\"area\":505992.0,\"gini\":34.7,\"timezones\":[\"UTC\",\"UTC+01:00\"],\"borders\":[\"AND\",\"FRA\",\"GIB\",\"PRT\",\"MAR\"],\"nativeName\":\"España\",\"numericCode\":\"724\",\"currencies\":[{\"code\":\"EUR\",\"name\":\"Euro\",\"symbol\":\"€\"}],\"languages\":[{\"iso639_1\":\"es\",\"iso639_2\":\"spa\",\"name\":\"Spanish\",\"nativeName\":\"Español\"}],\"translations\":{\"de\":\"Spanien\",\"es\":\"España\",\"fr\":\"Espagne\",\"ja\":\"スペイン\",\"it\":\"Spagna\",\"br\":\"Espanha\",\"pt\":\"Espanha\",\"nl\":\"Spanje\",\"hr\":\"Španjolska\",\"fa\":\"اسپانیا\"},\"flag\":\"https://restcountries.eu/data/esp.svg\",\"regionalBlocs\":[{\"acronym\":\"EU\",\"name\":\"European Union\",\"otherAcronyms\":[],\"otherNames\":[]}],\"cioc\":\"ESP\"}]";
    
    public static void main(String[] args) throws Exception {
        System.out.println(parseJSON(testJson));
    }
    
    protected static String parseJSON(String jsonString) throws Exception {
        if (jsonString == null || jsonString.trim().length() == 0) {
            return "empty Object";
        }
        
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) ((JSONArray) parser.parse(jsonString)).get(0);
        JSONObject language = (JSONObject) ((JSONArray) jsonObject.get("languages")).get(0);
        return (String) language.get("name");
    }
    
}
