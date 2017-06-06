package myProject.model.infoFromFile;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import myProject.Helper;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FtpSource implements FileSource {

    private String ftpAddress = "";
    private String username = "";
    private String password = "";

    private String folder = "\\Serv-U.log\\";
    private String filename = new SimpleDateFormat("yyyy_MM_dd").format(new Date()) + ".log";
    private String fullPath = folder + filename;
    private File fileCopy = null;
    private File ftpFile = null;

    //    private boolean copyExists = false;
    private boolean error = false;
    private BooleanProperty copyExists = new SimpleBooleanProperty(false);

    private FTPClient ftpClient;

    public FtpSource() {
        try {
            fileCopy = Files.createTempFile("", "").toFile();
            ftpFile = Files.createTempFile("", "").toFile();
            System.out.println(ftpFile.getAbsolutePath());
            System.out.println(fileCopy.getAbsolutePath());
            Helper.writeLog("BEFORE");
            try {
                ftpClient = new FTPClient();
            }catch (Throwable thr) {
                Helper.writeLog(thr.toString());
            }
        } catch (IOException ex) {
            Helper.log(ex);
        }
    }

    @Override
    public File getFile() {
        return fileCopy;
    }

    public void setFtpAddress(String ftpAddress) {
        this.ftpAddress = ftpAddress;
    }

    public boolean isCopyExists() {
        return copyExists.get();
    }

    public BooleanProperty copyExistsProperty() {
        return copyExists;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

/*    public boolean isCopyExists() {
        return copyExists;
    }*/

    public boolean isError() {
        return error;
    }

    public boolean connectToFtp() {

        FTPClientConfig config = new FTPClientConfig();
        ftpClient.configure(config);

        try {
            int reply;
            ftpClient.connect(ftpAddress);

            System.out.println("Connected to ftp.vgtrk.com on " + 21);

            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new IOException();
            }
        } catch (IOException ex) {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException f) {
                    Helper.log(f);
                }
            }
            Helper.log(ex);
            System.err.println("Could not connectToFtp to server.");
            return false;
        }
        return true;
    }

    public boolean loginOk() {
        System.out.println("trying login and password");
        try {
            if (!ftpClient.login(username, password)) {
                ftpClient.logout();
                return false;
            }
        } catch (IOException ex) {
            error = true;
            System.out.println("IO Exception");
            Helper.log(ex);
        }
        return true;
    }

    public void start() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        ftpClient.enterLocalPassiveMode();
                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                        OutputStream output = new FileOutputStream(ftpFile);
                        boolean success = ftpClient.retrieveFile(fullPath, output);
                        output.close();

                        if (success) {
                            System.out.println("File " + filename + " has been downloaded successfully.");
                            System.out.println("Making copy!");
//                            copyExists = ;
                            copyExists.set(Helper.transferFile(ftpFile, fileCopy));
                            System.out.println("Copy done!");
                        }
                        Helper.pause(10);
                    }
                } catch (IOException ex) {
                    error = true;
                    System.out.println("IO Exception");
                    Helper.log(ex);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
