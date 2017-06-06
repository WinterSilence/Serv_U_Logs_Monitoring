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
    public boolean equals(Object obj) {

        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        UnitFile unitFile = (UnitFile) obj;

        return file.equals(unitFile.file) && size.equals(unitFile.size);
    }

    @Override
    public String toString() {
        return file.getAbsolutePath() + " - " + getSize();
    }
}
