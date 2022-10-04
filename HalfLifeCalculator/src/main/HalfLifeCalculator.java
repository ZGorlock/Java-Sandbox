/*
 * File:    HalfLifeCalculator.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.awt.Desktop;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
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
import commons.lambda.function.unchecked.UncheckedFunction;
import commons.lambda.stream.collector.MapCollectors;
import commons.lambda.stream.mapper.Mappers;
import commons.object.string.StringUtility;
import org.apache.commons.lang3.tuple.ImmutablePair;

public final class HalfLifeCalculator {
    
    //Constants
    
    private static final String MEASURE = "";
    
    private static final boolean COMPARE = true;
    
    private static final boolean GRAPH = true;
    
    private static final int GRAPH_START_OFFSET_DAYS = 4;
    
    private static final int GRAPH_END_OFFSET_DAYS = 3;
    
    private static final double GRAPH_GRADUATION_DAYS = 1.0 / 72.0;
    
    private static final boolean GRAPH_OPEN_AFTER = true;
    
    
    //Static Fields
    
    private static final List<ImmutablePair<String, List<ImmutablePair<Instant, Double>>>> dataSets = List.of(
            new ImmutablePair<>("A", FileUtil.loadData.get()),
            new ImmutablePair<>("B", FileUtil.loadComparison.get()));
    
    private static final Map<String, List<Double>> components = FileUtil.loadComponents.get();
    
    
    //Main Method
    
    @SuppressWarnings("ConstantConditions")
    public static void main(String[] args) throws Exception {
        final Instant test = MEASURE.isEmpty() ? TimeUtil.now.get() : TimeUtil.parse.apply(MEASURE);
        
        if (GRAPH) {
            graph(test);
        } else {
            run(test);
        }
    }
    
    
    //Static Methods
    
    private static void run(Instant test) throws Exception {
        dataSets.stream().filter(dataSet -> Objects.nonNull(dataSet.getValue())).forEachOrdered(dataSet ->
                calculateAt(test, dataSet, true));
    }
    
    private static void graph(Instant test) {
        FileUtil.saveGraph.accept(Stream.concat(
                Stream.of(Stream.concat(
                                Stream.of("Time", "Label"),
                                dataSets.stream().filter(dataSet -> Objects.nonNull(dataSet.getValue())).map(Map.Entry::getKey)
                        ).map(StringUtility::quote),
                        Stream.of("")),
                Stream.concat(
                                IntStream.rangeClosed((int) -(GRAPH_START_OFFSET_DAYS / GRAPH_GRADUATION_DAYS), (int) (GRAPH_END_OFFSET_DAYS / GRAPH_GRADUATION_DAYS))
                                        .mapToObj(i -> Map.entry(TimeUtil.addDays.apply(test, (i * GRAPH_GRADUATION_DAYS)).toEpochMilli(), "")),
                                IntStream.rangeClosed(-GRAPH_START_OFFSET_DAYS, GRAPH_END_OFFSET_DAYS)
                                        .mapToObj(i -> Map.entry(TimeUtil.addDays.apply(test, i).toEpochMilli(), ("T" + ((i != 0) ? (((i > 0) ? "+" : "") + i) : ""))))
                        ).sorted(Map.Entry.comparingByKey()).collect(MapCollectors.toMap(LinkedHashMap.class)).entrySet().stream()
                        .map(graphPoint -> Map.entry(TimeUtil.atMillis.apply(graphPoint.getKey()), graphPoint.getValue()))
                        .map(graphPoint -> Stream.concat(
                                Stream.of(TimeUtil.formatPrint.apply(graphPoint.getKey()), graphPoint.getValue()).map(StringUtility::quote),
                                dataSets.stream().filter(dataSet -> Objects.nonNull(dataSet.getValue()))
                                        .map(dataSet -> calculateAt(graphPoint.getKey(), dataSet, (graphPoint.getValue().equals("T")))).map(String::valueOf)
                        ))
        ).map(e -> e.collect(Collectors.joining(","))).collect(Collectors.toList()));
        if (GRAPH_OPEN_AFTER) {
            FileUtil.openGraph.performQuietly();
        }
    }
    
    private static double calculateAt(Instant test, Map.Entry<String, List<ImmutablePair<Instant, Double>>> dataSet, boolean print) {
        final Map<String, AtomicDouble> totals = components.keySet().stream()
                .collect(MapCollectors.mapEachTo(() -> new AtomicDouble(0.0)));
        
        dataSet.getValue().stream().filter(data -> data.getKey().isBefore(test)).forEach(data ->
                totals.forEach((component, total) -> {
                    final double time = (test.toEpochMilli() - data.getKey().toEpochMilli()) / (double) TimeUnit.HOURS.toMillis(1);
                    total.addAndGet(((0.80 * (data.getValue() * components.get(component).get(0)) * components.get(component).get(2)) /
                            (components.get(component).get(2) - components.get(component).get(1))) *
                            (Math.pow(Math.E, (-components.get(component).get(1) * time)) - Math.pow(Math.E, (-components.get(component).get(2) * time))));
                }));
        
        try {
            return totals.values().stream().mapToDouble(AtomicDouble::get).sum();
        } finally {
            if (print) {
                printTotals(dataSet.getKey(), totals);
            }
        }
    }
    
    private static void printTotals(String set, Map<String, AtomicDouble> totals) {
        final DecimalFormat df = new DecimalFormat("00.00");
        final double total = totals.values().stream().mapToDouble(AtomicDouble::get).sum();
        
        System.out.println((COMPARE ? (set + " - ") : "") + df.format(total) + " - " +
                components.keySet().stream().map(component ->
                                component + " [" + df.format(totals.get(component).get()) + "]" +
                                        " [" + df.format(totals.get(component).get() / (total - totals.get(component).get())) + "]")
                        .collect(Collectors.joining(" - ")));
    }
    
    
    //Inner Classes
    
    private static class FileUtil {
        
        //Constants
        
        static final File DATA_DIR = new File("data");
        
        static final File DATA_FILE = new File(DATA_DIR, "data.txt");
        
        static final File COMPONENTS_FILE = new File(DATA_DIR, "components.txt");
        
        static final File SCHEDULE_FILE = new File(DATA_DIR, "schedule.txt");
        
        static final File OUTPUT_DIR = new File("output");
        
        static final File GRAPH_FILE = new File(OUTPUT_DIR, "graph.csv");
        
        
        //Functions
        
        static final UnaryOperator<List<String>> cleanLines = (List<String> lines) ->
                lines.stream().map(e -> e.replaceAll("(?://|#).*$", "")).map(String::strip).filter(e -> !e.isEmpty()).collect(Collectors.toList());
        
        static final Function<List<String>, List<ImmutablePair<Instant, Double>>> parseData = (List<String> data) ->
                data.stream().map(e -> e.split("\\|")).flatMap(e ->
                                (e[1].contains("X") ? IntStream.of(0, 4) : (IntStream.of(0))).boxed()
                                        .map((UncheckedFunction<Integer, ImmutablePair<Instant, Double>>) delay ->
                                                new ImmutablePair<>(TimeUtil.addHours.apply(TimeUtil.parseLocal.apply(e[0]), delay),
                                                        (Double.parseDouble(e[1].replace("X", "")) / (e[1].contains("X") ? 2.0 : 1.0)))))
                        .sorted(Comparator.comparing(o -> o.left)).collect(Collectors.toList());
        
        static final Supplier<Map<Double, String>> loadSchedule = () ->
                cleanLines.apply(Filesystem.readLines(SCHEDULE_FILE)).stream()
                        .map(e -> e.split(",")).collect(MapCollectors.toMap(LinkedHashMap.class,
                                e -> Double.parseDouble(e[0].strip()),
                                e -> e[1].strip()));
        
        static final Supplier<List<ImmutablePair<Instant, Double>>> loadData = () ->
                parseData.apply(cleanLines.apply(Filesystem.readLines(DATA_FILE)));
        
        static final Supplier<List<ImmutablePair<Instant, Double>>> loadComparison = () ->
                !COMPARE ? null : parseData.apply(
                        IntStream.rangeClosed(-30, 30)
                                .mapToObj(i -> TimeUtil.addDays.apply(TimeUtil.startOfToday.get(), i))
                                .flatMap(day -> loadSchedule.get().entrySet().stream().map(e -> TimeUtil.format.apply(TimeUtil.addHours.apply(day, e.getKey())) + '|' + e.getValue()))
                                .collect(Collectors.toList()));
        
        static final Supplier<Map<String, List<Double>>> loadComponents = () ->
                cleanLines.apply(Filesystem.readLines(COMPONENTS_FILE)).stream()
                        .map(e -> e.split(",")).collect(MapCollectors.toMap(LinkedHashMap.class,
                                e -> e[0].strip(),
                                e -> List.of(Double.parseDouble(e[1]), (Math.log(2) / Double.parseDouble(e[2])), (Double.parseDouble(e[3]) / 3.0))));
        
        static final Consumer<List<String>> saveGraph = (List<String> graphData) ->
                Filesystem.writeLines(GRAPH_FILE, graphData);
        
        static final Action openGraph = () -> {
            final File libreCalcExe = new File("C:/Program Files/LibreOffice/program/scalc.exe");
            if (OperatingSystem.isWindows() && libreCalcExe.exists()) {
                CmdLine.executeCmdAsync(StringUtility.quote(libreCalcExe.getAbsolutePath()) +
                        " -o " + StringUtility.quote(GRAPH_FILE.getAbsolutePath()) +
                        " --infilter=" + StringUtility.quote("CSV:44,34,0,0,4/2/1/1"));
            } else {
                Desktop.getDesktop().open(GRAPH_FILE);
            }
        };
        
    }
    
    private static class TimeUtil {
        
        //Constants
        
        static final SimpleDateFormat SDF_IN = new SimpleDateFormat("yyyyMMdd-HHmm");
        
        static final SimpleDateFormat SDF_OUT = Optional.of(new SimpleDateFormat(SDF_IN.toPattern())).map(df -> Mappers.perform(df, e -> df.setTimeZone(TimeZone.getTimeZone("UTC")))).orElse(null);
        
        static final SimpleDateFormat SDF_OUT_PRINT = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        
        
        //Functions
        
        static final Supplier<Instant> now = Instant::now;
        
        static final Function<Long, Instant> atMillis = Instant::ofEpochMilli;
        
        static final Function<Instant, Date> toDate = Date::from;
        
        static final UncheckedFunction<String, Instant> parse = (String string) ->
                SDF_IN.parse(string).toInstant();
        
        static final UncheckedFunction<String, Instant> parseLocal = (String string) ->
                SDF_IN.parse(string).toInstant().atZone(ZoneId.systemDefault()).toInstant();
        
        static final UncheckedFunction<Instant, String> format = (Instant instant) ->
                SDF_OUT.format(toDate.apply(instant));
        
        static final UncheckedFunction<Instant, String> formatPrint = (Instant instant) ->
                SDF_OUT_PRINT.format(toDate.apply(instant));
        
        static final BiFunction<Instant, Number, Instant> addMillis = (Instant instant, Number millis) ->
                instant.plus(millis.longValue(), ChronoUnit.MILLIS);
        
        static final BiFunction<Instant, Number, Instant> addHours = (Instant instant, Number hours) ->
                addMillis.apply(instant, (hours.doubleValue() * TimeUnit.HOURS.toMillis(1)));
        
        static final BiFunction<Instant, Number, Instant> addDays = (Instant instant, Number days) ->
                addMillis.apply(instant, (days.doubleValue() * TimeUnit.DAYS.toMillis(1)));
        
        static final UnaryOperator<Instant> startOfDay = (Instant instant) ->
                instant.truncatedTo(ChronoUnit.DAYS);
        
        static final UnaryOperator<Instant> endOfDay = (Instant instant) ->
                addDays.apply(startOfDay.apply(instant), 1);
        
        static final Supplier<Instant> startOfToday = () ->
                startOfDay.apply(now.get());
        
        static final Supplier<Instant> endOfToday = () ->
                endOfDay.apply(now.get());
        
    }
    
}
