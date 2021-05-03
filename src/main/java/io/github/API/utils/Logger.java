package io.github.API.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;

/**
 * Wrapper classes for more readable/cleaner system-wide error message logging. Uses:
 * @see java.util.logging.Logger
 * @see LogManager
 */
public final class Logger {
    private static boolean production = false;
    private static final int DEFAULT_DEPTH = 4;
    private static java.util.logging.Logger appLogger = java.util.logging.Logger.getLogger("Unknown");
    private static String appName = "Unknown";
    private static String basePackage = null;
    private static boolean initialized = false;


    /**
     * Constructor
     */
    private Logger() {
    }

    /**
     * @return the current logger level;
     */
    public static Level getLevel() {
        return appLogger.getLevel();
    }

    /**
     * Change the log level of this logger.
     * @param newLevel the level of log to show
     */
    public static void setLevel(Level newLevel) {
        appLogger.setLevel(newLevel);
    }

    /**
     * Load a config file for the logger and set locale to english.
     * @param relativePath the path in resources of the config file
     */
    public static void init(String relativePath) {
        init(relativePath, Level.INFO);
    }

    /**
     * Load a config file for the logger and set locale to english.
     * @param relativePath the path in resources of the config file
     * @param level        the level to set the logger to
     */
    public static void init(String relativePath, Level level) {
        Locale.setDefault(Locale.ENGLISH);
        loadConfigFromFile(relativePath);
        Logger.setLevel(level);
        Logger.initialized = true;

    }

    /**
     * Default init method
     */
    public static void init() {
        init("", Level.INFO);
    }
    /**
     * Load a config file for the logger.
     * @param relativePath the path in resources of the config file
     */
    private static void loadConfigFromFile(String relativePath) {
        if (relativePath.equals("")) {
            try {
                LogManager.getLogManager().readConfiguration();
            } catch (SecurityException | IOException e) {
                Logger.log(e);
            }
            return;
        } else if (relativePath.equalsIgnoreCase("production")) {
            production = true;
            return;
        }

        try {
            InputStream is = Logger.class.getClassLoader().getResourceAsStream(relativePath);
            if (is == null) {
                Logger.log(Level.SEVERE, "Logger config file not found at path {0}", relativePath);
                return;
            }
            LogManager.getLogManager().readConfiguration(is);

            appName = getString(relativePath, "app_name", "Unknown");
            basePackage = getString(relativePath, "default_package", null);

            SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
            String outputFile = "log_" + simpleFormatter.format(new Date()) + ".log";
            String output_dir = getString(relativePath, "output_dir", null);

            appLogger = java.util.logging.Logger.getLogger(appName);

            if (output_dir != null) {
                deleteOldFiles(
                        Files.createDirectories(Path.of(System.getProperty("user.dir") + "/" + output_dir)),
                        null,
                        8
                );
//            Files.createDirectories(Path.of(System.getProperty("user.dir") + "/" + output_dir));
                Handler handler = new FileHandler(output_dir.concat(outputFile), true);
                handler.setFormatter(new SimpleFormatter());
                appLogger.addHandler(handler);
            }
        } catch (SecurityException | IOException e) {
            Logger.log(e);
        }
    }

    /**
     * Log an exception.
     * @param e the exception to log
     */
    public static void log(Throwable e) {
        Logger.log(Logger.DEFAULT_DEPTH, Level.SEVERE, e.toString(), e);
    }

    /**
     * Log an exception.
     * @param lvl the level of logging
     * @param e   the exception to log
     */
    public static void log(Level lvl, Throwable e) {
        Logger.log(Logger.DEFAULT_DEPTH, lvl, e.toString(), e);
    }

    /**
     * Log an exception.
     * @param e   the exception to log
     * @param msg the exception msg
     */
    public static void log(Throwable e, String msg) {
        Logger.log(Logger.DEFAULT_DEPTH, Level.SEVERE, msg + ": {0}", e);
    }

    /**
     * Log an exception.
     * @param lvl the level of logging
     * @param e   the exception to log
     * @param msg the exception msg
     */
    public static void log(Level lvl, Throwable e, String msg) {
        Logger.log(Logger.DEFAULT_DEPTH, lvl, msg + ": {0}", e);
    }

    /**
     * Log an INFO message.
     * @param message the message
     * @param objects the object for the message formatting
     */
    public static void log(String message, Object... objects) {
        Logger.log(Logger.DEFAULT_DEPTH, Level.INFO, message, objects);
    }

