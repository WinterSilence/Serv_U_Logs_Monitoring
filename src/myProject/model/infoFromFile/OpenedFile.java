package myProject.model.infoFromFile;

import myProject.Helper;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.nio.charset.Charset;

public class OpenedFile {

    private Map<String, StringBuilder> initDataMap;
    private Map<String, StringBuilder> newUpdateMap = new HashMap<>();

    private String todayFileFullPath = "";

    private long initBytes;
    private String lastLine = "";
    private boolean isOffline = true;

    private int count = 0;
    private final int TRY_COUNT = 5;

    public void initTodayDataMap(File folder) {
        String todayDayFilename = new SimpleDateFormat("yyyy_MM_dd").format(new Date()) + ".log";
        while (count != TRY_COUNT) {
            try {
                File todayFile = new File(folder + File.separator + todayDayFilename);
                todayFileFullPath = todayFile.getAbsolutePath();
                lastLine = initMapByDate(todayFile);
                initBytes = todayFile.length() - lastLine.length() - 2; // минус длина строки и перенос строки !!!
                if (initBytes < 0) initBytes = 0;
                isOffline = false;
                break;
            } catch (IOException ex) {
                Helper.log(ex);
                count++;
                Helper.print("Try number " + (count));
                Helper.pause(5);
            }
        }
    }

    public void initAnyDateDataMap(File folder, Date anyDate) {
        String yesterdayDayFilename = new SimpleDateFormat("yyyy_MM_dd").format(anyDate) + ".log";

        File yesterdayFile = new File(folder + File.separator + yesterdayDayFilename);
        try {
            initMapByDate(yesterdayFile);
        } catch (IOException ex) {
            Helper.log(ex);
        }
    }

    public void resetInitMap(){
        initDataMap = new HashMap<>();
    }

    private String initMapByDate(File dateFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(dateFile);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")))) {
            String currentLine;
            String result = "";
            while ((currentLine = bufferedReader.readLine()) != null) {
                checkAndInputStringToInitMap(currentLine);
                result = currentLine;
            }
            return result;
        }
    }

    public void update() {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(todayFileFullPath, "r")) {

            randomAccessFile.seek(initBytes);
            String currentLine = null;

            while ((currentLine = randomAccessFile.readLine()) != null) {
                currentLine = new String(currentLine.getBytes("ISO-8859-1"), "UTF-8");
                if (!currentLine.equals(lastLine)) {
                    checkAndInputStringToUpdateMap(currentLine);
                }
                lastLine = currentLine;
            }
            initBytes = randomAccessFile.length() - lastLine.getBytes("UTF-8").length - 2;
            if (initBytes < 0) initBytes = 0;
        } catch (IOException ex) {
            Helper.log(ex);
        }
    }

    private void checkAndInputStringToInitMap(String string) {
        if (string.length() > 35) {
            try {
                String id = string.substring(string.indexOf(" - (") + 4, string.indexOf(") "));
                int checkID = Integer.parseInt(id);
                if (id.matches("\\d+")) {
                    int operationNumber = Integer.parseInt(string.substring(string.indexOf("[") + 1, string.indexOf("] ")));

                    if (operationNumber != 3 && operationNumber != 6) {

                        StringBuilder value = initDataMap.get(id);

                        if (value != null) {
                            initDataMap.put(id, value.append(string).append("\n"));
                        } else {
                            value = new StringBuilder(string + "\n");
                            initDataMap.put(id, value);
                        }
                    }
                }
            } catch (NumberFormatException ex) {
                Helper.log(ex);
                Helper.print("OpenedFile.java + checkAndInputStringToInitMap");
            } catch (StringIndexOutOfBoundsException secondEx) {
//                Helper.print("Skip \n" + string + " - secondEx");
//                Helper.log(secondEx);
//   Пропускаем строку типа:   Event: FILE_UPLOAD (File upload OK Event - EMAIL); Type: EMAIL; To: info_arrive_ftp@vgtrk.com; smena-ogs@vgtrk.com
            }
        }
    }

    private void checkAndInputStringToUpdateMap(String string) {
        if (string.length() > 35) {
            try {
                String id = string.substring(string.indexOf(" - (") + 4, string.indexOf(") "));
                int checkID = Integer.parseInt(id);
                if (id.matches("\\d+")) {
                    int operationNumber = Integer.parseInt(string.substring(string.indexOf("[") + 1, string.indexOf("] ")));

                    if (operationNumber != 3 && operationNumber != 6) {
                        if (newUpdateMap.get(id) != null)
                            newUpdateMap.put(id, newUpdateMap.get(id).append(string).append("\n"));
                        else {
                            if (initDataMap.get(id) != null) {
                                newUpdateMap.put(id, initDataMap.get(id).append(string).append("\n"));
                            } else {
                                newUpdateMap.put(id, new StringBuilder(string + "\n"));
                            }
                        }
                    }
                }
            } catch (NumberFormatException ex) {
                Helper.print("OpenedFile.java + checkAndInputStringToUpdateMap");
                Helper.print(string);
                Helper.log(ex);
            } catch (StringIndexOutOfBoundsException secondEx) {
                Helper.print("Skip \n" + string);
                Helper.print(secondEx.toString());
            }
        }
    }

    public void setTodayFileFullPath(String todayFileFullPath) {
        initDataMap = new HashMap<>();
        newUpdateMap = new HashMap<>();
        this.todayFileFullPath = todayFileFullPath;
    }

    public Map<String, StringBuilder> getInitDataMap() {
        return initDataMap;
    }

    public Map<String, StringBuilder> getNewUpdateMap() {
        return newUpdateMap;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline() {
        isOffline = true;
    }
}