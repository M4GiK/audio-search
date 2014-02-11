/**
 * Project audio-search.
 * Copyright Michał Szczygieł.
 * Created at Feb 8, 2014.
 */
package com.m4gik.core;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
     * The object for FTP connection, based on {@link FTPClient}.
     */
    private FTPClient ftp = null;

    /**
     * The variable for timeout connection.
     */
    private Long timeout;

    /**
     * The constructor for {@link FTPConnection}. This constructor connects with
     * FTP server.
     */
    public FTPConnection(String server, String username, String password,
            Boolean keepConnectionAlive, Long timeout) {
        setTimeout(timeout);
        this.ftp = getFtpConnection(server, username, password,
                keepConnectionAlive);
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
                    // retrieveFile(file, path);
                    getMP3File(file, "FtpFiles/");
                } else if (file.isDirectory()) {
                    // Recursive search
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
}
