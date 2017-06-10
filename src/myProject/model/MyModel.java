package myProject.model;

import myProject.Helper;
import myProject.model.data.Session;
import myProject.model.data.Task;
import myProject.model.data.UploadState;
import myProject.model.infoFromFile.FileSource;
import myProject.model.infoFromFile.OpenedFile;

import java.io.FileNotFoundException;
import java.util.*;

public class MyModel {

    private Map<String, Session> allSessionsMap;
    private Set<Task> completedTasks;
    private Set<Task> uploadingTasks;
    private Set<Task> uncompletedTasks;

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
        uploadingTasks = new HashSet<>();
        completedTasks = new HashSet<>();
        uncompletedTasks = new HashSet<>();
    }

    public void setFullPath(String fullPath) {
        openedFile.setFullPath(fullPath);
    }

    public List<Task> getCompletedTasks() {
        return new ArrayList<>(completedTasks);
    }

    public List<Task> getUncompletedTasks() {
        return new ArrayList<>(uncompletedTasks);
    }

    public List<Task> getUploadingTasks() {
        return new ArrayList<>(uploadingTasks);
    }

    public void init(FileSource fileSource) {
        openedFile.initDataMap(fileSource);
        for (Map.Entry<String, String> pair : openedFile.getInitDataMap().entrySet()) {
            Session session = new Session(pair.getKey(), pair.getValue());

            if (session.isOffline() && session.isEmpty()) continue;

            allSessionsMap.put(pair.getKey(), session);
        }
        Helper.print(allSessionsMap.size());
        removeBannedSessions();
        getTasksUpdate();
    }

    public void update() {
        getDataUpdates();
        removeBannedSessions();
        getTasksUpdate();
    }

    private void removeBannedSessions() {
        Iterator iterator = allSessionsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Session> pair = (Map.Entry<String, Session>) iterator.next();
            Session value = pair.getValue();                              // Session
            if (bannedLogins.contains(value.getLogin().toLowerCase())) {
                iterator.remove();
            }
        }
    }

    private void getDataUpdates() {
        openedFile.update();

        List<String> list = new ArrayList<>();
        Map<String, String> map = openedFile.getNewUpdateMap();
        list.addAll(map.keySet());
        Helper.print(map.size() + " updated sessions - " + list);


        Iterator iterator = openedFile.getNewUpdateMap().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> pair = (Map.Entry<String, String>) iterator.next();
            String key = pair.getKey();                                   // ID Session
            String value = pair.getValue();                               // Data может быть неполной (не сначала)

            Session session = new Session(key, value);

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

    private void getTasksUpdate() {
        uploadingTasks = new HashSet<>();
        completedTasks = new HashSet<>();
        uncompletedTasks = new HashSet<>();
        for (Session session : allSessionsMap.values()) {

            for (Task task : session.getTasks()) {
                if (task.getState().equals(UploadState.START_UPLOAD) &&
                        allSessionsMap.containsKey(task.getIDSession())) {
                    if (!allSessionsMap.get(task.getIDSession()).isOffline()) uploadingTasks.add(task);
                    else {
                        uncompletedTasks.add(task);
                    }
                }
                if (task.getState().equals(UploadState.END_UPLOAD)) {
                    completedTasks.add(task);
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