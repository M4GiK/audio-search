/**
 * Project audio-search.
 * Copyright Michał Szczygieł.
 * Created at Feb 8, 2014.
 */
package com.m4gik;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.m4gik.core.FTPConnection;

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

    private static Boolean checkIsExisting() {
        // TODO Auto-generated method stub
        return false;
    }

    private static Boolean checkPropertiesFile() {
        Boolean isCorrect = false;

        if (checkIsExisting()) {

        }

        return isCorrect;
    }

    /**
     * This method contains configuration for this application.
     */
    private static void initConfiguration() {
        logger.debug("Application is starting...");
        // BasicConfigurator.configure();
        File file = new File("temp/");
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
        initConfiguration();
        if (args.length == 4) {
            FTPConnection ftpConn = initConnection(args[0], args[1], args[2],
                    args[3]);
            // ftpConn.listFiles(args[3]);
            // ftpConn.retrieveFiles(args[3]);
            // ftpConn.storeFile(new JSONBuilder().createJSONFile(), args[3]
            // + JSONBuilder.JSON_FILE);
            // new JSONBuilder().buildLibrary();
        } else if (checkPropertiesFile()) {
            // Loading from properties file.
        } else {
            logger.debug("The application needs 4 arguments: "
                    + "\n- server address \n- login \n- password \n- path to files\n");
            System.out
                    .println("Do uruchomienia aplikacja potrzebuje 4 argumenty:");
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
}
