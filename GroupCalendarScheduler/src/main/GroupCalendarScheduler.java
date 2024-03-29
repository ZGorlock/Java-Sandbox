/*
 * File:    GroupCalendarScheduler.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    
    private static File SA_USER_FILE = new File("data/sa-user.txt");
    
    private static Long TIMEZONE_OFFSET = TimeUnit.SECONDS.toHours(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()).getTotalSeconds());
    
    private static ZoneId ZONE_ID = ZoneOffset.ofHours(TIMEZONE_OFFSET.intValue());
    
    private static final int INTERVAL = 30;
    
    private static final long INTERVAL_MS = TimeUnit.MINUTES.toMillis(INTERVAL);
    
    private static final long INTERVAL_NS = TimeUnit.MILLISECONDS.toNanos(INTERVAL_MS);
    
    private static final int INTERVALS_PER_HOUR = (int) TimeUnit.HOURS.toMinutes(1) / INTERVAL;
    
    private static final int INTERVALS_PER_DAY = (int) TimeUnit.DAYS.toMinutes(1) / INTERVAL;
    
    
    //Static Fields
    
    private static Calendar service;
    
    private static Map<String, List<String>> calendars;
    
    private static Map<String, List<Event>> events;
    
    private static List<EventRange> eventRanges;
    
    private static List<Slot> availableSlots;
    
    private static final int minimumSlot = 4 * INTERVALS_PER_HOUR; //4 hours
    
    private static final int slotBorder = INTERVALS_PER_HOUR / 2; //30 minutes
    
    private static final boolean checkWeekdays = true;
    
    private static final boolean checkWeekends = true;
    
    private static final List<DayOfWeek> daysAllowed = Stream.of(
            (checkWeekdays ? DayOfWeek.MONDAY : null),
            (checkWeekdays ? DayOfWeek.TUESDAY : null),
            (checkWeekdays ? DayOfWeek.WEDNESDAY : null),
            (checkWeekdays ? DayOfWeek.THURSDAY : null),
            (checkWeekdays ? DayOfWeek.FRIDAY : null),
            (checkWeekends ? DayOfWeek.SATURDAY : null),
            (checkWeekends ? DayOfWeek.SUNDAY : null)
    ).filter(Objects::nonNull).collect(Collectors.toList());
    
    private static final int earliestTime = 9 * INTERVALS_PER_HOUR; //9:00 AM
    
    private static final int latestTime = 22 * INTERVALS_PER_HOUR; //10:00 PM
    
    private static final TemporalAmount searchRange = Period.ofMonths(1);
    
    //private static final String startDate = "2024-03-01";
    private static final String startDate = LocalDate.now(ZONE_ID).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    
    private static final String endDate = "2024-05-01";
    //private static final String endDate = LocalDate.now(ZONE_ID).atStartOfDay().plus(searchRange).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    
    private static final DateTime startTime = DateTime.parseRfc3339(startDate + "T00:00:00" + ZONE_ID.getId());
    
    private static final DateTime endTime = DateTime.parseRfc3339(endDate + "T00:00:00" + ZONE_ID.getId());
    
    private static final boolean printSlots = true;
    
    private static final boolean printConfig = true;
    
    private static final boolean printWeekSeparators = true;
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        init();
        loadCalendars();
        loadEvents();
        
        calculateEventRanges();
        findAvailableSlots();
        
        print();
    }
    
    
    //Static Methods
    
    private static Calendar init() throws Exception {
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
        
        //final GoogleCredential credential = new GoogleCredential.Builder()
        //        .setTransport(readJsonFile.getTransport())
        //        .setJsonFactory(readJsonFile.getJsonFactory())
        //        .setServiceAccountId(readJsonFile.getServiceAccountId())
        //        //.setServiceAccountUser(Files.readString(SA_USER_FILE.toPath()).strip())
        //        .setServiceAccountScopes(readJsonFile.getServiceAccountScopes())
        //        .setServiceAccountPrivateKey(readJsonFile.getServiceAccountPrivateKey())
        //        .build();
        
        return serviceKey;
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
                final List<Event> calendarEvents = getCalendarEvents(calendarId, true);
                
                events.putIfAbsent(calendarId, new ArrayList<>());
                events.get(calendarId).addAll(calendarEvents);
            }
        }
        
        return events;
    }
    
    private static List<Event> getCalendarEvents(String calendarId, boolean getInstances) {
        final List<Event> events = new ArrayList<>();
        
        String pageToken = null;
        do {
            try {
                final Events eventsResponse = service.events().list(calendarId)
                        .setTimeMin(startTime).setTimeMax(endTime)
                        .setPageToken(pageToken).setMaxResults(2500)
                        .execute();
                
                eventsResponse.getItems().stream()
                        .map(e -> (!getInstances || (e.getRecurrence() == null)) ? List.of(e) :
                                  getEventInstances(calendarId, e.getId()))
                        .flatMap(Collection::stream)
                        .forEachOrdered(events::add);
                
                pageToken = eventsResponse.getNextPageToken();
            } catch (Exception e) {
                throw new RuntimeException(("Error: " + calendarId), e);
            }
        } while (pageToken != null);
        
        return events;
    }
    
    private static List<Event> getCalendarEvents(String calendarId) {
        return getCalendarEvents(calendarId, false);
    }
    
    private static List<Event> getEventInstances(String calendarId, String eventId) {
        final List<Event> instances = new ArrayList<>();
        
        String pageToken = null;
        do {
            try {
                final Events instancesResponse = service.events().instances(calendarId, eventId)
                        .setTimeMin(startTime).setTimeMax(endTime)
                        .setPageToken(pageToken).setMaxResults(2500)
                        .execute();
                
                instances.addAll(instancesResponse.getItems());
                
                pageToken = instancesResponse.getNextPageToken();
            } catch (Exception e) {
                throw new RuntimeException(("Error: " + calendarId), e);
            }
        } while (pageToken != null);
        
        return instances;
    }
    
    private static List<EventRange> calculateEventRanges() {
        eventRanges = new ArrayList<>();
        
        for (List<Event> calendarEvents : events.values()) {
            for (Event event : calendarEvents) {
                final EventRange eventRange = new EventRange(event);
                
                eventRanges.add(eventRange);
            }
        }
        
        return eventRanges;
    }
    
    private static List<Slot> findAvailableSlots() {
        availableSlots = new ArrayList<>();
        
        Slot currentSlot = null;
        for (long pointer = startTime.getValue(); pointer < endTime.getValue(); pointer += INTERVAL_MS) {
            final DateTime point = new DateTime(pointer);
            final Slot interval = new Slot(point);
            
            if (interval.isValid() && interval.isAvailable()) {
                if (currentSlot != null) {
                    currentSlot.extend(interval);
                } else {
                    currentSlot = interval;
                }
            } else {
                if (currentSlot != null) {
                    if (currentSlot.isValidLength()) {
                        availableSlots.add(currentSlot);
                    }
                    currentSlot = null;
                }
            }
        }
        if ((currentSlot != null) && currentSlot.isValidLength()) {
            availableSlots.add(currentSlot);
        }
        
        return availableSlots;
    }
    
    private static void print() {
        if (!printSlots) {
            return;
        }
        
        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEE MM/dd/yyyy").withZone(ZONE_ID);
        final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm").withZone(ZONE_ID);
        final DateTimeFormatter weekFormat = DateTimeFormatter.ofPattern("w").withZone(ZONE_ID);
        
        System.out.println("┌" + "─".repeat(48) + "┐");
        
        int currentWeek = 0;
        
        for (Slot slot : availableSlots) {
            final Instant start = Instant.ofEpochMilli(slot.start.getValue());
            final Instant end = Instant.ofEpochMilli(slot.end.getValue());
            
            if (printWeekSeparators) {
                final DayOfWeek day = start.atZone(ZONE_ID).getDayOfWeek();
                final int week = Integer.parseInt(weekFormat.format(start)) - ((day == DayOfWeek.SUNDAY) ? 1 : 0);
                if ((week != currentWeek) && (currentWeek > 0)) {
                    System.out.printf("│ %s   |   %s   |  %s │%n", "-".repeat(14), "-".repeat(14), "-".repeat(5));
                }
                currentWeek = week;
            }
            
            System.out.printf("│ %s   |   %s -> %s   |  %4.1fh │%n",
                    dateFormat.format(start), timeFormat.format(start), timeFormat.format(end), slot.getHours());
        }
        
        System.out.println("└" + "─".repeat(48) + "┘");
        
        printConf();
    }
    
    private static void printConf() {
        if (!printConfig) {
            return;
        }
        
        final Function<Integer, String> durationStringBuilder = (Integer minutes) ->
                Stream.of(
                        Optional.ofNullable(minutes).map(e -> (e / 60)).filter(e -> (e > 0)).map(String::valueOf).map(e -> (e + "hr")).orElse(null),
                        Optional.ofNullable(minutes).map(e -> (e % 60)).filter(e -> (e > 0)).map(String::valueOf).map(e -> (e + "min")).orElse(null)
                ).filter(Objects::nonNull).collect(Collectors.joining(" "));
        
        System.out.println("Start:      " + startDate);
        System.out.println("End:        " + endDate);
        System.out.println("Check:      " + daysAllowed.stream().map(day -> day.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())).collect(Collectors.joining(" ")));
        System.out.println("Earliest:   " + LocalTime.ofNanoOfDay(earliestTime * INTERVAL_NS));
        System.out.println("Latest:     " + LocalTime.ofNanoOfDay(latestTime * INTERVAL_NS));
        System.out.println("Require:    " + durationStringBuilder.apply(minimumSlot * INTERVAL));
        System.out.println("Buffer:     " + durationStringBuilder.apply(slotBorder * INTERVAL));
        System.out.println("Resolution: " + durationStringBuilder.apply(INTERVAL));
    }
    
    
    //Inner Classes
    
    private static class Interval {
        
        //Fields
        
        public DateTime start;
        
        public DateTime end;
        
        
        //Constructors
        
        public Interval(DateTime start, DateTime end) {
            this.start = start;
            this.end = end;
        }
        
        
        //Methods
        
        public void extend(DateTime time) {
            this.start = startsAfter(time) ? time : this.start;
            this.end = endsBefore(time) ? time : this.end;
        }
        
        public void extend(Interval interval) {
            if (!touches(interval)) {
                throw new UnsupportedOperationException();
            }
            extend(interval.start);
            extend(interval.end);
        }
        
        public boolean encloses(DateTime time) {
            return (start.getValue() < time.getValue()) &&
                    (end.getValue() > time.getValue());
        }
        
        public boolean encloses(Interval interval) {
            return encloses(interval.start) && encloses(interval.end);
        }
        
        public boolean overlaps(Interval interval) {
            return encloses(interval.start) || encloses(interval.end);
        }
        
        public boolean touches(Interval interval) {
            return (start.getValue() == interval.end.getValue()) ||
                    (end.getValue() == interval.start.getValue());
        }
        
        public boolean endsBefore(DateTime time) {
            return (end.getValue() <= time.getValue());
        }
        
        public boolean endsBefore(Interval interval) {
            return endsBefore(interval.start);
        }
        
        public boolean startsAfter(DateTime time) {
            return (start.getValue() >= time.getValue());
        }
        
        public boolean startsAfter(Interval interval) {
            return startsAfter(interval.end);
        }
        
    }
    
    private static class Slot extends Interval {
        
        //Constructors
        
        public Slot(DateTime start) {
            super(start, new DateTime(start.getValue() + INTERVAL_MS));
        }
        
        
        //Methods
        
        private boolean isTimeValid(DateTime test, boolean start) {
            final Instant instant = Instant.ofEpochMilli(test.getValue());
            final ZonedDateTime time = ZonedDateTime.ofInstant(instant, ZONE_ID);
            final Instant startOfDay = time.plus((start ? 1 : -1), ChronoUnit.MILLIS).toLocalDate().atStartOfDay(ZONE_ID).toInstant();
            final Instant earliest = startOfDay.plus((earliestTime * INTERVAL_MS), ChronoUnit.MILLIS);
            final Instant latest = startOfDay.plus((latestTime * INTERVAL_MS), ChronoUnit.MILLIS);
            
            return isDayValid(time) && !instant.isBefore(earliest) && !instant.isAfter(latest);
        }
        
        private boolean isDayValid(ZonedDateTime time) {
            return daysAllowed.contains(time.getDayOfWeek());
        }
        
        public boolean isStartTimeValid() {
            return isTimeValid(start, true);
        }
        
        public boolean isEndTimeValid() {
            return isTimeValid(end, false);
        }
        
        public boolean isValid() {
            return isStartTimeValid() && isEndTimeValid();
        }
        
        public boolean isValidLength() {
            return getDuration() >= ((minimumSlot + (2L * slotBorder)) * INTERVAL_MS);
        }
        
        public boolean isAvailable() {
            return eventRanges.stream().noneMatch(e -> e.blocks(this));
        }
        
        public long getDuration() {
            return end.getValue() - start.getValue();
        }
        
        public double getHours() {
            return getDuration() / (double) TimeUnit.HOURS.toMillis(1);
        }
        
    }
    
    private static class EventRange extends Interval {
        
        //Fields
        
        public final Event event;
        
        public final boolean busy;
        
        
        //Constructors
        
        public EventRange(Event event) {
            super(Optional.ofNullable(event).map(Event::getStart).map(e -> e.containsKey("dateTime") ? e.getDateTime() : e.getDate()).orElse(null),
                    Optional.ofNullable(event).map(Event::getEnd).map(e -> e.containsKey("dateTime") ? e.getDateTime() : e.getDate()).orElse(null));
            
            this.event = event;
            this.busy = Optional.ofNullable(event).map(Event::getTransparency).map(e -> !e.equalsIgnoreCase("transparent")).orElse(true);
        }
        
        
        //Methods
        
        public boolean blocks(Interval interval) {
            return busy && overlaps(interval);
        }
        
    }
    
}
