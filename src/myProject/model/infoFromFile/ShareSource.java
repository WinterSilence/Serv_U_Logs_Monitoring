package myProject.model.infoFromFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShareSource implements FileSource {

    private String folder = "\\\\ftpres\\f$\\Serv-U.log\\";

    private SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy_MM_dd");
    private String currentDateFilename = filenameDateFormat.format(new Date()) + ".log";
    // Default value
    private String fullPathCurrentDate = folder + currentDateFilename;

    @Override
    public File getFile() {
        return new File(fullPathCurrentDate);
    }

    public static void main(String[] args) {
        ShareSource shareSource = new ShareSource();
        File folder = new File(shareSource.folder);

/*
        if (folder.exists()) {
            for (File file : folder.listFiles()) {
                System.out.println(file);
            }
        }
*/

        File[] filesInFolder = folder.listFiles();


    }
}
