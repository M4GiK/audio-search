/**
 * Project audio-search.
 * Copyright Michał Szczygieł.
 * Created at Feb 8, 2014.
 */
package com.m4gik.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class is responsible for building JSON library. The basic operation of
 * this class provide: initialization of library, updating library, and build
 * library.
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
     * This constant value keeps name for file with FTP properties.
     */
    public final static String JSON_PROPERTIES = "ftp.properties";

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
     * This method build JSON object with given properties.
     * 
     * @param address
     * @param login
     * @param password
     * @param path
     * @return The prepared JSONObject.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject buildProperties(String address, String login,
            String password, String path) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("address", address);
        jsonObject.put("login", login);
        jsonObject.put("password", password);
        jsonObject.put("directory", path);

        return jsonObject;
    }

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
     * This method gathers all information from list and store in single string.
     * 
     * @param informationList
     *            The list with information.
     * @return The single string with information from the list.
     */
    private static String getInformationFromList(List<TagField> informationList) {
        String information = "";

        for (Object object : safe(informationList)) {
            information += object + " ";
        }

        return information;
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
    @SuppressWarnings("unchecked")
    public static InputStream getMP3FileInformation(String fileName,
            String path, OutputStream jsonLib) {
        // net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject
        // .fromObject(jsonLib.toString());
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonLib.toString());
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        MP3File mp3File = null;
        HashMap<String, String> information = new HashMap<String, String>();

        try {
            mp3File = new MP3File(new File(TEMP + "/" + fileName));

            if (!mp3File.getID3v1Tag().getTitle().isEmpty()) {
                information.put("title", getInformationFromList(mp3File
                        .getID3v1Tag().getTitle()));
            } else {
                information.put("title", "");
            }

            if (!mp3File.getID3v1Tag().getArtist().isEmpty()) {
                information.put("artist", getInformationFromList(mp3File
                        .getID3v1Tag().getArtist()));
            } else {
                information.put("artist", "");
            }

            if (!mp3File.getID3v1Tag().getYear().isEmpty()) {
                information.put("year", getInformationFromList(mp3File
                        .getID3v1Tag().getYear()));
            } else {
                information.put("year", "");
            }

            if (!mp3File.getID3v1Tag().getAlbum().isEmpty()) {
                information.put("album", getInformationFromList(mp3File
                        .getID3v1Tag().getAlbum()));
            } else {
                information.put("album", "");
            }

            information.put("lenght", getTime(mp3File.getMP3AudioHeader()
                    .getTrackLength()));
            information.put("bit rate", mp3File.getMP3AudioHeader()
                    .getBitRate());
            information.put("size",
                    new Long(mp3File.getFile().getTotalSpace()).toString());

        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);
        } catch (TagException e) {
            logger.error(e);
            logger.debug(e);
        } catch (ReadOnlyFileException e) {
            logger.error(e);
            logger.debug(e);
        } catch (InvalidAudioFrameException e) {
            logger.error(e);
            logger.debug(e);
        } catch (NullPointerException e) {
            logger.error(e);
            logger.debug(e);
        }

        mp3File.getFile().delete();
        information.put("orginal name", fileName);
        information.put("directory", path);
        information.put("location", path + fileName);
        jsonObject.put(fileName, information);

        try {
            jsonLib.write(jsonObject.toString().getBytes());
        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);
        }

        System.out.println(jsonObject.toString());

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
     * This method read values from properties file and return string array with
     * data.
     * 
     * @param jsonProperties
     *            The file name for properties file.
     * @return The array with data from properties file.
     */
    public static String[] readProperties(String jsonProperties) {
        String[] properties = new String[4];
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) new JSONParser().parse(new FileReader(
                    new File(jsonProperties)));

            properties[0] = (String) jsonObject.get("address");
            properties[1] = (String) jsonObject.get("login");
            properties[2] = (String) jsonObject.get("password");
            properties[3] = (String) jsonObject.get("directory");

        } catch (FileNotFoundException e) {
            logger.error(e);
            logger.debug(e);
        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);
        } catch (ParseException e) {
            logger.error(e);
            logger.debug(e);
        }

        return properties;
    }

    /**
     * This method perform safe operations on the lists.
     * 
     * @param list
     *            The list to check if is empty.
     * @return safe list, which avoid {@link NullPointerException}
     */
    @SuppressWarnings("rawtypes")
    public static List safe(List list) {
        return list == null ? Collections.EMPTY_LIST : list;
    }

    /**
     * This method makes validation for properties file.
     * 
     * @param jsonProperties
     *            The file name for properties file.
     * @return True if file is valid correctly, false if is not.
     */
    public static Boolean validProperties(String jsonProperties) {
        Boolean isValid = false;
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) new JSONParser().parse(new FileReader(
                    new File(jsonProperties)));

            if (jsonObject.containsKey("address")
                    && !jsonObject.get("address").equals("")) {
                isValid = true;
            } else {
                isValid = false;
            }

            if (jsonObject.containsKey("login")
                    && !jsonObject.get("login").equals("") && isValid == true) {
                isValid = true;
            } else {
                isValid = false;
            }

            if (jsonObject.containsKey("password")
                    && !jsonObject.get("password").equals("")
                    && isValid == true) {
                isValid = true;
            } else {
                isValid = false;
            }

            if (jsonObject.containsKey("directory")
                    && !jsonObject.get("directory").equals("")
                    && isValid == true) {
                isValid = true;
            } else {
                isValid = false;
            }

        } catch (FileNotFoundException e) {
            logger.error(e);
            logger.debug(e);
        } catch (IOException ioe) {
            logger.error(ioe);
            logger.debug(ioe);
        } catch (ParseException e) {
            logger.error(e);
            logger.debug(e);
        }

        return isValid;
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
