/**
 * Project audio-search.
 * Copyright Michał Szczygieł.
 * Created at Feb 8, 2014.
 */
package com.m4gik.core;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.m4gik.util.JSONBuilder;

import de.vdheide.mp3.FrameDamagedException;
import de.vdheide.mp3.ID3v2DecompressionException;
import de.vdheide.mp3.ID3v2IllegalVersionException;
import de.vdheide.mp3.ID3v2WrongCRCException;
import de.vdheide.mp3.MP3File;
import de.vdheide.mp3.NoMP3FrameException;

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

    private Boolean isLibraryChecked = false;

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

        logger.debug("Connecting to " + server);
        this.ftp = getFtpConnection(server, username, password,
                keepConnectionAlive);
        logger.debug("Connected to " + server);

        logger.debug("Counting audio files in directory" + path);
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
        FileOutputStream outStream = null;

        try {
            outStream = new FileOutputStream(JSONBuilder.TEMP
                    + JSONBuilder.JSON_FILE);

            if (ftp.retrieveFile(path + JSONBuilder.JSON_FILE, outStream) == true) {
                isLibraryChecked = true;
            } else {
                storeFile(new JSONBuilder().initLibrary(), path
                        + JSONBuilder.JSON_FILE);
                checkJsonFile(path);
            }

            outStream.close();

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
                    amount += countAudio(path + "/" + file.getName());
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
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
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
     * Test method.
     * 
     * @param file
     * @param path
     */
    private void getMP3File(FTPFile file, String path) {
        try {
            MP3File mp3 = new MP3File(path, file.getName());
            logger.debug(mp3.getArtist().getTextContent());
        } catch (ID3v2WrongCRCException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ID3v2DecompressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ID3v2IllegalVersionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoMP3FrameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FrameDamagedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
    private OutputStream retrieveFile(FTPFile file, String path) {
        FileOutputStream outStream = null;

        try {
            outStream = new FileOutputStream("FtpFiles/" + file.getName());

            if (ftp.retrieveFile(path + file.getName(), outStream)) {
                logger.debug(file.getName() + " retrive succesfull");
            } else {
                logger.debug(file.getName() + " retrive failed, because "
                        + ftp.getReplyString());
            }

            outStream.close();
        } catch (FileNotFoundException e) {
            logger.error(e);
            logger.debug(e);
        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);
        }

        return outStream;
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
                    // getMP3File(file, "FtpFiles/");
                } else if (isFolder(file)) {
                    retrieveFiles(path + "/" + file.getName());
                }
            }
        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);
        }
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
                System.out.println(ftp.getReplyString());
                System.exit(1);
            }

        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);
        }
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

        System.out.print("] " + (int) (progressPercentage * 100) + "%");
    }
}
