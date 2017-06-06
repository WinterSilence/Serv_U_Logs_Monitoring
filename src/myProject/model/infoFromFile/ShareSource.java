package myProject.model.infoFromFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShareSource implements FileSource {

    private String folder = "\\\\ftpres\\f$\\Serv-U.log\\";
    // Default value
    private String fullPath = folder + new SimpleDateFormat("yyyy_MM_dd").format(new Date()) + ".log";

    @Override
    public File getFile() {
//        fullPath = new SimpleDateFormat("yyyy_MM_dd").format(new Date()) + ".log";
        return new File(fullPath);
    }

}
