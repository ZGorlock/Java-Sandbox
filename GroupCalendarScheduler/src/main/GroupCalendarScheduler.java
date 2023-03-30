/*
 * File:    GroupCalendarScheduler.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

public class GroupCalendarScheduler {
    
    //Constants
    
    private static final String APPLICATION_NAME = "GroupCalendarScheduler";
    
    private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport.Builder().build();
    
    private static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    
    private static List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    
    private static File SERVICE_ACCOUNT_KEY_FILE = new File("resources/dominica-341900-aa9fcdb8aa12.json");
    
    private static File CALENDARS_FILE = new File("data/calendars.txt");
    
    
    //Static Fields
    
    private static Calendar service;
    
    private static Map<String, List<String>> calendars;
    
    private static Map<String, List<Event>> events;
    
    private static String startTime = "2023-04-01";
    
    private static String endTime = "2023-05-01";
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        initService();
        loadCalendars();
        loadEvents();
        
        parseEvents();
        
        System.out.println("Done");
    }
    
    
    //Static Methods
    
    private static void parseEvents() {
        //TODO
    }
    
    private static Map<String, List<String>> loadCalendars() throws Exception {
        calendars = new LinkedHashMap<>();
        
        Files.readAllLines(CALENDARS_FILE.toPath()).stream()
                .map(e -> e.replaceAll("#.*$", ""))
                .filter(e -> !e.isBlank())
                .map(e -> e.split("\\|", -1))
                .forEachOrdered(line -> {
                    final String user = line[1].trim();
                    final String name = line[2].trim();
                    final String id = line[3].trim();
                    
                    calendars.putIfAbsent(user, new ArrayList<>());
                    calendars.get(user).add(id);
                });
        
        return calendars;
    }
    
    private static Map<String, List<Event>> loadEvents() throws Exception {
        events = new LinkedHashMap<>();
        
        for (List<String> userCalendars : calendars.values()) {
            for (String calendarId : userCalendars) {
                final List<Event> calendarEvents = getCalendarEvents(calendarId);
                
                events.putIfAbsent(calendarId, new ArrayList<>());
                events.get(calendarId).addAll(calendarEvents);
            }
        }
        
        return events;
    }
    
    private static List<Event> getCalendarEvents(String calendarId) {
        final List<Event> events = new ArrayList<>();
        
        String pageToken = null;
        do {
            try {
                final Events eventData = service.events().list(calendarId)
                        .setTimeMin(DateTime.parseRfc3339(startTime + "T00:00:00-04:00"))
                        .setTimeMax(DateTime.parseRfc3339(endTime + "T00:00:00-04:00"))
                        .setPageToken(pageToken)
                        .setMaxResults(50)
                        .execute();
                
                events.addAll(eventData.getItems());
                
                pageToken = eventData.getNextPageToken();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } while (pageToken != null);
        
        return events;
    }
    
    private static Calendar initService() throws Exception {
        service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize())
                .setApplicationName(APPLICATION_NAME)
                .build();
        return service;
    }
    
    @SuppressWarnings("UnnecessaryLocalVariable")
    private static Credential authorize() throws Exception {
        final GoogleCredential serviceKey = GoogleCredential
                .fromStream(new FileInputStream(SERVICE_ACCOUNT_KEY_FILE))
                .createScoped(SCOPES);

//        final GoogleCredential credential = new GoogleCredential.Builder()
//                .setTransport(readJsonFile.getTransport())
//                .setJsonFactory(readJsonFile.getJsonFactory())
//                .setServiceAccountId(readJsonFile.getServiceAccountId())
////                .setServiceAccountUser("zgorlock@gmail.com")
//                .setServiceAccountScopes(readJsonFile.getServiceAccountScopes())
//                .setServiceAccountPrivateKey(readJsonFile.getServiceAccountPrivateKey())
//                .build();
        
        return serviceKey;
    }
    
}
