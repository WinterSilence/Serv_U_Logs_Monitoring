package myProject.model;

import myProject.Helper;
import myProject.model.data.Session;
import myProject.model.data.Task;
import myProject.model.data.UploadState;
import myProject.model.infoFromFile.FileSource;
import myProject.model.infoFromFile.OpenedFile;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyModel {

    private Map<String, Session> allSessionsMap = new ConcurrentHashMap<>();
    private List<Task> completedTasks = new CopyOnWriteArrayList<>();
    private List<Task> uploadingTasks = new CopyOnWriteArrayList<>();
    private List<Task> uncompletedTasks = new CopyOnWriteArrayList<>();

    private OpenedFile openedFile = new OpenedFile();

    //  Ненужные учётки
    private List<String> bannedLogins = new ArrayList<>();
    private FileSource fileSource;

    {
        bannedLogins.add("mediagrid-reklama");
        bannedLogins.add("mediagrid-mchs");
        bannedLogins.add("promo");
        bannedLogins.add("vashetv_w");
        bannedLogins.add("vashetv_r");
        bannedLogins.add("oformlenie");
        bannedLogins.add("loader");
        bannedLogins.add("rtrpl-europe");
        bannedLogins.add("quantelr");
        bannedLogins.add("kino");
        bannedLogins.add("kinopokaz-m24");
        bannedLogins.add("m24-newsroom");
    }

    public void setTodayFullPath(String fullPath) {
        openedFile.setTodayFileFullPath(fullPath);
    }

    public List<Task> getCompletedTasks() {
        return completedTasks;
    }

    public List<Task> getUncompletedTasks() {
        uncompletedTasks.sort(new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                long task1Time = task1.getTimeStart().getTime();
                long task2Time = task2.getTimeStart().getTime();
                return task1Time > task2Time ? -1 : 1;
            }
        });
        return uncompletedTasks;
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
        while (iterator.hasNext()) {

            Map.Entry<String, StringBuilder> pair = iterator.next();
            String key = pair.getKey();                                      // ID Session
            StringBuilder value = pair.getValue();                           // Data может быть неполной (не сначала)

            Session session = new Session(key, value.toString());

            if (session.isOffline() && session.isEmpty()) {
                iterator.remove();
                continue;
            }

            allSessionsMap.put(key, session);
        }
        removeBannedSessions();
        tasksUpdate();
    }

    public void initDefault() {
        initToday();
        File mainFolderFile = new File(fileSource.getSourceFolder());

        openedFile.initAnyDateDataMap(mainFolderFile, Helper.yesterday());

        Iterator<Map.Entry<String, StringBuilder>> iterator = openedFile.getInitDataMap().entrySet().iterator();
        while (iterator.hasNext()) {

            Map.Entry<String, StringBuilder> pair = iterator.next();
            String key = pair.getKey();                                      // ID Session
            StringBuilder value = pair.getValue();                           // Data может быть неполной (не сначала)

            Session session = new Session(key, value.toString());

            if (session.isOffline() && session.isEmpty()) {
                iterator.remove();
                continue;
            }

            allSessionsMap.put(key, session);
        }
        removeBannedSessions();
        tasksUpdate();
    }

    // Init with any date
    public void init(Date... dates) {
        reset();

        File mainFolderFile = new File(fileSource.getSourceFolder());

        openedFile.resetInitMap();
        for (Date date : dates) {
            openedFile.initAnyDateDataMap(mainFolderFile, date);
        }

        Iterator<Map.Entry<String, StringBuilder>> iterator = openedFile.getInitDataMap().entrySet().iterator();
        while (iterator.hasNext()) {

            Map.Entry<String, StringBuilder> pair = iterator.next();
            String key = pair.getKey();                                      // ID Session
            StringBuilder value = pair.getValue();                           // Data может быть неполной (не сначала)

            Session session = new Session(key, value.toString());

            if (session.isOffline() && session.isEmpty()) {
                iterator.remove();
                continue;
            }

            allSessionsMap.put(key, session);
        }
        removeBannedSessions();
        tasksUpdate();
    }


    public void update() {
        dataUpdate();
        removeBannedSessions();
        tasksUpdate();
    }

    private void removeBannedSessions() {
        Iterator<Map.Entry<String, Session>> iterator = allSessionsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Session> pair = iterator.next();
            Session value = pair.getValue();                              // Session
            if (bannedLogins.contains(value.getLogin().toLowerCase())) {
                iterator.remove();
            }
        }
    }

    private void dataUpdate() {
        openedFile.update();
        // Проверка колическтва онлайн сессий
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
                allSessionsMap.remove(key);
                continue;
            }

            if (allSessionsMap.containsKey(key) && allSessionsMap.get(key).getData().equals(value.toString())) {
                continue;
            }
            allSessionsMap.put(key, session);
            count++;

            if (session.isOffline()) {
                iterator.remove();
            }
        }

        Helper.print("Check updated - " + count);
    }

    private void tasksUpdate() {
        uploadingTasks = new ArrayList<>();
        completedTasks = new ArrayList<>();
        uncompletedTasks = new ArrayList<>();
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
                                System.err.println("Время последнего изменения сессиии большле восьми часов = " +
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
                if (task.getLogin().equals(Helper.EMPTY_LOGIN_FIELD)){
                    checkTaskUnknownLogin(task);
                }
                if (task.getState().equals(UploadState.END_UPLOAD)) {
                    completedTasks.add(task);
                }
                if (task.getState().equals(UploadState.ERROR_UPLOAD)) {
                    uncompletedTasks.add(task);
                }
            }
        }

    }

    private void checkTaskUnknownLogin(Task task){
        String taskFolder = task.getFolder().toLowerCase();
        String taskFilename = task.getFilename().toLowerCase();
        if (taskFolder.endsWith("upload_inet")) task.setLogin("Reporter");
        else if (taskFilename.contains("spb")) task.setLogin("Spb");
        else if (taskFilename.contains("rostovdon")) task.setLogin("Rostovdon");
    }

    public Map<String, Session> getOnlineSessionsMap() {
        Map<String, Session> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, Session> pair : allSessionsMap.entrySet()) {
            if (!pair.getValue().isOffline()) {
                result.put(pair.getKey(), pair.getValue());
            }
        }
        return result;
    }

    private void reset() {
        allSessionsMap = new HashMap<>();
        uploadingTasks = new ArrayList<>();
        completedTasks = new ArrayList<>();
        uncompletedTasks = new ArrayList<>();
    }

    public boolean isOffline() {
        return openedFile.isOffline();
    }

    public void setOffline() {
        openedFile.setOffline();
    }
}