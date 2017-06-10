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
                    login = login.substring(0, 1).toUpperCase() + login.substring(1, login.length()).toLowerCase();
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

            // Начало загрузки
            if (str.contains(") Receiving file \"")) {
                startFileTransfer(str);
            }
            // Конец загрузки
            if (str.contains(") Received file \"")) {
                endUpload(str);
            }

            // Ошибка загрузки
            if (str.contains(") Error receiving file ")) {
                errorUpload(str);
            }

            if (str.contains(") CLNT ")) {
                client = str.substring(42);
            }
        }
    }

    private void startFileTransfer(String data) {
        Task task = new Task(login, IDSession);
        task.startUpload(data);
        tasks.add(task);
    }

    private void endUpload(String data) {
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
            tasks.add(task);
            Helper.print(task);
            Helper.print("Check found: " + login + " - " + IDSession + " - " + "task: " + task.getFilename() +
                    " - tasks.size: " + tasks.size());
        }
    }

    private void errorUpload(String data) {
        for (Task task : tasks) {
            int startIndex = data.indexOf(") Error receiving file \"") + 24;
            File file = new File(data.substring(startIndex, data.indexOf("\"", startIndex)));
            if (task.getFullname().equals(file.getAbsolutePath())) {
                task.errorUpload();
                break;
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