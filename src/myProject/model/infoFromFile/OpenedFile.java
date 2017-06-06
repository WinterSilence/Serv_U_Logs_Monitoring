package myProject.model.infoFromFile;

import myProject.Helper;
import myProject.model.data.Session;

import java.io.*;
import java.util.*;
import java.nio.charset.Charset;

public class OpenedFile {

    private Map<String, StringBuilder> initDataMap = new HashMap<>();
    private Map<String, String> newUpdateMap = new HashMap<>();

    private String fullPath = "";

    private long initBytes;
    private String lastLine = "";
    private boolean isOffline = true;

    private int count = 0;
    private final int TRY_COUNT = 5;

    public void initDataMap(FileSource fileSource) {
        File currentFile = fileSource.getFile();
        fullPath = currentFile.getAbsolutePath();
        while (count != TRY_COUNT) {
            try (FileInputStream fis = new FileInputStream(currentFile);
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")))) {
                String currentLine;

                while ((currentLine = bufferedReader.readLine()) != null) {
                    checkAndInputStringToInitMap(currentLine);
                    lastLine = currentLine;
                }
                initBytes = currentFile.length() - lastLine.length() - 2; // минус длина строки и перенос строки !!!
//                System.out.println(initBytes);
                if (initBytes < 0) initBytes = 0;
                isOffline = false;
                return;
            } catch (IOException ex) {
                System.out.println("Try number " + (count + 1));
                Helper.log(ex);
                count++;
                Helper.pause(5);
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
                            value = new StringBuilder(string);
                            initDataMap.put(id, value.append("\n"));
                        }
                    }
                }
            } catch (NumberFormatException ex) {
                Helper.log(ex);
                System.out.println("OpenedFile.java + checkAndInputStringToInitMap");
            } catch (StringIndexOutOfBoundsException secondEx) {
//                System.out.println("Skip \n" + string + " - secondEx");
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
                    if (operationNumber != 3 && operationNumber != 6) {// && operationNumber != 20) {
                        if (newUpdateMap.get(id) != null)
                            newUpdateMap.put(id, newUpdateMap.get(id) + string + "\n");
                        else {
                            if (initDataMap.get(id) != null) {
                                newUpdateMap.put(id, initDataMap.get(id) + string + "\n");
                            } else {
                                newUpdateMap.put(id, string + "\n");
                            }
                        }
                    }
                }
            } catch (NumberFormatException ex) {
                System.out.println("OpenedFile.java + checkAndInputStringToUpdateMap");
                System.out.println(string);
                Helper.log(ex);
            } catch (StringIndexOutOfBoundsException secondEx) {
                System.out.println("Skip \n" + string);
                System.out.println(secondEx.toString());
            }
        }
    }

    public void setFullPath(String fullPath) {
        initDataMap = new TreeMap<>();
        newUpdateMap = new TreeMap<>();
        this.fullPath = fullPath;
    }

    public Map<String, String> getInitDataMap() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, StringBuilder> pair : initDataMap.entrySet()) {
            result.put(pair.getKey(),pair.getValue().toString());
        }
        return result;
    }

    public Map<String, String> getNewUpdateMap() {
        return newUpdateMap;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

}