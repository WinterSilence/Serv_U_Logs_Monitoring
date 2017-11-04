package myProject.model.data;

import java.io.File;

public class UnitFile {
    private File file;
    private String size = "";

    public UnitFile(String fullpath) {
        this.file = new File(fullpath);
    }

    public File getFile() {
        return file;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {
        if (size != null && size.length() > 6) {
            String result = size.substring(0, size.length() - 6).replaceAll("\\D+", "");
            double temp = Long.parseLong(result) / 1048576.0;
            return String.valueOf((int) Math.ceil(temp)).concat(" Mb");
        }
        return "";
    }

    @Override
    public String toString() {
        return file.getAbsolutePath() + " - " + getSize();
    }
}
