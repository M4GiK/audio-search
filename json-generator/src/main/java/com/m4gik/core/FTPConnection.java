/**
 * Project audio-search.
 * Copyright Michał Szczygieł.
 * Created at Feb 8, 2014.
 */
package com.m4gik.core;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

/**
 * TODO COMMENTS MISSING!
 * 
 * @author m4gik <michal.szczygiel@wp.pl>
 * 
 */
public class FTPConnection {

    /**
     * 
     */
    static final Logger logger = Logger.getLogger(FTPConnection.class);

    /**
     * 
     */
    private FTPClient ftp = null;

    /**
     * 
     */
    private Long timeout;

    /**
     * 
     */
    public FTPConnection(String server, String username, String password,
            Boolean keepConnectionAlive, Long timeout) {
        setTimeout(timeout);
        this.ftp = getFtpConnection(server, username, password,
                keepConnectionAlive);
    }

    /**
     * 
     * @param server
     * @param username
     * @param password
     * @param keepConnectionAlive
     * @return
     */
    private FTPClient getFtpConnection(String server, String username,
            String password, boolean keepConnectionAlive) {
        FTPClient ftp = null;

        try {
            ftp = new FTPClient();
            ftp.connect(server);

            logger.debug("ftp reply = "
                    + FTPReply.isPositiveCompletion(ftp.getReplyCode()));

            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                return null;
            }

            logger.debug("ftp login=" + ftp.login(username, password));
            logger.debug("timeout=" + getTimeout());
            logger.debug("keepConnectionALive=" + keepConnectionAlive);

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
     * This method sets timeout for FTP connection.
     * 
     * @param timeout
     *            the timeout to set
     */
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
}
