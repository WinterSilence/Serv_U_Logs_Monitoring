package myProject.model;

import myProject.Helper;
import myProject.model.data.Session;
import myProject.model.data.Task;
import myProject.model.data.UploadState;
import myProject.model.infoFromFile.FileSource;
import myProject.model.infoFromFile.OpenedFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyModel {

    private Map<String, Session> allSessionsMap = new HashMap<>();
    private List<Task> completedTasks = new ArrayList<>();
    private List<Task> uploadingTasks = new ArrayList<>();
    private List<Task> uncompletedTasks = new ArrayList<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock w = lock.writeLock();
    private final Lock r = lock.writeLock();


    private OpenedFile openedFile = new OpenedFile();

    //  Ненужные учётки
    private List<String> bannedLogins = new ArrayList<>();
    private List<String> bannedFolders = new ArrayList<>();
    private FileSource fileSource;

    {
        File bannedLoginsFile = new File("banned/bannedLogins.txt");
        if (!bannedLoginsFile.exists()) {
            bannedLoginsFile = new File("src/main/resources/banned/bannedLogins.txt");
        }
        try (BufferedReader buf = new BufferedReader(new FileReader(bannedLoginsFile))) {
            while (buf.ready()) {
                bannedLogins.add(buf.readLine());
            }
        } catch (IOException ex) {
            Helper.log(ex);
        }

        File bannedFoldersFile = new File("banned/bannedFolders.txt");
        if (!bannedFoldersFile.exists()) {
            bannedFoldersFile = new File("src/main/resources/banned/bannedFolders.txt");
        }
        try (BufferedReader buf = new BufferedReader(new FileReader(bannedFoldersFile))) {
            while (buf.ready()) {
                bannedFolders.add(buf.readLine());
            }
        } catch (IOException ex) {
            Helper.log(ex);
        }
    }

    public void setTodayFullPath(String fullPath) {
        openedFile.setTodayFileFullPath(fullPath);
    }

    public List<Task> getCompletedTasks() {
        r.lock();
        try {
            return completedTasks;
        } finally {
            r.unlock();
        }
    }

    public List<Task> getUncompletedTasks() {
        w.lock();
        try {
            uncompletedTasks.sort(new Comparator<Task>() {
                @Override
                public int compare(Task task1, Task task2) {
                    long task1Time = task1.getTimeStart().getTime();
                    long task2Time = task2.getTimeStart().getTime();
                    return task1Time > task2Time ? -1 : 1;
                }
            });
        } finally {
            w.unlock();
        }
        r.lock();
        try {
            return uncompletedTasks;
        } finally {
            r.unlock();
        }
    }

    public List<Task> getUploadingTasks() {
        return uploadingTasks;
    }

    public void setFileSource(FileSource fileSource) {
        this.fileSource = fileSource;
    }

    // Init today
    public void initToday() {
        reset();
        File mainFolderFile = new File(fileSource.getSourceFolder());

        openedFile.resetInitMap();
        openedFile.initTodayDataMap(mainFolderFile);

        Iterator<Map.Entry<String, StringBuilder>> iterator = openedFile.getInitDataMap().entrySet().iterator();
        iterateData(iterator);
    }

    public void initDefault() {
        initToday();
        File mainFolderFile = new File(fileSource.getSourceFolder());

        openedFile.initAnyDateDataMap(mainFolderFile, Helper.yesterday());

        Iterator<Map.Entry<String, StringBuilder>> iterator = openedFile.getInitDataMap().entrySet().iterator();
        iterateData(iterator);
    }

    // Init with any date
/*
    public void init(Date... dates) {
        reset();

        File mainFolderFile = new File(fileSource.getSourceFolder());

        openedFile.resetInitMap();
        for (Date date : dates) {
            openedFile.initAnyDateDataMap(mainFolderFile, date);
        }

        Iterator<Map.Entry<String, StringBuilder>> iterator = openedFile.getInitDataMap().entrySet().iterator();
        iterateData(iterator);
    }
*/

    public void init(File file) {
        reset();

        openedFile.resetInitMap();
        openedFile.initDataMapByFile(file);

        Iterator<Map.Entry<String, StringBuilder>> iterator = openedFile.getInitDataMap().entrySet().iterator();
        iterateData(iterator);
    }

    private void iterateData(Iterator<Map.Entry<String, StringBuilder>> iterator) {
        while (iterator.hasNext()) {

            Map.Entry<String, StringBuilder> pair = iterator.next();
            String key = pair.getKey();                                      // ID Session
            StringBuilder value = pair.getValue();                           // Data может быть неполной (не сначала)

            Session session = new Session(key, value.toString());

            if (session.isOffline() && session.isEmpty()) {
                iterator.remove();
                continue;
            }
            w.lock();
            try {
                allSessionsMap.put(key, session);
            } finally {
                w.unlock();
            }
        }
        tasksUpdate();
    }


    public void update() {
        dataUpdate();
        removeBannedSessionsAndFolders();
        tasksUpdate();
    }

    private void removeBannedSessionsAndFolders() {
        w.lock();
        try {
            Iterator<Map.Entry<String, Session>> iterator = allSessionsMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Session> pair = iterator.next();
                Session value = pair.getValue();                              // Session
                if (bannedLogins.contains(value.getLogin().toLowerCase())) {
                    iterator.remove();
                }
                for (Task task : value.getTasks()) {
                    if (bannedFolders.stream().anyMatch(s -> task.getFolder().toLowerCase().contains(s.toLowerCase()))) {
                        iterator.remove();
                        break;
                    }
                }
            }
        } finally {
            w.unlock();
        }
    }

    private void dataUpdate() {
        openedFile.update();
        // Проверка количества онлайн сессий
/*
        List<String> list = new ArrayList<>();
        Map<String, StringBuilder> map = openedFile.getNewUpdateMap();
        list.addAll(map.keySet());
        Helper.print(map.size() + " updated sessions - " + list);
*/
        int count = 0;

        Iterator<Map.Entry<String, StringBuilder>> iterator = openedFile.getNewUpdateMap().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, StringBuilder> pair = iterator.next();
            String key = pair.getKey();                                      // ID Session
            StringBuilder value = pair.getValue();                           // Data может быть неполной (не сначала)

            Session session = new Session(key, value.toString());

            if ((session.isOffline() && session.isEmpty())) {
                iterator.remove();
                w.lock();
                try {
                    allSessionsMap.remove(key);
                } finally {
                    w.unlock();
                }
                continue;
            }
            r.lock();
            try {
                if (allSessionsMap.containsKey(key) && allSessionsMap.get(key).getData().equals(value.toString())) {
                    continue;
                }
            } finally {
                r.unlock();
            }
            w.lock();
            try {
                allSessionsMap.put(key, session);
            } finally {
                w.unlock();
            }
            count++;

            if (session.isOffline()) {
                iterator.remove();
            }
        }

        Helper.print("Check updated - " + count);
    }

    private void tasksUpdate() {
        w.lock();
        try {
            uploadingTasks = new ArrayList<>();
            completedTasks = new ArrayList<>();
            uncompletedTasks = new ArrayList<>();
        } finally {
            w.unlock();
        }
        r.lock();
        try {
            for (Session session : allSessionsMap.values()) {

                for (Task task : session.getTasks()) {
                    if (task.getState().equals(UploadState.START_UPLOAD) &&
                            allSessionsMap.containsKey(task.getIDSession())) {
                        if (!allSessionsMap.get(task.getIDSession()).isOffline()) {
                            File fileTask = new File(Helper.renameFolder(task.getFolder().toLowerCase()) + File.separator + task.getFilename());
                            if (fileTask.exists()) {
                                if (new Date().getTime() - fileTask.lastModified() < 8 * 60 * 60000) {
                                    uploadingTasks.add(task);
                                } else {
                                    System.err.println("Время последнего изменения сессиии больше восьми часов = " +
                                            (new Date().getTime() - fileTask.lastModified()) / (60 * 60000));
                                    task.setState(UploadState.END_UPLOAD);
                                }
                            } else {
                                Helper.print("not exist - " + fileTask.getAbsolutePath());
                                task.setState(UploadState.ERROR_UPLOAD);
                            }
                        } else {
                            task.setState(UploadState.ERROR_UPLOAD);
                        }
                    }
                    if (task.getState().equals(UploadState.END_UPLOAD)) {
                        w.lock();
                        try {
                            completedTasks.add(task);
                        } finally {
                            w.unlock();
                        }
                    }
                    if (task.getState().equals(UploadState.ERROR_UPLOAD)) {
                        w.lock();
                        try {
                            uncompletedTasks.add(task);
                        } finally {
                            w.unlock();
                        }
                    }
                }
            }
        } finally {
            r.unlock();
        }
        checkTaskUnknownLogin(uncompletedTasks);
        checkTaskUnknownLogin(completedTasks);
    }

    private void checkTaskUnknownLogin(List<Task> tasks) {
        for (Task task : tasks) {
            if (task.getLogin().equals(Helper.EMPTY_LOGIN_FIELD)) {
                String taskFolder = task.getFolder().toLowerCase();
                String taskFilename = task.getFilename().toLowerCase();
                if (taskFolder.endsWith("upload_inet")) task.setLogin("Reporter");
                else if (taskFilename.contains("spb")) task.setLogin("Spb");
                else if (taskFilename.contains("rostovdon")) task.setLogin("Rostovdon");
                for (Task uploadingTask : uploadingTasks) {
                    if (task.getFullname().equals(uploadingTask.getFullname())
                            && (task.getState().equals(UploadState.END_UPLOAD) || task.getState().equals(UploadState.ERROR_UPLOAD))) {
                        task.setLogin(uploadingTask.getLogin());
                        uploadingTask.setState(task.getState());
                        uploadingTask.setSpeed(task.getSpeed());
                        uploadingTask.setTimeEnd(task.getTimeEnd());
                        uploadingTask.setUnitFile(task.getUnitFile());
                        break;
                    }
                }
            }
        }
    }

    public Map<String, Session> getOnlineSessionsMap() {
        Map<String, Session> result = new HashMap<>();
        r.lock();
        try {
            for (Map.Entry<String, Session> pair : allSessionsMap.entrySet()) {
                if (!pair.getValue().isOffline()) {
                    w.lock();
                    try {
                        result.put(pair.getKey(), pair.getValue());
                    } finally {
                        w.unlock();
                    }
                }
                if (pair.getValue().getLogins().size() > 1) {
                    Helper.writeStringToLog("Несколько логинов за одну сессию!!!!!!!! Номер сессии - " + pair.getKey()); //todo test!!!
                }
            }
        } finally {
            r.unlock();
        }
        return result;
    }

    public Session getSessionById(String id) {
        Session result = null;
        r.lock();
        try {
            for (Map.Entry<String, Session> pair : allSessionsMap.entrySet()) {
                w.lock();
                try {
                    if (pair.getKey().equals(id)){
                        return pair.getValue();
                    }
                } finally {
                    w.unlock();
                }
            }
        } finally {
            r.unlock();
        }
        return null;
    }


    private void reset() {
        w.lock();
        try {
            allSessionsMap = new HashMap<>();
            uploadingTasks = new ArrayList<>();
            completedTasks = new ArrayList<>();
            uncompletedTasks = new ArrayList<>();
        } finally {
            w.unlock();
        }
    }

    public boolean isOffline() {
        return openedFile.isOffline();
    }

    public void setOffline() {
        openedFile.setOffline();
    }
}