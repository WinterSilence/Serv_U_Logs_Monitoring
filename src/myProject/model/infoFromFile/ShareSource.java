package myProject.model.infoFromFile;

import myProject.Helper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ShareSource implements FileSource {

    private String folder = "\\\\ftpres\\f$\\Serv-U.log\\";

    private SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy_MM_dd");
    private String currentDateFilename = filenameDateFormat.format(new Date()) + ".log";
    // Default value
    private String fullPathCurrentDate = folder + currentDateFilename;

    @Override
    public List<File> getFiles() {
        File[] files = new File(folder).listFiles();
        List<File> result = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.getName().matches("\\d{4}_\\d{2}_\\d{2}\\.log")) {
                    result.add(file);
                }
            }
            Collections.reverse(result);
        }
        return result;
    }

    public static void main(String[] args) {
        ShareSource shareSource = new ShareSource();

/*
        if (folder.exists()) {
            for (File file : folder.listFiles()) {
                Helper.print(file);
            }
        }
*/

        for (File file : shareSource.getFiles()) {
//            Helper.print(file.getName());
            if (file.getName().equals(shareSource.currentDateFilename)) {
                Helper.print("Test");
            }
        }

    }
}
