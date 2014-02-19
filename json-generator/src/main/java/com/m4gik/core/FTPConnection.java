/**
 * Project audio-search.
 * Copyright Michał Szczygieł.
 * Created at Feb 8, 2014.
 */
package com.m4gik.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.m4gik.util.JSONBuilder;

/**
 * This class is responsible for operation on FTP server, provides connection
 * and files operations.
 * 
 * @author m4gik <michal.szczygiel@wp.pl>
 * 
 */
public class FTPConnection {

    /**
     * This logger is responsible for the registration of events.
     */
    static final Logger logger = LogManager.getLogger(FTPConnection.class
            .getName());

    /**
     * The variable store amount of audio files.
     */
    private Integer audioAmount = 0;

    /**
     * The variable store counted amount of audio files.
     */
    private Integer currentAmount = 0;

    /**
     * The object for FTP connection, based on {@link FTPClient}.
     */
    private FTPClient ftp = null;

    /**
     * This boolean value keep information about proper configuration of library
     * on server side.
     */
    private Boolean isLibraryChecked = false;

    /**
     * The outputStream, for keep JSON library
     */
    private OutputStream jsonLib;

    /**
     * This variable store information about location of library on server side.
     */
    private String libraryPath;

    /**
     * The variable for timeout connection.
     */
    private Long timeout;

    /**
     * The constructor for {@link FTPConnection}. This constructor connects with
     * FTP server, counts amount of all files in given directory and checks JSON
     * library on server, if library does not existing create one.
     */
    public FTPConnection(String server, String username, String password,
            Boolean keepConnectionAlive, Long timeout, String path) {
        setTimeout(timeout);
        setLibraryPath(path + JSONBuilder.JSON_FILE);

        logger.debug("Connecting to " + server);
        this.ftp = getFtpConnection(server, username, password,
                keepConnectionAlive);
        logger.debug("Connected to " + server);

        logger.debug("Counting audio files in directory " + path);
        this.audioAmount = countAudio(path);
        logger.debug("Audio files counted - " + this.audioAmount + " files");

        logger.debug("Checking library");
        if (checkJsonFile(path)) {
            logger.debug("Library checked");
        } else {
            logger.debug("Checking failed");
            logger.error("Checking failed");
            System.exit(1);
        }
    }

    /**
     * This method checks if file contain given extension.
     * 
     * @param fileName
     *            The file name to check.
     * @param extension
     *            The extensions which is compare with fileName.
     * @return True if fileName contain given extension, if not return false.
     */
    private Boolean checkExtension(String fileName, String extension) {
        Boolean isProperExtension = false;
        Integer i = fileName.lastIndexOf('.');

        if (i > 0) {
            if (extension.equalsIgnoreCase(fileName.substring(i + 1))) {
                isProperExtension = true;
            }
        }

        return isProperExtension;
    }

    /**
     * 
     * @param path
     */
    private Boolean checkJsonFile(String path) {
        OutputStream jsonLib = null;

        try {
            jsonLib = new ByteArrayOutputStream();
            // FileOutputStream(JSONBuilder.TEMP
            // + JSONBuilder.JSON_FILE);

            if (ftp.retrieveFile(path + JSONBuilder.JSON_FILE, jsonLib) == true) {
                isLibraryChecked = true;
                jsonLib.flush();
                setJsonLib(jsonLib);
            } else {
                storeFile(new JSONBuilder().initLibrary(), path
                        + JSONBuilder.JSON_FILE);
                checkJsonFile(path);
            }

            jsonLib.close();

        } catch (FileNotFoundException e) {
            logger.error(e);
            logger.debug(e);
        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);
        }

