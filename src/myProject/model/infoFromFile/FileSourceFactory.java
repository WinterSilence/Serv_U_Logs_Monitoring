package myProject.model.infoFromFile;

public class FileSourceFactory {
    private FileSourceFactory(){
    }


    public static FtpSource createFtpSource(){
        return new FtpSource();
    }

    public static ShareSource createShareSource(){
        return new ShareSource();
    }
}
