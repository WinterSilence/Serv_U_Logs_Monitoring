package myProject.view.viewUtils;

import myProject.Helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportUtils {
    private static String reportFilename = System.getProperty("user.home") + "/Desktop/" + getDateInfo() + ".txt";

    public static String getDateInfo() {
        String yesterday800 = new SimpleDateFormat("dd.MM.yy").format(getYesterday800());
        String today800 = new SimpleDateFormat("dd.MM.yy").format(getToday800());
        return yesterday800 + " - " + today800;
    }

    public static Date getToday800() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getYesterday800() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    public static int getCountOfFiles(String searchFolder) {
        int countOfFiles = 0;
        File file = new File(searchFolder);
        if (!file.exists() || !file.isDirectory()) {
            return 0;
        }

        Path path = file.toPath();
        List<Path> files = new ArrayList<>();
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!attrs.isDirectory()) {
                        if (file.toFile().lastModified() >= ReportUtils.getYesterday800().getTime()
                                && file.toFile().lastModified() < ReportUtils.getToday800().getTime())
                            files.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            Helper.log(ex);
        }
        List<String> ignoredPostfixes = new ArrayList<>();
        ignoredPostfixes.add(".db");
        ignoredPostfixes.add(".doc");
        ignoredPostfixes.add(".docx");
        ignoredPostfixes.add(".txt");
        ignoredPostfixes.add(".pdf");
        ignoredPostfixes.add(".jpg");
        ignoredPostfixes.add(".bmp");
        ignoredPostfixes.add(".gif");
        ignoredPostfixes.add(".log");
        ignoredPostfixes.add(".xmp");
        for (Path pathLoc : files) {
            String filename = pathLoc.getFileName().toString();
            String postfix;
            if (filename.lastIndexOf(".") < 0) {
                postfix = "";
            } else {
                postfix = filename.substring(filename.lastIndexOf("."));
            }
            if (!ignoredPostfixes.contains(postfix)) {
                countOfFiles++;
            }
        }
        return countOfFiles;
    }

    public static void initFile() {
        File file = new File(reportFilename);
        try {
            new FileWriter(file);
        } catch (IOException ex) {
            Helper.log(ex);
        }
    }

    public static void writeInfoToFile(String string) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(reportFilename, true));) {
            bufferedWriter.write(string + "\r\n");
        } catch (IOException ex) {
            Helper.log(ex);
        }
    }
}
