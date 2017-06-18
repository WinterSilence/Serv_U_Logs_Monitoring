package myProject.model;

import myProject.Helper;
import myProject.model.data.Session;
import myProject.model.data.Task;
import myProject.model.data.UploadState;
import myProject.model.infoFromFile.FileSource;
import myProject.model.infoFromFile.OpenedFile;

import java.util.*;

public class MyModel {

    private Map<String, Session> allSessionsMap;
    private List<Task> completedTasks;
    private List<Task> uploadingTasks;
    private List<Task> uncompletedTasks;

    private OpenedFile openedFile = new OpenedFile();

    //  Ненужные учётки
    private List<String> bannedLogins = new ArrayList<>();

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
    }

    public MyModel() {
        allSessionsMap = new HashMap<>();
        uploadingTasks = new ArrayList<>();
        completedTasks = new ArrayList<>();
        uncompletedTasks = new ArrayList<>();
    }

    public void setFullPath(String fullPath) {
        openedFile.setFullPath(fullPath);
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

    public void init(FileSource fileSource) {
        openedFile.initDataMap(fileSource);

        Iterator<Map.Entry<String, StringBuilder>> iterator = openedFile.getInitDataMap().entrySet().iterator();
//        for (Map.Entry<String, StringBuilder> pair : openedFile.getInitDataMap().entrySet()) {
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

//        Helper.print(allSessionsMap.size());
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

        List<String> list = new ArrayList<>();
        Map<String, StringBuilder> map = openedFile.getNewUpdateMap();
        list.addAll(map.keySet());
        Helper.print(map.size() + " updated sessions - " + list);

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

            allSessionsMap.put(key, session);

            if (session.isOffline()) {
                iterator.remove();
            }
        }

        for (Session session : allSessionsMap.values()) {
            if (session.getLogin().equals(Helper.EMPTY_LOGIN_FIELD)) {
                Helper.print(session.getIDSession() + " --- ");
                Helper.print(session.getData());
                Helper.print("*******************************");
            }
        }
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
                        uploadingTasks.add(task);
                    } else {
                        task.setStateErrorUpload();
                    }
                }
                if (task.getState().equals(UploadState.END_UPLOAD)) {
//                    for (Task completedTask : completedTasks) {
//                        if (completedTask.getIDSession().equals(task.getIDSession()) &&
//                                completedTask.getFilename().equals(task.getFilename())) {
//                            System.out.println(task.getFilename());
//                        }
//                    }
                    completedTasks.add(task);
                }
                if (task.getState().equals(UploadState.ERROR_UPLOAD)) {
                    uncompletedTasks.add(task);
                }
            }
        }
    }

    public Map<String, Session> getOnlineSessionsMap() {
        Map<String, Session> result = new HashMap<>();
        for (Map.Entry<String, Session> pair : allSessionsMap.entrySet()) {
            if (!pair.getValue().isOffline()) {
                result.put(pair.getKey(), pair.getValue());
            }
        }
        return result;
    }

    public void reset() {
        allSessionsMap = new HashMap<>();
    }

    public boolean isOffline() {
        return openedFile.isOffline();
    }

    public void setOffline() {
        openedFile.setOffline(true);
    }
}