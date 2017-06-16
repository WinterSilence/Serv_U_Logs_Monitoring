package myProject.model.infoFromFile;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import myProject.Helper;
import org.apache.commons.net.ftp.*;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FtpSource implements FileSource {

    private String ftpAddress = "";
    private String username = "";
    private String password = "";

    private String folderTo = "ftp";
    private String folderFrom = File.separator + "Serv-U.log" + File.separator;
    private SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy_MM_dd");
    private String filename = filenameDateFormat.format(new Date()) + ".log";

    private BooleanProperty copyExists = new SimpleBooleanProperty(false);

    private FTPClient ftpClient;

    public FtpSource() {
        try {
            File folderToFile = new File(folderTo);
            if (!folderToFile.exists() || !folderToFile.isDirectory()) {
                Files.createDirectories(folderToFile.toPath());
            }
            try {
                ftpClient = new FTPClient();
            } catch (Throwable thr) {
                Helper.writeLog(thr.toString());
            }
        } catch (IOException ex) {
            Helper.log(ex);
        }
    }

    @Override
    public String getSourceFolder() {
        return "ftp";
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

    public boolean connectToFtp() {

        FTPClientConfig config = new FTPClientConfig();
        ftpClient.configure(config);

        try {
            int reply;
            ftpClient.connect(ftpAddress);

            Helper.print("Connected to ftp.vgtrk.com on " + 21);

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
        Helper.print("Trying login and password");
        try {
            if (!ftpClient.login(username, password)) {
                ftpClient.logout();
                return false;
            }
        } catch (IOException ex) {
            Helper.print("IO Exception");
            Helper.log(ex);
        }
        return true;
    }

    public void start() {
        try {
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (
                IOException ex)

        {
            Helper.print("IO Exception");
            Helper.log(ex);
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {

                        // TODO Может пригодиться
                        for (FTPFile ftpfile : ftpClient.listFiles(folderFrom, new FTPFileFilter() {
                            @Override
                            public boolean accept(FTPFile file) {
                                return file.getName().matches("\\d{4}_\\d{2}_\\d{2}\\.log");
                            }
                        })) {
                        }

/*                        File fileFromFTP = new File(folderTo + File.separator + "copy" + filename);
                        if (!fileFromFTP.exists()) {
                            fileFromFTP = Files.createFile(fileFromFTP.toPath()).toFile();
                        }
                        fileFromFTP.deleteOnExit();*/

                        File fileFromFTP = createLocalFile("copy" + filename);

/*                        OutputStream outputStream = new FileOutputStream(fileFromFTP);    // file for update
                        boolean success = ftpClient.retrieveFile(folderFrom + File.separator + filename, outputStream);
                        outputStream.close();*/

                        boolean success = downloadFromFTP(fileFromFTP, folderFrom + File.separator + filename);

                        //TODO
                        if (success) {
//                            makingCopy();


                            Helper.print("File " + filename + " has been downloaded successfully.");
                            Helper.print("Making copy!");
                            File fileCopy = new File(folderTo + File.separator + filename);
                            fileCopy.deleteOnExit();
                            copyExists.set(Helper.transferFile(fileFromFTP, fileCopy));
                            Helper.print("Copy done!");

                        }
                        Helper.pause(10);
                    }
                } catch (IOException ex) {
                    Helper.print("IO Exception");
                    Helper.log(ex);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private File createLocalFile(String filename) throws IOException {
        File file = new File(folderTo + File.separator + filename);
        if (!file.exists()) {
            file = Files.createFile(file.toPath()).toFile();
        }
        file.deleteOnExit();
        return file;
    }

    private boolean downloadFromFTP(File localFileTo, String pathOnFTPToFile) throws IOException {
        OutputStream outputStream = new FileOutputStream(localFileTo);    // file for update
        boolean success = ftpClient.retrieveFile(pathOnFTPToFile, outputStream);
        outputStream.close();
        return success;
    }


    //TODO
    private void makingCopy(String filenameFromFTP) throws IOException {
        Helper.print("File " + filenameFromFTP + " has been downloaded successfully.");
        Helper.print("Making copy!");
//        File fileCopy = new File(folderTo + File.separator + filenameFromFTP);
        File fileCopy = createLocalFile(filename);
        fileCopy.deleteOnExit();
//        copyExists.set(Helper.transferFile(fileFromFTP, fileCopy));
        Helper.print("Copy done!");
    }
}
