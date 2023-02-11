/*
 * File:    HalfLifeCalculator.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.awt.Desktop;
import java.io.File;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.util.concurrent.AtomicDouble;
import commons.access.CmdLine;
import commons.access.Filesystem;
import commons.access.OperatingSystem;
import commons.lambda.Action;
import commons.lambda.function.checked.CheckedRunnable;
import commons.lambda.function.unchecked.UncheckedFunction;
import commons.lambda.stream.collector.MapCollectors;
import commons.lambda.stream.mapper.Mappers;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import org.apache.commons.lang3.tuple.ImmutablePair;

public final class HalfLifeCalculator {
    
    //Constants
    
    private static final String TEST = "";
    
    private static final boolean DATA = true;
    
    private static final boolean BASE = true;
    
    private static final boolean ALT = false;
    
    private static final List<List<String>> ALT_DATA = List.of(
            List.of("~|10X", "~|5"),
            List.of("~|10")
    );
    
    private static final GraphMode GRAPH = GraphMode.FRAME;
    
    private static final boolean GRAPH_OPEN_AFTER = true;
    
    
    //Enums
    
    private enum GraphMode {
        OFF(false, 1, 0, 0, false),
        DETAIL(true, 576, 1, 1, true),      //1152
        FRAME(true, 360, 2, 2, true),       //1440
        RANGE(true, 288, 3, 3, true),       //1728
        TREND(true, 144, 5, 3, true),       //1152
        FORECAST(true, 360, 0, 3, false),   //1080
        BACKCAST(true, 360, 3, 0, false);   //1080
        
        final boolean enabled;
        
        final double graduation;
        
        final int prefix;
        
        final int suffix;
        
        final boolean divider;
        
        GraphMode(boolean enabled, int scale, int prefix, int suffix, boolean divider) {
            this.enabled = enabled;
            this.graduation = 1.0 / scale;
            this.prefix = prefix;
            this.suffix = suffix;
            this.divider = divider;
        }
        
    }
    
    
    //Static Fields
    
    private static final Instant test = TimeUtil.parse.apply(TEST);
    
    private static final Instant minBound = TimeUtil.addDays.apply(TimeUtil.localStartOfDay.apply(test), -30);
    
    private static final Instant maxBound = TimeUtil.addDays.apply(TimeUtil.localStartOfDay.apply(test), 30);
    
    private static final Map<String, List<Double>> species = FileUtil.loadSpecies.get();
    
    private static final List<ImmutablePair<String, List<ImmutablePair<Instant, Double>>>> dataSets = Stream.of(
                    Stream.of(new ImmutablePair<>("A", FileUtil.loadData.get())),
                    IntStream.range(0, ALT_DATA.size()).mapToObj(i -> new ImmutablePair<>(Character.toString('B' + i), FileUtil.loadAltData.apply(ALT_DATA.get(i)))),
                    Stream.of(new ImmutablePair<>("#", FileUtil.loadBaseData.get())))
            .flatMap(e -> e)
            .filter(Objects::nonNull).filter(e -> (e.getValue() != null))
            .collect(Collectors.toList());
    
    
    //Main Method
    
    public static void main(String[] args) {
        (GRAPH.enabled ? graph : run).accept(test);
    }
    
    
    //Static Functions
    
    private static final BiConsumer<String, Map<String, AtomicDouble>> printTotals = (String set, Map<String, AtomicDouble> totals) ->
            System.out.print(totals.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(total -> ((total.getKey().isEmpty() && (BASE || ALT)) ? set : total.getKey()) +
                            Optional.of(total.getValue()).map(AtomicDouble::get)
                                    .map(value -> Stream.of(value, (total.getKey().isEmpty() ? null : (value / (totals.get("").get() - value))))).orElse(Stream.empty())
                                    .filter(Objects::nonNull).map(new DecimalFormat("00.00")::format)
                                    .collect(Collectors.joining(" : ", ((!total.getKey().isEmpty() || (BASE || ALT)) ? " - " : ""), ""))
                    ).collect(Collectors.joining("  |  ", "", System.lineSeparator()))
                    .replaceAll("(?s)^\\s+$", ""));
    
    private static final BiFunction<Map.Entry<Instant, String>, Map.Entry<String, List<ImmutablePair<Instant, Double>>>, Double> calculateAt = (Map.Entry<Instant, String> test, Map.Entry<String, List<ImmutablePair<Instant, Double>>> dataSet) ->
            Optional.of(dataSet.getValue().stream()
                            .filter(data -> data.getKey().isBefore(test.getKey()))
                            .filter(data -> data.getKey().isAfter(minBound)).filter(data -> data.getKey().isBefore(maxBound))
                            .map(data -> Map.entry(((test.getKey().toEpochMilli() - data.getKey().toEpochMilli()) / (double) TimeUnit.HOURS.toMillis(1)), data.getValue()))
                            .reduce(species.keySet().stream().collect(MapCollectors.mapEachTo(() -> new AtomicDouble(0.0))),
                                    (totals, data) -> Mappers.perform(totals, endTotals ->
                                            species.forEach((speciesName, species) -> endTotals.get(speciesName).addAndGet(
                                                    ((0.80 * (data.getValue() * species.get(0)) * species.get(2)) /
                                                            (species.get(2) - species.get(1))) *
                                                            (Math.pow(Math.E, (-species.get(1) * data.getKey())) - Math.pow(Math.E, (-species.get(2) * data.getKey())))))),
                                    (totals, total) -> totals))
                    .map(Mappers.forEach(totals -> totals.put("", new AtomicDouble(totals.values().stream().mapToDouble(AtomicDouble::get).sum()))))
                    .map(Mappers.forEach(totals -> printTotals.accept(dataSet.getKey(), (test.getValue().equals("T") ? totals : Map.of()))))
                    .map(totals -> totals.get("")).map(AtomicDouble::get).orElse(0.0);
    
    private static final Consumer<Instant> graph = (Instant test) ->
            FileUtil.saveGraph.accept(Stream.concat(
                    Stream.of(Stream.concat(
                                    Stream.of("Time", "Label", (GRAPH.divider ? "~" : null)).filter(Objects::nonNull),
                                    dataSets.stream().filter(dataSet -> Objects.nonNull(dataSet.getValue())).map(Map.Entry::getKey)
                            ).map(StringUtility::quote),
                            Stream.of("")),
                    Stream.concat(
                                    IntStream.rangeClosed((int) -(GRAPH.prefix / GRAPH.graduation), (int) (GRAPH.suffix / GRAPH.graduation))
                                            .mapToObj(i -> Map.entry(TimeUtil.addDays.apply(test, (i * GRAPH.graduation)).toEpochMilli(), "")),
                                    IntStream.rangeClosed(-GRAPH.prefix, GRAPH.suffix)
                                            .mapToObj(i -> Map.entry(TimeUtil.addDays.apply(test, i).toEpochMilli(), ("T" + ((i != 0) ? (((i > 0) ? "+" : "") + i) : ""))))
                            ).sorted(Map.Entry.comparingByKey()).collect(MapCollectors.toMap(LinkedHashMap.class)).entrySet().stream()
                            .map(graphPoint -> Map.entry(TimeUtil.atMillis.apply(graphPoint.getKey()), graphPoint.getValue()))
                            .map(graphPoint -> Stream.concat(
                                    Stream.of(TimeUtil.print.apply(graphPoint.getKey()), graphPoint.getValue()).map(StringUtility::quote),
                                    Optional.of(dataSets.stream().filter(dataSet -> Objects.nonNull(dataSet.getValue()))
                                                    .map(dataSet -> calculateAt.apply(graphPoint, dataSet)).collect(Collectors.toList()))
                                            .map(e -> !GRAPH.divider ? e : ListUtility.addAndGet(e, 0, e.stream()
                                                    .filter(e2 -> !graphPoint.getValue().isEmpty())
                                                    .mapToDouble(e2 -> e2).max().orElse(0.0)))
                                            .stream().flatMap(Collection::stream)
                                            .map(String::valueOf)
                            ))
            ).map(e -> e.collect(Collectors.joining(","))).collect(Collectors.toList()));
    
    private static final Consumer<Instant> run = (Instant test) ->
            dataSets.stream().filter(dataSet -> Objects.nonNull(dataSet.getValue()))
                    .forEachOrdered(dataSet -> calculateAt.apply(Map.entry(test, "T"), dataSet));
    
    
    //Inner Classes
    
    private static class FileUtil {
        
        //Constants
        
        static final File DATA_DIR = new File("data");
        
        static final File DATA_FILE = new File(DATA_DIR, "data.txt");
        
        static final File SPECIES_FILE = new File(DATA_DIR, "species.txt");
        
        static final File SCHEDULE_FILE = new File(DATA_DIR, "schedule.txt");
        
        static final File OUTPUT_DIR = new File("output");
        
        static final File GRAPH_FILE = new File(OUTPUT_DIR, "graph.csv");
        
        
        //Static Functions
        
        static final Function<File, List<String>> readFile = Filesystem::readLines;
        
        static final BiConsumer<File, List<String>> writeFile = Filesystem::writeLines;
        
        static final Function<File, List<String>> readData = (File file) ->
                readFile.apply(file).stream()
                        .map(e -> e.replaceAll("(?://|#).*$", "")).map(String::strip)
                        .filter(e -> !e.isEmpty())
                        .collect(Collectors.toList());
        
        static final Function<List<String>, List<ImmutablePair<Instant, Double>>> parseData = (List<String> data) ->
                data.stream().filter(Objects::nonNull).filter(e -> !e.isBlank())
                        .map(e -> e.replace("~", (TimeUtil.format.apply(TimeUtil.now.get())) + "|"))
                        .map(e -> e.split("\\|+")).flatMap(e ->
                                IntStream.of(0, (e[1].contains("X") ? 4 : 0)).boxed().distinct()
                                        .map((UncheckedFunction<Integer, ImmutablePair<Instant, Double>>) delay ->
                                                new ImmutablePair<>(TimeUtil.addHours.apply(TimeUtil.parse.apply(e[0]), delay),
                                                        (Double.parseDouble(e[1].replace("X", "")) / (e[1].contains("X") ? 2.0 : 1.0)))))
                        .sorted(Comparator.comparing(o -> o.left)).collect(Collectors.toList());
        
        static final Supplier<Map<Double, String>> loadSchedule = () ->
                readData.apply(SCHEDULE_FILE).stream()
                        .map(e -> e.split(",")).collect(MapCollectors.toMap(LinkedHashMap.class,
                                e -> ((double) TimeUtil.parseTime.apply(e[0].strip()).toSecondOfDay() / TimeUnit.HOURS.toSeconds(1)),
                                e -> e[1].strip()));
        
        static final Supplier<List<ImmutablePair<Instant, Double>>> loadData = () ->
                !DATA ? null : parseData.apply(readData.apply(DATA_FILE));
        
        static final Function<List<String>, List<ImmutablePair<Instant, Double>>> loadAltData = (List<String> alt) ->
                (!ALT || alt.isEmpty()) ? null : parseData.apply(
                        Stream.concat(
                                alt.stream().map(e -> e.replace("~", TimeUtil.format.apply(TimeUtil.now.get()))),
                                readData.apply(DATA_FILE).stream()
                        ).collect(Collectors.toList()));
        
        static final Supplier<List<ImmutablePair<Instant, Double>>> loadBaseData = () ->
                !BASE ? null : parseData.apply(
                        Stream.iterate(minBound, e -> !e.isAfter(maxBound), e -> TimeUtil.addDays.apply(e, 1))
                                .flatMap(day -> loadSchedule.get().entrySet().stream()
                                        .map(e -> TimeUtil.format.apply(TimeUtil.addHours.apply(day, e.getKey())) + '|' + e.getValue()))
                                .collect(Collectors.toList()));
        
        static final Supplier<Map<String, List<Double>>> loadSpecies = () ->
                readData.apply(SPECIES_FILE).stream()
                        .map(e -> e.split(",")).collect(MapCollectors.toMap(LinkedHashMap.class,
                                e -> e[0].strip(),
                                e -> List.of(Double.parseDouble(e[1]), (Math.log(2) / Double.parseDouble(e[2])), (Double.parseDouble(e[3]) / 3.0))));
        
        static final Action openGraph = () ->
                Optional.of(new File("C:/Program Files/LibreOffice/program/scalc.exe"))
                        .filter(File::exists).filter(e -> OperatingSystem.isWindows())
                        .ifPresentOrElse(
                                libreCalcExe -> CmdLine.executeCmdAsync(String.join(" ",
                                        StringUtility.quote(libreCalcExe.getAbsolutePath()),
                                        "-o " + StringUtility.quote(GRAPH_FILE.getAbsolutePath()),
                                        "--infilter=" + StringUtility.quote("CSV:44,34,0,0,4/2/1/1"))),
                                (CheckedRunnable) () -> Desktop.getDesktop().open(GRAPH_FILE));
        
        static final Consumer<List<String>> saveGraph = (List<String> graphData) ->
                writeFile.andThen((graphFile, graphLines) ->
                                (GRAPH_OPEN_AFTER ? openGraph : Action.NULL).performQuietly())
                        .accept(GRAPH_FILE, graphData);
        
    }
    
    private static class TimeUtil {
        
        //Constants
        
        static final ZoneId UTC = ZoneOffset.UTC;
        
        static final ZoneId LOCAL = ZoneOffset.systemDefault();
        
        static final DateTimeFormatter STAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm").withZone(LOCAL);
        
        static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(LOCAL);
        
        static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm").withZone(LOCAL);
        
        static final DateTimeFormatter PRINT_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm").withZone(LOCAL);
        
        
        //Static Functions
        
        static final Supplier<Instant> now = Instant::now;
        
        static final Supplier<LocalDate> date = LocalDate::now;
        
        static final Supplier<LocalTime> time = LocalTime::now;
        
        static final Supplier<LocalDateTime> datetime = LocalDateTime::now;
        
        static final BiFunction<Instant, ZoneId, ZonedDateTime> toZone = ZonedDateTime::ofInstant;
        
        static final Function<Instant, ZonedDateTime> toUtc = (Instant instant) ->
                toZone.apply(instant, UTC);
        
        static final Function<Instant, ZonedDateTime> toLocal = (Instant instant) ->
                toZone.apply(instant, LOCAL);
        
        static final Function<Long, Instant> atMillis = Instant::ofEpochMilli;
        
        static final UncheckedFunction<String, Instant> parse = (String stamp) ->
                Optional.ofNullable(stamp).filter(e -> !e.isBlank())
                        .map(e -> STAMP_FORMAT.parse(stamp, Instant::from))
                        .orElseGet(now);
        
        static final Function<String, LocalDate> parseDate = (String dateStamp) ->
                Optional.ofNullable(dateStamp).filter(e -> !e.isBlank())
                        .map(e -> LocalDate.parse(e, DATE_FORMAT))
                        .orElseGet(date);
        
        static final Function<String, LocalTime> parseTime = (String timeStamp) ->
                Optional.ofNullable(timeStamp).filter(e -> !e.isBlank())
                        .map(e -> LocalTime.parse(e, TIME_FORMAT))
                        .orElseGet(time);
        
        static final UncheckedFunction<Instant, String> format = (Instant instant) ->
                STAMP_FORMAT.format(Optional.ofNullable(instant).orElseGet(now));
        
        static final UncheckedFunction<Instant, String> print = (Instant instant) ->
                PRINT_FORMAT.format(Optional.ofNullable(instant).orElseGet(now));
        
        static final BiFunction<Instant, Number, Instant> addMillis = (Instant instant, Number millis) ->
                instant.plus(millis.longValue(), ChronoUnit.MILLIS);
        
        static final BiFunction<Instant, Number, Instant> addHours = (Instant instant, Number hours) ->
                addMillis.apply(instant, (hours.doubleValue() * TimeUnit.HOURS.toMillis(1)));
        
        static final BiFunction<Instant, Number, Instant> addDays = (Instant instant, Number days) ->
                addMillis.apply(instant, (days.doubleValue() * TimeUnit.DAYS.toMillis(1)));
        
        static final UnaryOperator<Instant> startOfDay = (Instant instant) ->
                instant.truncatedTo(ChronoUnit.DAYS);
        
        static final UnaryOperator<Instant> endOfDay = (Instant instant) ->
                startOfDay.apply(addDays.apply(instant, 1));
        
        static final Supplier<Instant> startOfToday = () ->
                startOfDay.apply(now.get());
        
        static final Supplier<Instant> endOfToday = () ->
                endOfDay.apply(now.get());
        
        static final UnaryOperator<Instant> localStartOfDay = (Instant instant) ->
                ZonedDateTime.ofInstant(instant, LOCAL).toLocalDate().atStartOfDay(LOCAL).toInstant();
        
        static final UnaryOperator<Instant> localEndOfDay = (Instant instant) ->
                localStartOfDay.apply(addDays.apply(instant, 1));
        
        static final Supplier<Instant> localStartOfToday = () ->
                localStartOfDay.apply(now.get());
        
        static final Supplier<Instant> localEndOfToday = () ->
                localEndOfDay.apply(now.get());
        
    }
    
}