    /**
     * Log a message.
     * @param lvl     the level of logging
     * @param message the message
     * @param objects the object for the message formatting
     */
    public static void log(Level lvl, String message, Object... objects) {
        Logger.log(Logger.DEFAULT_DEPTH, lvl, message, objects);
    }

    /**
     * Log a message.
     * @param depth   the source depth in stack
     * @param lvl     the level of logging
     * @param message the message
     * @param objects the object for the message formatting
     */
    private static void log(int depth, Level lvl, String message, Object... objects) {
        if (!initialized) {
            initialized = true;
            appLogger.log(Level.WARNING, "Logger was not initialized please do so before using.\n\n");
//            Logger.log(Level.WARNING, "Logger was not initialized please do so before using.");
        } else if (production) {
            return;
        }
        if (lvl.intValue() < appLogger.getLevel().intValue())
            return;
        message = String.format("[thr%s %s] %s", Thread.currentThread().getId(), getHeaderInfo(depth), message);
        appLogger.log(lvl, message, objects);
        if (objects.length > 0 && objects[0] instanceof Throwable) {
            Throwable throwable = (Throwable) objects[0];
            if (lvl == Level.SEVERE) {
                boolean inPackage = false;
                for (StackTraceElement ste : throwable.getStackTrace()) {
                    Logger.log(depth + 1, Level.SEVERE, "\t {0}", ste);
                    if (Logger.basePackage != null) {
                        if (!inPackage && ste.getClassName().startsWith(Logger.basePackage))
                            inPackage = true;
                        else if (inPackage && !ste.getClassName().startsWith(Logger.basePackage))
                            break;
                    }
                }
            }
            if (throwable.getCause() != null)
                Logger.log(depth + 1, lvl, "Caused by: {0}", throwable.getCause());
        }

    }

    /**
     * Get a configuration string by its key.
     * @param bundlePath the path to the configuration file
     * @param key the key in the config file
     * @param defaultValue the default value to use if not found
     * @return the string or default value if not found
     */
    private static String getString(String bundlePath, String key, String defaultValue) {
        int pos = bundlePath.indexOf(".properties");
        try {
            return  ResourceBundle.getBundle(bundlePath.substring(0,pos)).getString(key);
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    /**
     * Return the class name from the calling class in th stack trace.
     * @param stackLevel the level in the stack trace
     * @return the classname of th calling class
     */
    private static String getHeaderInfo(int stackLevel) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackLevel >= stackTrace.length)
            return null;
//        String[] source = stackTrace[stackLevel].getClassName().split("\\.");
        return "\"" + stackTrace[stackLevel].getFileName() + "\"" + ":" +
                stackTrace[stackLevel].getLineNumber() + " " +
                stackTrace[stackLevel].getMethodName() + "()";

//        return source[source.length - 1] + ":" +
//                stackTrace[stackLevel].getLineNumber() + " " +
//                stackTrace[stackLevel].getMethodName() + "()";
    }

    /**
     * Caps the number of log file in the log dir
     * @param parentFolder path to root folder
     * @param extensionCouldBeNull your choice of added an extension to delete
     * @param limit number of files permitted in the parent dir
     * @author Kord Boniadi
     */
    private static void deleteOldFiles(Path parentFolder, String extensionCouldBeNull, int limit) {
        List<Path> files = getSortedFilesByDataCreated(parentFolder, extensionCouldBeNull, false);

        if (files.size() <= limit)
            return;

        files.subList(0, limit).clear();

        files.forEach(f -> {
            try {
                Files.delete(f);
            } catch (IOException e) {
                Logger.log(e);
            }
        });
    }

    /**
     *
     * @param parentFolder path to root folder
     * @param targetExtensionCouldBeNull your choice of added an extension to delete
     * @param ascendingOrder ascending or descending order boolean
     * @return  List of filtered/ordered files
     * @author Kord Boniadi
     */
    private static List<Path> getSortedFilesByDataCreated(Path parentFolder, String targetExtensionCouldBeNull, boolean ascendingOrder) {
        try {
            Comparator<Path> pathComparator = Comparator.comparingLong(f -> getFileCreationEpoch((f).toFile()));

            return Files.list(parentFolder)
                    .filter(Files::isRegularFile)
                    .filter(f -> targetExtensionCouldBeNull == null || f.getFileName().toString().endsWith(targetExtensionCouldBeNull))
                    .sorted(ascendingOrder ? pathComparator : pathComparator.reversed())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the time the file was created and returns it in milli
     * @param file specific file to analyze
     * @return time created in milli
     * @author Kord Boniadi
     */
    private static long getFileCreationEpoch(File file) {
        try {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return attr.creationTime().toInstant().toEpochMilli();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
