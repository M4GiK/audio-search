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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
