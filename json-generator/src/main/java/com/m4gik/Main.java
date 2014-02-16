/**
 * Project audio-search.
 * Copyright Michał Szczygieł.
 * Created at Feb 8, 2014.
 */
package com.m4gik;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Handler;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;

import com.m4gik.core.FTPConnection;
import com.m4gik.util.JSONBuilder;

/**
 * The main class, which is the core of application. This class uses all needed
 * method to creates JSON file on remote server. This JSON file contains all
 * information about music files which gets from tags.
 * 
 * @author m4gik <michal.szczygiel@wp.pl>
 * 
 */
public class Main {

    /**
     * This logger is responsible for the registration of events.
     */
    static final Logger logger = LogManager.getLogger(Main.class.getName());

    /**
     * This method builds JSON library on server.
     * 
     * @param properties
     *            The data for FTP connection.
     */
    private static void buildLibrary(String... properties) {
        initConfiguration();
        // FTPConnection ftpConn = initConnection(properties[0], properties[1],
        // properties[2], properties[3]);
        logger.debug("Bulding library process in progress...");
        // ftpConn.retrieveFiles(properties[3]);
        clean();

    }

    /**
     * 
     * @param address
     * @param login
     * @param password
     * @param path
     */
    // private static void buildLibrary(String address, String login,
    // String password, String path) {
    // initConfiguration();
    // FTPConnection ftpConn = initConnection(address, login, password, path);
    // logger.debug("Bulding library process in progress...");
    // ftpConn.retrieveFiles(path);
    // // ftpConn.storeFile(new JSONBuilder().createJSONFile(), args[3]
    // // + JSONBuilder.JSON_FILE);
    // // new JSONBuilder().buildLibrary();
    // clean();
    // }

    /**
     * This method builds file with properties.
     * 
     * @param address
     * @param login
     * @param password
     * @param path
     */
    private static void buildProperties(String address, String login,
            String password, String path) {
        FileWriter file = null;

        try {
            file = new FileWriter(JSONBuilder.JSON_PROPERTIES);
            file.write(JSONBuilder.buildProperties(address, login, password,
                    path).toJSONString());
        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);
        } finally {
            try {
                file.flush();
                file.close();
            } catch (IOException ioe) {
                logger.error(ioe);
                logger.debug(ioe);
            }
        }
    }

    /**
     * This method checks if the given file for fileName is existing.
     * 
     * @param fileName
     *            The name of file to check.
     * @return True if the file is existing, false if is not.
     */
    private static Boolean checkIsExisting(String fileName) {
        Boolean isExisting = false;
        File file = new File(JSONBuilder.JSON_PROPERTIES);

        if (file.exists()) {
            isExisting = true;
        }

        return isExisting;
    }

    /**
     * 
     * @param jsonProperties
     * @return
     */
    private static Boolean checkPropertiesFile(String jsonProperties) {
        Boolean isCorrect = false;

        if (checkIsExisting(jsonProperties)) {
            if (JSONBuilder.validProperties(jsonProperties)) {
                isCorrect = true;
            }
        }

        return isCorrect;
    }

    /**
     * This method deletes all temporary files.
     */
    private static void clean() {
        File file = new File(JSONBuilder.TEMP);

        if (file.exists()) {
            if (file.isDirectory()) {
                for (File singleFile : file.listFiles()) {
                    singleFile.delete();
                }
            }
            file.delete();
        }
    }

    /**
     * This method contains configuration for this application.
     */
    private static void initConfiguration() {
        loggerSetup();
        logger.debug("Application is starting...");

        System.out.println("Trwa uruchamianie aplikacji...");
        File file = new File(JSONBuilder.TEMP);
        if (!file.exists()) {
            if (!file.mkdir()) {
                System.out.println("Aplikacja nie ma praw do stworzenia"
                        + " lokalnego folderu temp.");
                System.out.println("Nadaj odpowiednia prawa aplikacji,"
                        + " lub spróbuj stworzyć folder ręcznie");
            }
        }
    }

    /**
     * This method initialize FTP connection with remote server.
     * 
     * @param address
     * @param login
     * @param password
     * @param path
     * @return
     */
    private static FTPConnection initConnection(String address, String login,
            String password, String path) {
        return new FTPConnection(address, login, password, true, 2000L, path);
    }

    /**
     * This method sets up configuration for loggers.
     */
    private static void loggerSetup() {
        // BasicConfigurator.configure();
        // mp3LoggerSetup();
    }

    /**
     * The main method of application. The application needs 4 args:
     * 
     * - server address
     * 
     * - login
     * 
     * - password
     * 
     * - path to files
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String... args) throws IOException {
        if (args.length == 4) {
            buildProperties(args[0], args[1], args[2], args[3]);
            buildLibrary(args[0], args[1], args[2], args[3]);
        } else if (checkPropertiesFile(JSONBuilder.JSON_PROPERTIES)) {
            // Loading from properties file.
            buildLibrary(JSONBuilder
                    .readProperties(JSONBuilder.JSON_PROPERTIES));
        } else {
            printInfromation();
        }
    }

    /**
     * This method deletes logger properties for AudioFile.
     */
    private static void mp3LoggerSetup() {
        AudioFile.logger.setUseParentHandlers(false);
        java.util.logging.Logger globalLogger = java.util.logging.Logger
                .getLogger("");
        Handler[] handlers = globalLogger.getHandlers();
        for (Handler handler : handlers) {
            globalLogger.removeHandler(handler);
        }
    }

    /**
     * 
     */
    private static void printInfromation() {
        logger.debug("The application needs 4 arguments: "
                + "\n- server address \n- login \n- password \n- path to files\n");
        System.out.println("Do uruchomienia aplikacja potrzebuje 4 argumenty:");
        System.out.println("- adres serwera");
        System.out.println("- nazwa użytkownika");
        System.out.println("- hasło");
        System.out.println("- ścieżka do plików audio (Jeśli foldery są "
                + "zagnieżdzone, to należy podać ten znajdujący"
                + " się najwyżej w fierarchi folderowej\n");
        System.out.println("Przykład użycia:");
        System.out.println("\t ftp.serwer.pl admin admin1 /public/muzyka");
    }
}
