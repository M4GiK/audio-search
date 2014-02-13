/**
 * Project audio-search.
 * Copyright Michał Szczygieł.
 * Created at Feb 8, 2014.
 */
package com.m4gik.util;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.vdheide.mp3.FrameDamagedException;
import de.vdheide.mp3.ID3v2DecompressionException;
import de.vdheide.mp3.ID3v2IllegalVersionException;
import de.vdheide.mp3.ID3v2WrongCRCException;
import de.vdheide.mp3.MP3File;
import de.vdheide.mp3.NoMP3FrameException;

/**
 * TODO COMMENTS MISSING!
 * 
 * @author m4gik <michal.szczygiel@wp.pl>
 * 
 */
public class JSONBuilder {

    /**
     * Header for JSON library.
     */
    private final static String HEADER = "JSON audio library";

    /**
     * Constant variable for name of JSOn library with extension.
     */
    public final static String JSON_FILE = "lib.json";

    /**
     * This logger is responsible for the registration of events.
     */
    static final Logger logger = LogManager.getLogger(JSONBuilder.class
            .getName());

    /**
     * This constant value keeps location for temporary folder.
     */
    public final static String TEMP = "temp/";

    /**
     * This method checks JSON library if contains given key.
     * 
     * @param key
     *            The key with name for current audio.
     * @return True if in JSON library contains key, false if not.
     */
    public static Boolean checkExistingKey(OutputStream jsonLib, String key) {
        Boolean isExistingKey = false;
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject
                .fromObject(jsonLib.toString());

        if (jsonObject.containsKey(key)) {
            isExistingKey = true;
        }

        return isExistingKey;
    }

    /**
     * This method gets current data time.
     * 
     * @return The current data time in format yyyy/MM/dd HH:mm:ss.
     */
    public static String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        return dateFormat.format(cal.getTime()).toString();
    }

    /**
     * This method gets basic information about mp3 file and store all
     * information in JSON library. After store operation, delete this file.
     * 
     * @param fileName
     *            The name of file located in temporary folder.
     * @param path
     *            The path on server side.
     * @param jsonLib
     *            The OutputStream for JSON library.
     * @return The InputStream for JSON library.
     */
    public static InputStream getMP3FileInformation(String fileName,
            String path, OutputStream jsonLib) {
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject
                .fromObject(jsonLib.toString());

        try {
            MP3File mp3File = new MP3File(TEMP, fileName);
            HashMap<String, String> information = new HashMap<String, String>();
            information.put("title", mp3File.getTitle().toString());
            information.put("artist", mp3File.getArtist().toString());
            information.put("year", mp3File.getYear().toString());
            information.put("album", mp3File.getAlbum().toString());
            information.put("lenght", getTime(mp3File.getLength()));
            information.put("bit rate",
                    new Integer(mp3File.getBitrate()).toString());
            information.put("orginal name", mp3File.getName());
            information.put("directory", path);
            information.put("location", path + fileName);
            information.put("size",
                    new Long(mp3File.getTotalSpace()).toString());

            jsonObject.put(fileName, information);
            mp3File.delete();
        } catch (ID3v2WrongCRCException e) {
            logger.error(e);
            logger.debug(e);
        } catch (ID3v2DecompressionException e) {
            logger.error(e);
            logger.debug(e);
        } catch (ID3v2IllegalVersionException e) {
            logger.error(e);
            logger.debug(e);
        } catch (IOException e) {
            logger.error(e);
            logger.debug(e);
        } catch (NoMP3FrameException e) {
            logger.error(e);
            logger.debug(e);
        } catch (FrameDamagedException e) {
            logger.error(e);
            logger.debug(e);
        }

        return new ByteArrayInputStream(jsonObject.toString().getBytes());
    }

    /**
     * This method convert long time to more readable format.
     * 
     * @param length
     *            The time in long format.
     * @return Readable time format.
     */
    private static String getTime(long length) {
        Date date = new Date(length);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        return formatter.format(date);
    }

    /**
     * @throws IOException
     * 
     */
    public void buildLibrary() throws IOException {
        // MP3File mp3 = new MP3File(path, file.getName());
        // logger.debug(mp3.getArtist().getTextContent());

        JSONObject obj = new JSONObject();
        obj.put("__comment", "JSON Library");
        obj.put("Author", "Arpit");

        JSONArray company = new JSONArray();
        company.add("Compnay: eBay");
        company.add("Compnay: Paypal");
        company.add("Compnay: Google");
        obj.put("Company List", company);

        FileWriter file = null;

        try {
            file = new FileWriter(JSON_FILE);
            file.write(obj.toJSONString());
            System.out.println("Successfully Copied JSON Object to File...");
            System.out.println("\nJSON Object: " + obj);
        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);

        } finally {
            file.flush();
            file.close();
        }
    }

    /**
     * 
     * @return
     */
    public InputStream createJSONFile() {
        byte buf[] = HEADER.getBytes();
        InputStream JSONFile = new ByteArrayInputStream(buf);

        return JSONFile;
    }

    /**
     * This method initialize the JSON library.
     */
    @SuppressWarnings({ "unchecked" })
    public InputStream initLibrary() {
        JSONObject library = new JSONObject();
        HashMap<String, String> information = new HashMap<String, String>();

        information.put("information", HEADER);
        information.put("update", getCurrentDateTime());
        information.put("file name", JSON_FILE);
        information.put("author", "Michał Szczygieł");
        information.put("contact", "michal.szczygiel@wp.pl");
        library.put("_comment", information);

        return new ByteArrayInputStream(library.toJSONString().getBytes());

    }
}
