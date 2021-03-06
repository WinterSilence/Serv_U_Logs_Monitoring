package myProject.model.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import myProject.Helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task {

    private UploadState state = UploadState.NOT_START;
    private Date timeStart;
    private Date timeEnd;
    private String speed = "";
    private String login = "";
    private String IDSession;

    // Implementation for JavaFX
    private final ObjectProperty<UnitFile> unitFile = new SimpleObjectProperty<>();
    private final BooleanProperty checkbox = new SimpleBooleanProperty();

    public Task(String login, String IDSession) {
        this.login = login;
        this.IDSession = IDSession;
    }

    public BooleanProperty checkboxProperty() {
        return checkbox;
    }

    public void setCheckbox(boolean checkbox) {
        this.checkbox.set(checkbox);
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
        unitFile.set(new UnitFile(data.substring(startIndex, endIndex)));
    }

    public void endUpload(String data) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyy HH:mm:ss", Locale.US);
            timeEnd = sdf.parse(data.substring(9, 26));
        } catch (ParseException ex) {
            Helper.log(ex);
        }

        double intSpeed = Integer.parseInt(data
                .substring(data.lastIndexOf("(") + 1, data.lastIndexOf(" KB/sec") - 3)
                .replaceAll("\\D", "")) / 1024.0;
        speed = String.format("%.2f", intSpeed).concat(" MB/sec");
        state = UploadState.END_UPLOAD;
        unitFile.get().setSize(data.substring(data.lastIndexOf(" - ") + 3, data.lastIndexOf(" Bytes") + 6));
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

    public final ObjectProperty<UnitFile> unitFileObjectProperty() {
        return this.unitFile;
    }

    public String getSpeed() {
        return speed;
    }

    public String getFileSize() {
        return unitFile.get().getSize();
    }

    public String getFilename() {
        return unitFile.get().getFile().getName();
    }

    public String getFullname() {
        return unitFile.get().getFile().getAbsolutePath();
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public String getFolder() {
        return unitFile.get().getFile().getParentFile().getAbsolutePath();
    }

    public UploadState getState() {
        return state;
    }

    public void setState(UploadState state) {
        this.state = state;
    }

    public String getSize() {
        return unitFile.get().getSize().replaceFirst("\\.d* ", "!!! ");
    }

    public UnitFile getUnitFile() {
        return unitFile.get();
    }

    public String getIDSession() {
        return IDSession;
    }

    public void setUnitFile(UnitFile unitFile) {
        this.unitFile.set(unitFile);
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public void setTimeStart(Date timeStart) {
        this.timeStart = timeStart;
    }

    public void setTimeEnd(Date timeEnd) {
        this.timeEnd = timeEnd;
    }
}
