package myProject;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Helper {
    private static final char[][] charTable = new char[65536][];
    private static File folder = new File("log");
    private static PropertiesConfiguration properties = new PropertiesConfiguration();
    private static PropertiesConfiguration defaultProperties = new PropertiesConfiguration();

    static {
        File defaultPropertyFile = new File("default.properties");
        if (!defaultPropertyFile.exists()) {
            defaultPropertyFile = new File("src/main/resources/default.properties");
        }
        defaultProperties.setFile(defaultPropertyFile);
        String userTempDir = System.getProperty("java.io.tmpdir");
        File propertiesFile = new File(userTempDir + File.separator + "workProjectProp" + File.separator + "wp.properties");
        if (!propertiesFile.exists()) {
            try {
                if (!propertiesFile.getParentFile().exists()) {
                    Files.createDirectories(propertiesFile.getParentFile().toPath());
                }
                transferFile(defaultPropertyFile, propertiesFile);
            } catch (IOException ex) {
                log(ex);
            }
        }
        properties.setFile(propertiesFile);
        try {
            defaultProperties.load();
            properties.load();
        } catch (ConfigurationException ex) {
            log(ex);
        }

    }

    static {
        if (!folder.exists() || !folder.isDirectory()) {
            try {
                Files.createDirectories(folder.toPath());
            } catch (IOException ex) {
                log(ex);
            }
        }
    }

    static {
        charTable['А'] = "A".toCharArray();
        charTable['Б'] = "B".toCharArray();
        charTable['В'] = "V".toCharArray();
        charTable['Г'] = "G".toCharArray();
        charTable['Д'] = "D".toCharArray();
        charTable['Е'] = "E".toCharArray();
        charTable['Ё'] = "E".toCharArray();
        charTable['Ж'] = "ZH".toCharArray();
        charTable['З'] = "Z".toCharArray();
        charTable['И'] = "I".toCharArray();
        charTable['Й'] = "I".toCharArray();
        charTable['К'] = "K".toCharArray();
        charTable['Л'] = "L".toCharArray();
        charTable['М'] = "M".toCharArray();
        charTable['Н'] = "N".toCharArray();
        charTable['О'] = "O".toCharArray();
        charTable['П'] = "P".toCharArray();
        charTable['Р'] = "R".toCharArray();
        charTable['С'] = "S".toCharArray();
        charTable['Т'] = "T".toCharArray();
        charTable['У'] = "U".toCharArray();
        charTable['Ф'] = "F".toCharArray();
        charTable['Х'] = "H".toCharArray();
        charTable['Ц'] = "C".toCharArray();
        charTable['Ч'] = "CH".toCharArray();
        charTable['Ш'] = "SH".toCharArray();
        charTable['Щ'] = "SH".toCharArray();
        charTable['Ъ'] = "'".toCharArray();
        charTable['Ы'] = "Y".toCharArray();
        charTable['Ь'] = "'".toCharArray();
        charTable['Э'] = "E".toCharArray();
        charTable['Ю'] = "U".toCharArray();
        charTable['Я'] = "YA".toCharArray();

        for (int i = 0; i < charTable.length; i++) {
            char idx = (char) i;
            char lower = new String(new char[]{idx}).toLowerCase().charAt(0);
            if (charTable[i] != null) {
                charTable[lower] = toLowerCase(charTable[i]);
            }
        }
    }

    public final static String EMPTY_LOGIN_FIELD = "_UNKNOWN";

    public static void log(Throwable throwable) {
        throwable.printStackTrace();
        writeStringToLog(throwable.toString());
    }

    public static void pause(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
            Helper.log(ex);
        }
    }

    private static char[] toLowerCase(char[] chars) {
        char[] r = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            r[i] = Character.toLowerCase(chars[i]);
        }
        return r;
    }

    public static String renameFromCirrilic(String filename) {
        StringBuilder sb = new StringBuilder(filename.length());
        for (int i = 0; i < filename.length(); i++) {
            char[] replace = charTable[filename.charAt(i)];
            if (replace == null) {
                sb.append(filename.charAt(i));
            } else {
                sb.append(replace);
            }
        }
        return sb.toString();
    }

    // Переименование/преобразование пути к папкам из вида H:\FTPSERVER\ в \\ftpres и т.п.

    public static String renameFolder(String folder) {
        String result = folder.toLowerCase();
        result = result.replaceFirst("h:\\\\ftpserver\\\\", "\\\\\\\\ftpres\\\\");
        result = result.replaceFirst("u:\\\\obmen-utro\\\\", "\\\\\\\\ftpres\\\\obmen-utro\\$\\\\");
        result = result.replaceFirst("e:\\\\static_folders\\\\culture", "\\\\\\\\ftpres\\\\culture\\$\\\\");
        result = result.replaceFirst("h:\\\\obmen-folders\\\\", "\\\\\\\\ftpres\\\\h\\$\\\\obmen-folders\\\\");
        result = result.replaceFirst("h:\\\\static_folders\\\\departament_region", "\\\\\\\\ftpres\\\\departament_region\\$\\\\");
        result = result.replaceFirst("j:\\\\ftp-quantel", "\\\\\\\\ftpres\\\\quantel\\$");
        return result;
    }

    public static void transferFile(File from, File to) throws IOException {
        if (from.exists()) {
            if (!to.exists()) {
                Files.copy(from.toPath(), to.toPath());
            }
            to.setLastModified(new Date().getTime());
        }
    }

    public static void writeStringToLog(String string) {
        try {
            File file = new File(folder + File.separator + "log.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            bufferedWriter.write(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()) + " - " + string + "\r\n");
            bufferedWriter.close();
        } catch (IOException ex) {
        }
    }

    public static void print(Object object) {
        System.out.println(object);
    }

    private static void writeToFileFromConsole(Object object) {
        String filename = new SimpleDateFormat("yyyy_MM_dd_HH").format(new Date());

        try {
            File file = new File(folder + File.separator + "console_" + filename + ".txt");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            bufferedWriter.write(object + " - " + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "\r\n");
            bufferedWriter.close();
        } catch (IOException ex) {
        }
    }


    public static Date yesterday() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    public static int comparingDays(Calendar calendar1, Calendar calendar2) {
        if (calendar1.get(Calendar.YEAR) != calendar2.get(Calendar.YEAR))
            return calendar1.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR);
        if (calendar1.get(Calendar.MONTH) != calendar2.get(Calendar.MONTH))
            return calendar1.get(Calendar.MONTH) - calendar2.get(Calendar.MONTH);
        return calendar1.get(Calendar.DAY_OF_MONTH) - calendar2.get(Calendar.DAY_OF_MONTH);
    }

    public static int comparingHours(Calendar calendar1, Calendar calendar2) {
        if (calendar1.get(Calendar.YEAR) != calendar2.get(Calendar.YEAR))
            return calendar1.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR);
        if (calendar1.get(Calendar.MONTH) != calendar2.get(Calendar.MONTH))
            return calendar1.get(Calendar.MONTH) - calendar2.get(Calendar.MONTH);
        if (calendar1.get(Calendar.DAY_OF_MONTH) != calendar2.get(Calendar.DAY_OF_MONTH))
            return calendar1.get(Calendar.DAY_OF_MONTH) - calendar2.get(Calendar.DAY_OF_MONTH);
        return calendar1.get(Calendar.HOUR_OF_DAY) - calendar2.get(Calendar.HOUR_OF_DAY);
    }

    public static void createCurrentDateFolder(File folder) {
        if (!folder.exists()) {
            try {
                Files.createDirectories(folder.toPath());
            } catch (IOException ex) {
                Helper.log(ex);
            }
        }
    }

    public static PropertiesConfiguration getProperties() {
        return properties;
    }

    public static PropertiesConfiguration getDefaultProperties() {
        return defaultProperties;
    }
}