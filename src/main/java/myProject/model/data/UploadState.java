package myProject.model.data;

public enum UploadState {
    NOT_START("Not_Started"),
    START_UPLOAD("Start_Upload"),
    END_UPLOAD("End_Upload"),
    ERROR_UPLOAD("Error_Upload");

    final String name;

    UploadState(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return name;
    }
}
