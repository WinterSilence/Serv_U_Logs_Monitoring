package myProject.model.infoFromFile;

import javafx.application.Platform;
import myProject.Helper;

import java.io.*;
import java.util.*;
import java.nio.charset.Charset;

public class OpenedFile {

    private Map<String, StringBuilder> initDataMap;
    private Map<String, StringBuilder> yesterdayDataMap;
    private Map<String, StringBuilder> newUpdateMap = new HashMap<>();

    private String fullPath = "";

    private long initBytes;
    private String lastLine = "";
    private boolean isOffline = true;

    private int count = 0;
    private final int TRY_COUNT = 5;

    public void initDataMap(FileSource fileSource) {

        initDataMap = new HashMap<>();
        yesterdayDataMap = new HashMap<>();
        List<File> allFiles = fileSource.getFiles();
        File yesterdayFile = allFiles.get(1);
        if (yesterdayFile != null) {
            try (FileInputStream fis = new FileInputStream(yesterdayFile);
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")))) {
                String currentLine;

                while ((currentLine = bufferedReader.readLine()) != null) {
                    checkAndInputStringToMap(currentLine, yesterdayDataMap);
                }
            } catch (IOException ex) {
                Helper.print("Try number " + (count + 1));
                Helper.log(ex);
                count++;
                Helper.pause(5);
            }

        }
        Helper.print("Yesterday - " + yesterdayDataMap.size());

        File currentFile = allFiles.get(0);
        if (currentFile != null) {
            fullPath = currentFile.getAbsolutePath();
            while (count != TRY_COUNT) {
                try (FileInputStream fis = new FileInputStream(currentFile);
                     BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")))) {
                    String currentLine;

                    while ((currentLine = bufferedReader.readLine()) != null) {
                        checkAndInputStringToMap(currentLine, initDataMap);
                        lastLine = currentLine;
                    }
                    initBytes = currentFile.length() - lastLine.length() - 2; // минус длина строки и перенос строки !!!
//                Helper.print(initBytes);
                    if (initBytes < 0) initBytes = 0;
                    isOffline = false;
                    Helper.print("Today - " + initDataMap.size());
                    return;
                } catch (IOException ex) {
                    Helper.print("Try number " + (count + 1));
                    Helper.log(ex);
                    count++;
                    Helper.pause(5);
                }
            }
        } else {
            try {
                throw new FileNotFoundException();
            } catch (FileNotFoundException ex) {
                Helper.log(ex);
                Platform.exit();
            }
        }
    }

    public void update() {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(fullPath, "r")) {

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

    private void checkAndInputStringToMap(String string, Map<String, StringBuilder> map) {
        if (string.length() > 35) {
            try {
                String id = string.substring(string.indexOf(" - (") + 4, string.indexOf(") "));
                int checkID = Integer.parseInt(id);
                if (id.matches("\\d+")) {
                    int operationNumber = Integer.parseInt(string.substring(string.indexOf("[") + 1, string.indexOf("] ")));

                    if (operationNumber != 3 && operationNumber != 6) {

                        StringBuilder value = initDataMap.get(id);

                        if (value != null) {
                            map.put(id, value.append(string).append("\n"));
                        } else {
                            value = new StringBuilder(string + "\n");
                            map.put(id, value);
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

    public void setFullPath(String fullPath) {
        initDataMap = new TreeMap<>();
        newUpdateMap = new TreeMap<>();
        this.fullPath = fullPath;
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

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

}