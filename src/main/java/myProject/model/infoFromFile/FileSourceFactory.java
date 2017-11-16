package myProject.model.infoFromFile;

public class FileSourceFactory {
    private FileSourceFactory(){
    }

    public static FtpSourceImpl createFtpSource(){
        return new FtpSourceImpl();
    }

    public static ShareSourceImpl createShareSource(){
        return new ShareSourceImpl();
    }
}
