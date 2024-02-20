/*
 * File:    CmdLine.java
 * Package: commons.access
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.access;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.io.console.ProgressBar;
import commons.object.collection.ListUtility;
import commons.object.collection.MapUtility;
import commons.object.string.StringUtility;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides access to the system command line.
 */
public final class CmdLine {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(CmdLine.class);
    
    
    //Constants
    
    /**
     * The map of the script commands for different operating systems.
     */
    public static final Map<OperatingSystem.OS, ImmutablePair<String, String>> SCRIPT_COMMANDS = MapUtility.unmodifiableMap(MapUtility.mapOf(HashMap.class,
            new ImmutablePair<>(OperatingSystem.OS.WINDOWS, new ImmutablePair<>("cmd.exe", "/s /c")),
            new ImmutablePair<>(OperatingSystem.OS.UNIX, new ImmutablePair<>("/bin/bash", "-c")),
            new ImmutablePair<>(OperatingSystem.OS.MACOS, new ImmutablePair<>("/bin/bash", "-c")),
            new ImmutablePair<>(OperatingSystem.OS.POSIX, new ImmutablePair<>("/bin/sh", "-c"))));
    
    /**
     * The default value of the flag indicating whether to build command processes using the script command of the local operating system.
     */
    public static final boolean DEFAULT_USE_SCRIPT_COMMAND = true;
    
    /**
     * The default value of the flag indicating whether null should be returned if an exception occurs while executing a command process, rather then of throwing the exception to the caller.
     */
    public static final boolean DEFAULT_SAFE_EXECUTE = true;
    
    /**
     * The prefix to prepend to command output error lines.
     */
    public static final String ERROR_LOG_PREFIX = "[*]";
    
    
    //Static Fields
    
    /**
     * A list of running processes that were started during this session.
     */
    private static final Map<Process, String> runningProcesses = MapUtility.synchronizedMap();
    
