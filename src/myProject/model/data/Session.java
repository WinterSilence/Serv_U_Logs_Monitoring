package myProject.model.data;

import myProject.Helper;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Session {

    private String IPAddress;
    private String login;
    private String IDSession;
    private Date connectionTime;
    private List<Task> tasks;
    private String client = "";

    private String data;

    public Session(String IDSession, String data) {
        this.IDSession = IDSession;
        this.tasks = new ArrayList<>();
        this.data = data;
        init();
    }

    private void init() {

        if (data.contains("\" logged in")) {
            login = initLogin();
        } else {
            if (data.contains("\" logged out")) {
                int endIndex = data.indexOf("\" logged out");
                int startIndex = data.substring(0, endIndex).lastIndexOf("\"") + 1;
                login = data.substring(startIndex, endIndex);
                login = login.substring(0, 1).toUpperCase() + login.substring(1, login.length()).toLowerCase();
            } else {
                if (data.contains(" USER ")) {
                    int startIndex = data.indexOf(" USER ") + 6;
                    int endIndex = startIndex + data.substring(startIndex).indexOf("\n");
                    login = data.substring(startIndex, endIndex);
                    login = login.length() == 0 ? Helper.EMPTY_LOGIN_FIELD :
                            login.substring(0, 1).toUpperCase() + login.substring(1, login.length()).toLowerCase();
                } else {
                    login = Helper.EMPTY_LOGIN_FIELD;
                }
            }
        }

        if (data.contains(") Connected to ")) {
            int ipAddressStart = data.indexOf(") Connected to ") + 15;
            int ipAddressEnd = data.indexOf(" (", ipAddressStart);
            IPAddress = data.substring(ipAddressStart, ipAddressEnd);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyy HH:mm:ss", Locale.US);
            connectionTime = sdf.parse(data.substring(9, 26));
        } catch (ParseException ex) {
            Helper.log(ex);
        }

        initTasks();
    }

    public boolean isLoggedIn() {
        return data.contains("\" logged in") || data.contains(") Connected to ");
    }

    private String initLogin() {
        if (data.contains("User \"")) {
            int startIndex = data.indexOf("User \"") + 6;
            int endIndex = startIndex + data.substring(startIndex).indexOf("\"");

            String username = data.substring(startIndex, endIndex);
            username = username.substring(0, 1).toUpperCase() + username.substring(1, username.length()).toLowerCase();
            return username;
        }
        return Helper.EMPTY_LOGIN_FIELD;
    }

    private void initTasks() {
        for (String str : data.split("\n")) {

            // Начало скачивания
//            if (str.contains(" Sending file \"")) {
//                startFileTransfer("Start downloading", str);
//            }

            // Начало загрузки 1
            if (str.contains(") Receiving file \"")) { // STOR !!!
                startFileTransfer(str);
            }
/*
            // Начало загрузки 2
            if (str.contains(" STOR ")) {
                int startIndex = str.indexOf(" STOR ") + 6;
                int endIndex = str.length();
                String filename = str.substring(startIndex, endIndex);
                for (Task task : tasks) {
                    if (task.getFilename().equals(filename) && IDSession.equals(task.getIDSession())) {
                        System.out.println("**************************************");
                        System.out.println(filename + " - STOR check " + IDSession);
                        System.out.println(login);
//                        System.out.println(data);
                        System.out.println("**************************************");
                    } else {

                    }
                }
            }
*/
            // Конец загрузки
            if (str.contains(") Received file \"")) {
                endUpload(str);
            }

            // Ошибка загрузки
            if (str.contains(") Error receiving file ")) {
                errorUpload(str);
            }

            // Ошибка загрузки при нехватке места (другой синтаксис в логе)
            if (str.contains(" Sorry, insufficient disk space available - receive file ")) {
                insufficientSpaceErrorUpload(str);
            }

            if (str.contains(") CLNT ")) {
                client = str.substring(42);
            }
        }
    }

    private void startFileTransfer(String data) {
        Task task = new Task(login, IDSession);
        task.startUpload(data);
        addTask(task);
    }

    private void addTask(Task taskToAdd) {
/*
        for (Task task : tasks) {
            if (task.getFilename().equals(taskToAdd.getFilename())){
                tasks.remove(task);
                break;
            }
        }
*/
        tasks.add(taskToAdd);
    }

    private void endUpload(String data) {
        // End uploading exists, but have no start upload
        boolean found = false;
        for (Task task : tasks) {
            int startIndex = data.indexOf(" Received file \"") + 16;
            File file = new File(data.substring(startIndex, data.indexOf("\"", startIndex)));
            if (task.getFullname().equals(file.getAbsolutePath())) {
                task.endUpload(data);
                found = true;
                break;
            }
        }
        if (!found) {
            Task task = new Task(login, IDSession);
            task.startUpload(data);
            task.endUpload(data);
            addTask(task);
/*
            Helper.print(task);
            Helper.print("Check found: " + login + " - " + IDSession + " - " + "task: " + task.getFilename() +
                    " - tasks.size: " + tasks.size());
*/
        }
    }

    private void errorUpload(String data) {
        for (Task task : tasks) {
            int startIndex = data.indexOf(") Error receiving file \"") + 24;
            File file = new File(data.substring(startIndex, data.indexOf("\"", startIndex)));
            if (task.getFullname().equals(file.getAbsolutePath())) {
                task.setState(UploadState.ERROR_UPLOAD);
                break;
            }
        }
    }

    private void insufficientSpaceErrorUpload(String data) {
        for (Task task : tasks) {
            int startIndex = data.indexOf(" Sorry, insufficient disk space available - receive file ") + 57;
            int endIndex = data.lastIndexOf(" aborted.");
            File file = new File(data.substring(startIndex, endIndex));
            if (task.getFilename().equals(file.getName()) && task.getState() != UploadState.ERROR_UPLOAD) {
                task.setState(UploadState.ERROR_UPLOAD);
            }
        }
    }


    public void setData(String data) {
        this.data = data;
    }

    public String getLogin() {
        return login;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    // for test
    public String getData() {
        return data;
    }

    public boolean isEmpty() {
        return tasks.size() == 0;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public String getIDSession() {
        return IDSession;
    }

    public boolean isOffline() {
        return data.contains(" Closed session") || data.contains("\" logged out");
    }

    public Date getConnectionTime() {
        return connectionTime;
    }

    public String getTime() {
        return new SimpleDateFormat("HH:mm:ss").format(connectionTime);
    }

    public String getClient() {
        return client;
    }
}