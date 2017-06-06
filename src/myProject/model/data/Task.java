package myProject.model.data;

import myProject.Helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task {

    private UnitFile unitFile;

    private UploadState state = UploadState.NOT_START;
    private Date timeStart;
    private Date timeEnd;
    private String speed = "";
    private String login = "";
    private String IDSession;

    public Task(String login, String IDSession) {
        this.login = login;
        this.IDSession = IDSession;
    }

    public void startUpload(String data) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyy HH:mm:ss", Locale.US);
            timeStart = sdf.parse(data.substring(9, 26));
        } catch (ParseException ex) {
            Helper.log(ex);
        }

        int startIndex = data.indexOf("\"") + 1;
        int endIndex = data.substring(startIndex).indexOf("\"") + startIndex;
        state = UploadState.START_UPLOAD;
        unitFile = new UnitFile(data.substring(startIndex, endIndex));
    }

    public void endUpload(String data) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyy HH:mm:ss", Locale.US);
            timeEnd = sdf.parse(data.substring(9, 26));
        } catch (ParseException ex) {
            Helper.log(ex);
        }

//        timeEnd = data.substring(17, 26);
        double intSpeed = Integer.parseInt(data
                .substring(data.lastIndexOf("(") + 1, data.lastIndexOf(" KB/sec") - 3)
                .replaceAll("\\D", "")) / 1024.0;
        speed = String.format("%.2f", intSpeed).concat(" MB/sec");
        state = UploadState.END_UPLOAD;
        unitFile.setSize(data.substring(data.lastIndexOf(" - ") + 3, data.lastIndexOf(" Bytes") + 6));
    }

    public void errorUpload() {
        state = UploadState.ERROR_UPLOAD;
    }

    public Date getTimeStart() {
        return timeStart;
    }

    public String getTimeStartToString() {
        return new SimpleDateFormat("HH:mm:ss").format(timeStart);
    }

    public Date getTimeEnd() {
        return timeEnd;
    }

    public String getTimeEndToString() {
        if (timeEnd == null) return "";
        return new SimpleDateFormat("HH:mm:ss").format(timeEnd);
    }


    public String getSpeed() {
        return speed;
    }

    public String getFileSize() {
        return unitFile.getSize();
    }

    public String getFilename() {
        return unitFile.getFile().getName();
    }

    public String getFullname() {
        return unitFile.getFile().getAbsolutePath();
    }

    public String getLogin() {
        return login;
    }

    public String getFolder() {
        return unitFile.getFile().getParentFile().getAbsolutePath();
    }

    public UploadState getState() {
        return state;
    }

    public String getSize() {
        return unitFile.getSize().replaceFirst("\\.d* ", "!!! ");
    }

    public UnitFile getUnitFile() {
        return unitFile;
    }

    public String getIDSession() {
        return IDSession;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;

        if (!IDSession.equals(task.IDSession)) return false;
        if (!unitFile.equals(task.unitFile)) return false;

        if (!login.equals(task.login)) return false;

        if (timeStart != null) {
            if (task.timeStart != null) {
                if (!timeStart.equals(task.timeStart)) {
                    return false;
                }
            }
        } else {
            if (task.timeStart != null) return false;
        }
        return true;
    }
}
