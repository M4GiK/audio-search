/**
 * Project audio-search.
 * Copyright Michał Szczygieł.
 * Created at Feb 8, 2014.
 */
package com.m4gik.core;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
     * This method sets timeout for FTP connection.
     * 
     * @param timeout
     *            the timeout to set
     */
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
}