    //Attempt to terminate synchronous cmd processes that were started during this session and are still running
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                runningProcesses.forEach((process, cmd) -> {
                    if (!killProcess(process)) {
                        getProcessTree(process).stream().filter(ProcessHandle::isAlive).forEach(e ->
                                logger.error("{} with pid: {} could not be terminated: {}",
                                        ((e.pid() == process.pid()) ? "Process" : "Subprocess"), e.pid(), (((e.pid() == process.pid()) ? "" : "Subprocess of: ") + cmd)));
                    }
                })));
    }
    
    
    //Static Methods
    
    /**
     * Executes a command process on the system command line.
     *
     * @param safeExecute    Whether null should be returned if an exception occurs, rather then of throwing the exception to the caller.
     * @param progressBar    The progress bar to send the command output to.
     * @param commandProcess The command process.
     * @return The output; error lines are prepended with '[*]'; or null if there was an error and {@code safeExecute} is enabled.
     * @throws RuntimeException When there is an error executing the command and {@code safeExecute} is not enabled.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public static String executeCmd(boolean safeExecute, ProgressBar progressBar, ProcessBuilder commandProcess) throws RuntimeException {
        if (commandProcess == null) {
            return null;
        }
        final String command = String.join(" ", commandProcess.command());
        
        try {
            final AtomicReference<Process> process = new AtomicReference<>(null);
            final List<String> response = ListUtility.synchronizedList();
            
            final AtomicInteger logReadersCount = new AtomicInteger(0);
            final CountDownLatch logReadersLatch = new CountDownLatch(2);
            final ExecutorService logReaders = Executors.newFixedThreadPool((int) logReadersLatch.getCount(), task -> {
                final Thread thread = Executors.defaultThreadFactory().newThread(task);
                thread.setName(StringUtility.format("CmdLogReader-{}-{}", process.get().pid(), ((logReadersCount.getAndIncrement() > 0) ? "Out" : "Err")));
                thread.setDaemon(true);
                return thread;
            });
            final Function<Boolean, Runnable> logReaderBuilder = (Boolean err) -> () -> {
                while (process.get() == null) {
                }
                try (BufferedReader logReader = new BufferedReader(new InputStreamReader(err ? process.get().getErrorStream() : process.get().getInputStream()))) {
                    String line;
                    while ((line = logReader.readLine()) != null) {
                        response.add((err ? ERROR_LOG_PREFIX : "") + line);
                        if (progressBar != null) {
                            progressBar.processLog(line, err);
                        }
                    }
                } catch (Exception ignored) {
                } finally {
                    logReadersLatch.countDown();
                }
            };
            
            process.set(commandProcess.start());
            runningProcesses.put(process.get(), command);
            
            logReaders.execute(logReaderBuilder.apply(true));
            logReaders.execute(logReaderBuilder.apply(false));
            
            process.get().waitFor();
            logReadersLatch.await();
            logReaders.shutdown();
            
            process.get().destroy();
            process.get().descendants().forEachOrdered(ProcessHandle::destroy);
            runningProcesses.remove(process.get());
            
            if (progressBar != null) {
                progressBar.complete();
            }
            return StringUtility.unsplitLines(response);
            
        } catch (Exception e) {
            logger.error("Error executing command: " + StringUtility.quote(command), e);
            if (safeExecute) {
                return null;
            } else {
                throw new RuntimeException(e);
            }
        }
    }
    
    public static String executeCmd(boolean safeExecute, ProgressBar progressBar, OperatingSystem operatingSystem, File workingDirectory, List<String> command) throws RuntimeException {
        return executeCmd(safeExecute, progressBar, buildProcess(command));
    }
    
    public static String executeCmd(boolean safeExecute, ProgressBar progressBar, boolean useScriptCommand, File workingDirectory, List<String> command) throws RuntimeException {
        return executeCmd(safeExecute, progressBar, buildProcess(command));
    }
    
    /**
     * Executes a command on the system command line.
     *
     * @param command     The command.
     * @param safeExecute Whether null should be returned if an exception occurs, rather then of throwing the exception to the caller.
     * @param progressBar The progress bar to send the command output to.
     * @return The output; error lines are prepended with '[*]'; or null if there was an error and {@code safeExecute} is enabled.
     * @throws RuntimeException When there is an error executing the command and {@code safeExecute} is not enabled.
     * @see #executeCmd(boolean, ProgressBar, ProcessBuilder)
     */
    public static String executeCmd(String command, boolean safeExecute, ProgressBar progressBar) throws RuntimeException {
        return executeCmd(safeExecute, progressBar, buildProcess(command));
    }
    
    /**
     * Executes a command on the system command line.
     *
     * @param command     The command.
     * @param safeExecute Whether null should be returned if an exception occurs, rather then of throwing the exception to the caller.
     * @return The output; error lines are proceeded by '[*]'; or null if there was an error and {@code safeExecute} is enabled.
     * @throws RuntimeException When there is an error executing the command and {@code safeExecute} is not enabled.
     * @see #executeCmd(String, boolean, ProgressBar)
     */
    public static String executeCmd(String command, boolean safeExecute) throws RuntimeException {
        return executeCmd(command, safeExecute, null);
    }
    
    /**
     * Executes a command on the system command line.
     *
     * @param command     The command.
     * @param progressBar The progress bar to send the command output to.
     * @return The output; error lines are proceeded by '[*]'; or null if there was an error.
     * @see #executeCmd(String, boolean, ProgressBar)
     */
    public static String executeCmd(String command, ProgressBar progressBar) {
        return executeCmd(command, DEFAULT_SAFE_EXECUTE, progressBar);
    }
    
    /**
     * Executes a command on the system command line.
     *
     * @param command The command.
     * @return The output; error lines are proceeded by '[*]'; or null if there was an error.
     * @see #executeCmd(String, ProgressBar)
     */
    public static String executeCmd(String command) {
        return executeCmd(command, null);
    }
    
    /**
     * Executes a command on the system command line asynchronously.
     *
     * @param command The command.
     * @return The process running the command execution, or null if there was an error.
     */
    public static Process executeCmdAsync(String command) {
        try {
            return buildProcess(command).start();
        } catch (Exception ignored) {
            return null;
        }
    }
    
    public static Cmd cmd(String... command) {
        return new Cmd(command);
    }
    
    /**
     * Builds a command process.
     *
     * @param operatingSystem  The operating system to use the script command of, or null to use no script command:<br/>
     *                         Windows : <i>"cmd.exe /s /c"</i><br/>
     *                         Linux   : <i>"/bin/bash -c"</i><br/>
     *                         MacOS   : <i>"/bin/bash -c"</i><br/>
     *                         POSIX   : <i>"/bin/sh -c"</i>
     * @param workingDirectory The directory that the command will be executed in.
     * @param command          The array containing the program and arguments of the command.
     * @return The process builder for the command, or null if there was an error.
     * @throws RuntimeException When attempting to build a process for an unsupported operating system.
     * @see #SCRIPT_COMMANDS
     */
    private static ProcessBuilder buildProcess(OperatingSystem.OS operatingSystem, File workingDirectory, List<String> command) throws RuntimeException {
        return Optional.ofNullable(command)
                .map(Collection::stream)
                .map(cmdStream -> cmdStream.filter(Objects::nonNull))
                .filter(cmdStream -> cmdStream.findAny().isPresent())
                .map(cmdStream -> Optional.ofNullable(operatingSystem)
                        .map(os -> Optional.ofNullable(SCRIPT_COMMANDS.get(os))
                                .map(scriptCmd -> Stream.of(scriptCmd.left, scriptCmd.right))
                                .map(scriptCmdStream -> Stream.concat(scriptCmdStream, cmdStream))
                                .orElseThrow(() -> new RuntimeException(StringUtility.format(
                                        "Operating system: {} is not supported!", System.getProperty("os.name").toUpperCase()))))
                        .orElse(cmdStream))
                .map(cmdStream -> cmdStream.collect(Collectors.toList()))
                .map(ProcessBuilder::new)
                .map(processBuilder -> Optional.ofNullable(workingDirectory)
                        .map(dir -> processBuilder.directory(workingDirectory))
                        .orElse(processBuilder))
                .orElse(null);
    }
    
    /**
     * Builds a command process.
     *
     * @param operatingSystem  The operating system to use the script command of, or null to use no script command.
     * @param workingDirectory The directory that the command will be executed in.
     * @param command          The array containing the program and arguments of the command.
     * @return The process builder for the command, or null if there was an error.
     * @throws RuntimeException When attempting to build a process for an unsupported operating system.
     * @see #buildProcess(OperatingSystem.OS, File, List)
     */
    private static ProcessBuilder buildProcess(OperatingSystem.OS operatingSystem, File workingDirectory, String... command) throws RuntimeException {
        return buildProcess(operatingSystem, workingDirectory, Arrays.asList(command));
    }
    
    /**
     * Builds a command process.
     *
     * @param useScriptCommand Whether to build the process using the script command of the local operating system.
     * @param workingDirectory The directory that the command will be executed in.
     * @param command          A list containing the program and arguments of the command.
     * @return The process builder for the command, or null if there was an error.
     * @throws RuntimeException When attempting to use a script command to build a process from an unsupported operating system.
     * @see #buildProcess(OperatingSystem.OS, File, List)
     */
    public static ProcessBuilder buildProcess(boolean useScriptCommand, File workingDirectory, List<String> command) throws RuntimeException {
        return buildProcess((useScriptCommand ? OperatingSystem.getOperatingSystem() : null), workingDirectory, command);
    }
    
    /**
     * Builds a command process.
     *
     * @param useScriptCommand Whether to build the process using the script command of the local operating system.
     * @param workingDirectory The directory that the command will be executed in.
     * @param command          An array containing the program and arguments of the command.
     * @return The process builder for the command, or null if there was an error.
     * @throws RuntimeException When attempting to use a script command to build a process from an unsupported operating system.
     * @see #buildProcess(boolean, File, List)
     */
    public static ProcessBuilder buildProcess(boolean useScriptCommand, File workingDirectory, String... command) throws RuntimeException {
        return buildProcess(useScriptCommand, workingDirectory, Arrays.asList(command));
    }
    
    /**
     * Builds a command process.
     *
     * @param useScriptCommand Whether to build the process using the script command of the local operating system.
     * @param command          A list containing the program and arguments of the command.
     * @return The process builder for the command, or null if there was an error.
     * @throws RuntimeException When attempting to use a script command to build a process from an unsupported operating system.
     * @see #buildProcess(boolean, File, List)
     */
    public static ProcessBuilder buildProcess(boolean useScriptCommand, List<String> command) throws RuntimeException {
        return buildProcess(useScriptCommand, null, command);
    }
    
    /**
     * Builds a command process.
     *
     * @param useScriptCommand Whether to build the process using the script command of the local operating system.
     * @param command          An array containing the program and arguments of the command.
     * @return The process builder for the command, or null if there was an error.
     * @throws RuntimeException When attempting to use a script command to build a process from an unsupported operating system.
     * @see #buildProcess(boolean, List)
     */
    public static ProcessBuilder buildProcess(boolean useScriptCommand, String... command) throws RuntimeException {
        return buildProcess(useScriptCommand, Arrays.asList(command));
    }
    
    /**
     * Builds a command process.
     *
     * @param workingDirectory The directory that the command will be executed in.
     * @param command          A list containing the program and arguments of the command.
     * @return The process builder for the command, or null if there was an error.
     * @throws RuntimeException When attempting to build a process from an unsupported operating system.
     * @see #buildProcess(boolean, File, List)
     */
    public static ProcessBuilder buildProcess(File workingDirectory, List<String> command) throws RuntimeException {
        return buildProcess(DEFAULT_USE_SCRIPT_COMMAND, workingDirectory, command);
    }
    
    /**
     * Builds a command process.
     *
     * @param workingDirectory The directory that the command will be executed in.
     * @param command          An array containing the program and arguments of the command.
     * @return The process builder for the command, or null if there was an error.
     * @throws RuntimeException When attempting to build a process from an unsupported operating system.
     * @see #buildProcess(File, List)
     */
    public static ProcessBuilder buildProcess(File workingDirectory, String... command) throws RuntimeException {
        return buildProcess(workingDirectory, Arrays.asList(command));
    }
    
    /**
     * Builds a command process.
     *
     * @param command A list containing the program and arguments of the command.
     * @return The process builder for the command, or null if there was an error.
     * @throws RuntimeException When attempting to build a process from an unsupported operating system.
     * @see #buildProcess(File, List)
     */
    public static ProcessBuilder buildProcess(List<String> command) throws RuntimeException {
        return buildProcess(null, command);
    }
    
    /**
     * Builds a command process.
     *
     * @param command An array containing the program and arguments of the command.
     * @return The process builder for the command, or null if there was an error.
     * @throws RuntimeException When attempting to build a process from an unsupported operating system.
     * @see #buildProcess(List)
     */
    public static ProcessBuilder buildProcess(String... command) throws RuntimeException {
        return buildProcess(Arrays.asList(command));
    }
    
    /**
     * Returns the process tree of a process; which includes itself and all its descendants.
     *
     * @param process The process.
     * @return The process tree of the process.
     */
    public static List<ProcessHandle> getProcessTree(Process process) {
        return Stream.concat(
                        Stream.of(process.toHandle()),
                        process.descendants())
                .collect(Collectors.toList());
    }
    
    /**
     * Attempts to kill a running process.
     *
     * @param process The process.
     * @return Whether the process was successfully killed or not.
     * @see ProcessKiller#kill(Process)
     */
    public static boolean killProcess(Process process) {
        return ProcessKiller.kill(process);
    }
    
    
    //Inner Classes
    
    /**
     * Defines a Cmd.
     */
    public static class Cmd {
        
        //Fields
        
        public String executable = null;
        
        public List<String> arguments = ListUtility.emptyList();
        
        public boolean useScriptCommand = DEFAULT_USE_SCRIPT_COMMAND;
        
        public OperatingSystem.OS operatingSystem = null;
        
        public File workingDirectory = null;
        
        public ProgressBar progressBar = null;
        
        public boolean safeExecute = DEFAULT_SAFE_EXECUTE;
        
        private final AtomicReference<ProcessBuilder> processBuilder = new AtomicReference<>(null);
        
        private final AtomicReference<Process> process = new AtomicReference<>(null);
        
        
        //Functions
        
        private static final Function<Map<String, Object>, List<String>> argumentMapParser = (Map<String, Object> argumentMap) ->
                argumentMap.entrySet().stream()
                        .flatMap(argument -> Stream.of(argument.getKey(), argument.getValue()).filter(Objects::nonNull).map(String::valueOf))
                        .collect(Collectors.toList());
        
        private static final Function<File, String> executableParser = (File executable) ->
                Optional.ofNullable(executable).map(File::getAbsolutePath).orElse(null);
        
        
        //Constructors
        
        public Cmd(String executable, List<String> arguments) {
            this.executable = executable;
            this.arguments = ListUtility.clone(arguments);
        }
        
        public Cmd(String executable, String... arguments) {
            this(executable, ListUtility.toList(arguments));
        }
        
        public Cmd(String executable, Map<String, Object> argumentMap) {
            this(executable, argumentMapParser.apply(argumentMap));
        }
        
        public Cmd(File executable, List<String> arguments) {
            this(executableParser.apply(executable), arguments);
        }
        
        public Cmd(File executable, String... arguments) {
            this(executableParser.apply(executable), arguments);
        }
        
        public Cmd(File executable, Map<String, Object> argumentMap) {
            this(executableParser.apply(executable), argumentMap);
        }
        
        public Cmd(List<String> command) {
            this(ListUtility.getOrNull(command, 0), ListUtility.subList(command, 1));
        }
        
        public Cmd(String... command) {
            this(ListUtility.toList(command));
        }
        
        public Cmd() {
        }
        
        
        //Methods
        
        public Cmd addArgs(List<String> arguments) {
            Optional.ofNullable(processBuilder.get())
                    .ifPresentOrElse(processBuilder -> {
                        throw new UnsupportedOperationException("Cmd has already been compiled");
                    }, () -> this.arguments.addAll(arguments));
            return this;
        }
        
        public Cmd addArgs(String... arguments) {
            return addArgs(ListUtility.toList(arguments));
        }
        
        public Cmd addArgs(Map<String, Object> argumentMap) {
            return addArgs(argumentMapParser.apply(argumentMap));
        }
        
        public Cmd compile() {
            return this;
        }
        
        public Cmd compile(boolean useScriptCommand, OperatingSystem.OS operatingSystem) {
            this.useScriptCommand = useScriptCommand || (operatingSystem != null);
            this.operatingSystem = Optional.ofNullable(operatingSystem).orElseGet(OperatingSystem::getOperatingSystem);
            return compile();
        }
        
        public Cmd compile(boolean useScriptCommand) {
            this.useScriptCommand = useScriptCommand;
            return compile();
        }
        
        public Cmd compile(OperatingSystem.OS operatingSystem) {
            this.operatingSystem = operatingSystem;
            return compile(true);
        }
        
        public Cmd startIn(File workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }
        
        public String execute() {
            return null;
        }
        
        public String execute(boolean safeExecute, ProgressBar progressBar) {
            this.safeExecute = safeExecute;
            this.progressBar = progressBar;
            return execute();
        }
        
        public String execute(boolean safeExecute) {
            return execute(safeExecute, null);
        }
        
        public String execute(ProgressBar progressBar) {
            return execute(DEFAULT_SAFE_EXECUTE, progressBar);
        }
        
        public Process executeAsync() {
            return null; //TODO
        }
        
        public Process executeAsync(boolean safeExecute, ProgressBar progressBar) {
            this.safeExecute = safeExecute;
            this.progressBar = progressBar;
            return executeAsync();
        }
        
        public Process executeAsync(boolean safeExecute) {
            return executeAsync(safeExecute, null);
        }
        
        public Process executeAsync(ProgressBar progressBar) {
            return executeAsync(DEFAULT_SAFE_EXECUTE, progressBar);
        }
        
    }
    
    /**
     * Handles the termination of running processes.
     */
    private static final class ProcessKiller {
        
        //Constants
        
        /**
         * The sequence of Kill Stages to perform when killing a process.
         */
        private static final Map<KillStage, Supplier<KillStage>> KILL_SEQUENCE = new LinkedHashMap<>();
        
        //Populates the Kill Stage sequence
        static {
            KILL_SEQUENCE.put(KillStage.INIT, () -> KillStage.DESTROY);
            KILL_SEQUENCE.put(KillStage.DESTROY, () -> KillStage.DESTROY_FORCIBLY);
            KILL_SEQUENCE.put(KillStage.DESTROY_FORCIBLY, () -> (OperatingSystem.isWindows() ? KillStage.CMD_KILL_WINDOWS : KillStage.CMD_KILL));
            KILL_SEQUENCE.put(KillStage.CMD_KILL, () -> KillStage.CMD_KILL_HARD);
            KILL_SEQUENCE.put(KillStage.CMD_KILL_HARD, () -> null);
            KILL_SEQUENCE.put(KillStage.CMD_KILL_WINDOWS, () -> null);
        }
        
        /**
         * The default amount of time to wait after performing a Kill Stage before checking if the process has been killed or not, in milliseconds.
         */
        private static final long DEFAULT_VALIDATION_DELAY = 250L;
        
        
        //Enums
        
        /**
         * An enumeration of Process Killer Kill Stages.
         */
        private enum KillStage {
            
            //Values
            
            INIT(null, 0, false),
            DESTROY(ProcessHandle::destroy, 1, false),
            DESTROY_FORCIBLY(ProcessHandle::destroyForcibly, 3, false),
            CMD_KILL(e -> executeCmdAsync("kill -SIGTERM " + e.pid()), 1, true),
            CMD_KILL_HARD(e -> executeCmdAsync("kill -SIGKILL " + e.pid()), 1, false),
            CMD_KILL_WINDOWS(e -> executeCmdAsync("taskkill /F /PID " + e.pid()), 1, true);
            
            
            //Fields
            
            /**
             * The action to perform.
             */
            private final Consumer<ProcessHandle> action;
            
            /**
             * The amount of time to wait before check if the process has been killed or not, in milliseconds.
             */
            private final long validationDelay;
            
            /**
             * Whether to iterate the process tree in reverse order or not.
             */
            private final boolean reverseTree;
            
            
            //Constructors
            
            /**
             * Constructs a Kill Stage.
             *
             * @param action      The action to perform.
             * @param delayFactor The factor to apply to the default validation delay.
             * @param reverseTree Whether to iterate the process tree in reverse order or not.
             */
            KillStage(Consumer<ProcessHandle> action, int delayFactor, boolean reverseTree) {
                this.action = action;
                this.validationDelay = delayFactor * DEFAULT_VALIDATION_DELAY;
                this.reverseTree = reverseTree;
            }
            
        }
        
        
        //Fields
        
        /**
         * The process tree of a process, including itself and all its descendants.
         */
        private final List<ProcessHandle> processTree;
        
        /**
         * The current Kill Stage of the Process Killer.
         */
        private final AtomicReference<KillStage> stage;
        
        
        //Constructors
        
        /**
         * The private constructor for a Process Killer.
         *
         * @param process The process to kill.
         */
        private ProcessKiller(Process process) {
            this.processTree = getProcessTree(process);
            this.stage = new AtomicReference<>(null);
        }
        
        
        //Methods
        
        /**
         * Executes the next Kill Stage of the Process Killer.
         */
        public synchronized void nextStage() {
            Optional.ofNullable(stage.updateAndGet(currentStage ->
                    KILL_SEQUENCE.get(Optional.ofNullable(currentStage).orElse(KillStage.INIT)).get())
            
            ).ifPresent(nextStage -> {
                if (nextStage.reverseTree) {
                    Collections.reverse(processTree);
                }
                
                try {
                    processTree.stream().filter(ProcessHandle::isAlive).forEachOrdered(nextStage.action);
                    Thread.sleep(nextStage.validationDelay);
                } catch (Exception ignored) {
                }
            });
        }
        
        /**
         * Determines whether the Process Killer has finished or not.
         *
         * @return Whether the Process Killer has finished or not.
         */
        public synchronized boolean finished() {
            return ((stage.get() != null) && (KILL_SEQUENCE.get(stage.get()).get() == null)) || succeeded();
        }
        
        /**
         * Determines whether the Process Killer was successful or not.
         *
         * @return Whether the Process Killer was successful or not.
         */
        public synchronized boolean succeeded() {
            return processTree.stream().noneMatch(ProcessHandle::isAlive);
        }
        
        
        //Static Methods
        
        /**
         * Attempts to kill a running process.
         *
         * @param process The process.
         * @return Whether the process was successfully killed or not.
         * @see ProcessKiller
         */
        public static boolean kill(Process process) {
            final ProcessKiller processKiller = new ProcessKiller(process);
            while (!processKiller.finished()) {
                processKiller.nextStage();
            }
            return processKiller.succeeded();
        }
        
    }
    
}