        return isLibraryChecked;
    }

    /**
     * This method checks JSON library if contains given key(name of file).
     * 
     * @param name
     *            The name of file to check in JSON library.
     * @return True if in JSON library contains key, false if not.
     */
    private Boolean checkLibrary(String FileName) {
        Boolean isExisting = false;

        if (JSONBuilder.checkExistingKey(jsonLib, FileName)) {
            isExisting = true;
        }

        return isExisting;
    }

    /**
     * This method counts amount of audio file in given directory.
     * 
     * @param path
     *            The path of directory.
     * @return Amount of audio files.
     */
    private Integer countAudio(String path) {
        Integer amount = 0;

        try {
            for (FTPFile file : ftp.listFiles(path)) {
                if (file.isFile() && checkExtension(file.getName(), "mp3")) {
                    amount++;
                } else if (isFolder(file)) {
                    amount += countAudio(path + file.getName() + "/");
                }
            }
        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);
        }

        return amount;
    }

    /**
     * This method makes FTP connection with server.
     * 
     * @param server
     * @param username
     * @param password
     * @param keepConnectionAlive
     * @return set up FTPClient connection.
     */
    private FTPClient getFtpConnection(String server, String username,
            String password, Boolean keepConnectionAlive) {
        FTPClient ftp = null;

        try {
            ftp = new FTPClient();
            ftp.connect(server);
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.ASCII_FILE_TYPE);
            ftp.setCharset(Charset.forName("utf8"));
            ftp.setAutodetectUTF8(true);

            if (!ftp.login(username, password)) {
                return null;
            }

            if (getTimeout() != null) {
                ftp.setDefaultTimeout(getTimeout().intValue());
            }

            if (!keepConnectionAlive) {
                ftp.logout();
            }

        } catch (Exception e) {
            logger.error(e);
            logger.debug(e);
            return null;
        } finally {
            try {
                if (!keepConnectionAlive && ftp.isConnected()) {
                    ftp.disconnect();
                }
            } catch (IOException ioe) {
                logger.error(ioe);
                logger.debug(ioe);
            }
        }

        return ftp;
    }

    /**
     * @return the jsonLib
     */
    public OutputStream getJsonLib() {
        return jsonLib;
    }

    /**
     * @return the libraryPath
     */
    public String getLibraryPath() {
        return libraryPath;
    }

    /**
     * This method gets timeout for FTP connection.
     * 
     * @return the timeout
     */
    public Long getTimeout() {
        return timeout;
    }

    /**
     * This method checks if given file is folder.
     * 
     * @param file
     * 
     * @return True if given file is folder, false if not.
     */
    private Boolean isFolder(FTPFile file) {
        Boolean isFolder = false;

        if (file.isDirectory()) {
            if (!file.getName().equals(".") && !file.getName().equals("..")) {
                isFolder = true;
            }
        }

        return isFolder;
    }

    /**
     * This method lists and displays all files from current path.
     * 
     * @param path
     */
    public void listFiles(String path) {
        try {
            for (FTPFile file : ftp.listFiles(path)) {
                logger.debug(file.getName());
            }
        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);
        }
    }

    /**
     * This method
     * 
     * @param currentAmount
     * @return
     */
    private Double progressPercentage(Integer currentAmount) {
        return ((100.0 * currentAmount) / audioAmount) / 100.0;
    }

    /**
     * This method retrieves file from current path.
     * 
     * @param file
     *            The file to download.
     * @param path
     *            The location of file on server side.
     * @return The retrieved file.
     */
    private void retrieveFile(FTPFile file, String path) {
        if (!checkLibrary(file.getName())) {
            FileOutputStream outStream = null;

            try {
                outStream = new FileOutputStream(JSONBuilder.TEMP
                        + file.getName());

                if (ftp.retrieveFile(path + file.getName(), outStream)) {
                    logger.debug(file.getName() + " retrive succesfull");
                } else {
                    logger.debug(file.getName() + " retrive failed, because "
                            + ftp.getReplyString());
                }

                outStream.close();
                storeFile(
                        new ByteArrayInputStream(updateMemory(JSONBuilder
                                .getMP3FileInformation(file.getName(), path,
                                        getJsonLib()).toString().getBytes())),
                        getLibraryPath());
            } catch (FileNotFoundException e) {
                logger.error(e);
                logger.debug(e);
            } catch (IOException ioe) {
                logger.error(ioe);
                logger.debug(ioe);
            }
        }
    }

    /**
     * 
     * @param path
     */
    public void retrieveFiles(String path) {

        try {
            for (FTPFile file : ftp.listFiles(path)) {
                if (file.isFile()) {
                    updateProgress(progressPercentage(currentAmount++));
                    // retrieveFile(file, path);
                    retrieveInputStream(file, path);
                } else if (isFolder(file)) {
                    retrieveFiles(path + file.getName() + "/");
                }
            }
        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);
        }
    }

    /**
     * 
     * @param file
     * @param path
     */
    private void retrieveInputStream(FTPFile file, String path) {
        if (!checkLibrary(file.getName())) {
            InputStream inputStream = null;

            try {
                // Create an InputStream to the File Data and use
                // FileOutputStream to write it.
                inputStream = ftp.retrieveFileStream(path + file.getName());
                FileOutputStream fileOutputStream = new FileOutputStream(
                        JSONBuilder.TEMP + file.getName());

                // Using org.apache.commons.io.IOUtils
                IOUtils.copy(inputStream, fileOutputStream);
                fileOutputStream.flush();
                IOUtils.closeQuietly(fileOutputStream);
                IOUtils.closeQuietly(inputStream);
                Boolean downloadStatus = ftp.completePendingCommand();

                if (downloadStatus) {
                    logger.debug(file.getName() + " retrive succesfull");
                } else {
                    logger.debug(file.getName() + " retrive failed, because "
                            + ftp.getReplyString());
                }

                storeFile(
                        new ByteArrayInputStream(updateMemory(JSONBuilder
                                .getMP3FileInformation(file.getName(), path,
                                        getJsonLib()).toString().getBytes())),
                        getLibraryPath());
            } catch (FileNotFoundException e) {
                logger.error(e);
                logger.debug(e);
            } catch (IOException ioe) {
                logger.error(ioe);
                logger.debug(ioe);
            }
        }
    }

    /**
     * @param jsonLib
     *            the jsonLib to set
     */
    public void setJsonLib(OutputStream jsonLib) {
        this.jsonLib = jsonLib;
    }

    /**
     * @param libraryPath
     *            the libraryPath to set
     */
    public void setLibraryPath(String libraryPath) {
        this.libraryPath = libraryPath;
    }

    /**
     * This method sets timeout for FTP connection.
     * 
     * @param timeout
     *            the timeout to set
     */
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    /**
     * 
     * @param inputStream
     * @param path
     */
    public void storeFile(InputStream inputStream, String path) {
        try {
            logger.debug("Creating JSON library on server");
            ftp.storeFile(path, inputStream);

            if (!(ftp.getReplyCode() == 226)) {
                inputStream.close();
                System.out.println(ftp.getReplyString());
                System.exit(1);
            }

            inputStream.close();

        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);
        }
    }

    /**
     * This method updates cache memory (Input Stream).
     * 
     * @param bytes
     * @return The updated memory.
     */
    private byte[] updateMemory(byte[] bytes) {
        try {
            OutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(bytes);
            getJsonLib().close();
            setJsonLib(outputStream);
        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);
        }

        return bytes;
    }

    /**
     * This method shows the progress of retrieve file from server.
     * 
     * @param progressPercentage
     */
    private void updateProgress(Double progressPercentage) {
        // Progress bar width in chars.
        final Integer width = 50;
        System.out.print("\r[");
        Integer i = 0;

        for (; i < progressPercentage * width; i++) {
            System.out.print("-");
        }

        for (; i < width; i++) {
            System.out.print(" ");
        }

        System.out.print("] " + (int) (progressPercentage * 100) + "% ");
    }
}
