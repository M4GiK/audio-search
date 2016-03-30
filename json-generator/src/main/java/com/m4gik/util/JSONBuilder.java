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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

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
     * The constant variable of comment for JSON library.
     */
    public final static String COMMENT = "_comment";

    /**
     * Header for JSON library.
     */
    private final static String HEADER = "JSON audio library";

    /**
     * Constant variable for name of JSON library with extension.
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
     * Map to changing special letters to polish.
     */
    static Map<Character, Character> polishLettersMap;

    /**
     * This constant value keeps location for temporary folder.
     */
    public final static String TEMP = "temp/";

    /**
     * Map of polish letters
     */
    static {
        polishLettersMap = new HashMap<Character, Character>();
        polishLettersMap.put('¥', 'Ą');
        polishLettersMap.put('¹', 'ą');
        polishLettersMap.put('Æ', 'Ć');
        polishLettersMap.put('æ', 'ć');
        polishLettersMap.put('Ê', 'Ę');
        polishLettersMap.put('ê', 'ę');
        polishLettersMap.put('£', 'Ł');
        polishLettersMap.put('³', 'ł');
        polishLettersMap.put('Ñ', 'Ń');
        polishLettersMap.put('ñ', 'ń');
        polishLettersMap.put('Ó', 'Ó');
        polishLettersMap.put('ó', 'ó');
        polishLettersMap.put('', 'Ś');
        polishLettersMap.put('', 'ś');
        polishLettersMap.put('', 'Ź');
        polishLettersMap.put('', 'ź');
        polishLettersMap.put('¯', 'Ż');
        polishLettersMap.put('¿', 'ż');
    }

    /**
     * This method converts the array of String to single String.
     * 
     * @param split
     *            The split String into array.
     * @return Converted String from array to single String.
     */
    private static String arrayToString(String[] splitString) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String string : splitString) {
            stringBuilder.append(string);
            stringBuilder.append(" ");
        }

        return stringBuilder.toString();
    }

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
     * Convert from internal Java String format -> UTF-8.
     * 
     * @param pIncomingString
     * @return Converted String.
     * @throws CharacterCodingException
     */
    public static String convertStringToUTF8(String pIncomingString)
            throws CharacterCodingException {
        CharsetDecoder cd = Charset.availableCharsets().get("UTF-8")
                .newDecoder();
        CharBuffer buffer = cd.decode(ByteBuffer.wrap(pIncomingString
                .getBytes()));

        return convertToPolishLetters(buffer.toString());
    }

    /**
     * This method converts special signs to polish letters.
     * 
     * @param convertedString
     *            The string converted to UTF-8.
     * @return Converted String with polish characters instead of special
     *         characters.
     */
    private static String convertToPolishLetters(String convertedString) {
        StringBuilder sb = new StringBuilder();

        for (char ch : convertedString.toCharArray()) {
            sb.append(isNormalLetter(ch) ? ch : polishLettersMap.get(ch));
        }

        return sb.toString();
    }

    /**
     * This method creates basic information of comments for JSON library.
     * 
     * @return The basic information for JSON library.
     */
    private static Object getCommentInformation() {
        HashMap<String, String> information = new HashMap<String, String>();

        information.put("information", HEADER);
        information.put("update", getCurrentDateTime());
        information.put("file name", JSON_FILE);
        information.put("author", "Michał Szczygieł");
        information.put("contact", "michal.szczygiel@wp.pl");

        return information;
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
    @SuppressWarnings("unused")
    private static String getInformationFromList(List<TagField> informationList) {
        String information = "";

        for (Object object : safe(informationList)) {
            information += object.toString() + " ";
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
    public static net.sf.json.JSONObject getMP3FileInformation(String fileName,
            String path, OutputStream jsonLib) {
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject
                .fromObject(jsonLib.toString());

        Mp3File mp3File = null;
        MP3File fileInformation = null;
        HashMap<String, String> information = new HashMap<String, String>();

        try {

            fileInformation = new MP3File(TEMP + "/" + fileName);
            mp3File = new Mp3File(TEMP + "/" + fileName);

            if (mp3File.hasId3v2Tag()) {

                mp3File.getId3v2Tag().setEncoder("UTF-8");

                if (mp3File.getId3v2Tag().getTitle() != null) {
                    information.put("title", convertStringToUTF8(mp3File
                            .getId3v2Tag().getTitle()));
                }

                if (mp3File.getId3v2Tag().getArtist() != null) {
                    information.put("artist", convertStringToUTF8(mp3File
                            .getId3v2Tag().getArtist()));
                }

                if (mp3File.getId3v2Tag().getYear() != null) {
                    information.put("year", convertStringToUTF8(mp3File
                            .getId3v2Tag().getYear()));
                }

                if (mp3File.getId3v2Tag().getAlbum() != null) {
                    information.put("album", convertStringToUTF8(mp3File
                            .getId3v2Tag().getAlbum()));
                }
            }
        } catch (UnsupportedTagException e1) {
            logger.error(e1);
            logger.debug(e1);
        } catch (InvalidDataException e1) {
            logger.error(e1);
            logger.debug(e1);
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
        }

        if (!information.containsKey("title")
                || information.get("title").equals("")) {
            information.put("title",
                    removeMP3Extension(arrayToString(fileName.split("_"))));
        }

        if (!information.containsKey("artist")) {
            information.put("artist", "");
        }

        if (!information.containsKey("year")) {
            information.put("year", "");
        }

        if (!information.containsKey("album")) {
            information.put("album", "");
        }

        information.put("length", getTime(fileInformation.getAudioHeader()
                .getTrackLength() * 1000));
        information.put("bit rate", fileInformation.getAudioHeader()
                .getBitRate());
        information.put("size",
                new Long(fileInformation.getFile().length()).toString());
        information.put("orginal name", fileName);
        information.put("directory", path);
        information.put("web-directory", getWebDirectory(path + fileName));
        information.put("location", path + fileName);
        jsonObject.put(fileName, information);
        fileInformation.getFile().delete();

        return jsonObject;
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
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        return formatter.format(date);
    }

    /**
     * This method generates web directory from given path. Mainly deletes
     * prefix to first slash.
     * 
     * @param path
     *            The path on server side.
     * @return The path for web directory.
     */
    private static String getWebDirectory(String path) {
        String subString = path.substring(path.indexOf('/') + 1, path.length());

        return subString.substring(subString.indexOf('/') + 1,
                subString.length());
    }

    /**
     * This method initialize the JSON library.
     */
    public static InputStream initLibrary() {
        net.sf.json.JSONObject library = new net.sf.json.JSONObject();
        library.put(COMMENT, getCommentInformation());

        return new ByteArrayInputStream(library.toString().getBytes());

    }

    /**
     * This method checks if given character is inside map of polish letters.
     * 
     * @param ch
     *            The character to check.
     * @return True if char is different from polish map.
     */
    private static Boolean isNormalLetter(char ch) {
        Boolean isNormal = false;

        if (!polishLettersMap.containsKey(ch)) {
            isNormal = true;
        }

        return isNormal;
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
     * This method removes mp3 extension from given string.
     * 
     * @param name
     *            The string with mp3 extension.
     * @return The string without mp3 extension.
     */
    private static String removeMP3Extension(String name) {
        String stringWithoutExtension = name;
        Integer i = name.lastIndexOf('.');

        if (i > 0) {
            stringWithoutExtension = name.substring(0, i);
        }

        return stringWithoutExtension;
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
     * This method updates header in JSON library.
     * 
     * @param jsonLib
     * 
     * @return The InputStream of updated JSON library.
     */
    public static net.sf.json.JSONObject updateJsonHeader(OutputStream jsonLib) {
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject
                .fromObject(jsonLib.toString());

        if (jsonObject.containsKey(COMMENT)) {
            jsonObject.discard(COMMENT);
            jsonObject.put(COMMENT, getCommentInformation());
        }

        return jsonObject;
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
}
