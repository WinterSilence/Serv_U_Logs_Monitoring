package myProject.view.viewUtils;

import myProject.Helper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ReportUtils {
    private static String reportFilename = System.getProperty("user.home") + "/Desktop/" + getDateInfo() + ".txt";

    private static String getDateInfo() {
        String yesterday800 = new SimpleDateFormat("dd.MM.yy").format(getYesterday800());
        String today800 = new SimpleDateFormat("dd.MM.yy").format(getToday800());
        return yesterday800 + " - " + today800;
    }

    private static Date getToday800() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static Date getYesterday800() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    private static int getCountOfFiles(String searchFolder) {
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
                        if (file.toFile().lastModified() >= getYesterday800().getTime()
                                && file.toFile().lastModified() < getToday800().getTime())
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

    private static void initFile() {
        File file = new File(reportFilename);
        try {
            new FileWriter(file);
        } catch (IOException ex) {
            Helper.log(ex);
        }
    }

    private static void writeInfoToFile(String string) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(reportFilename, true));) {
            bufferedWriter.write(string + "\r\n");
        } catch (IOException ex) {
            Helper.log(ex);
        }
    }

    public static void createReport() {
        initFile();
        writeInfoToFile(getDateInfo());

        String lastnames = getLastnames();
        writeInfoToFile(lastnames);

        System.out.println(getCountOfFiles("\\\\FTPRES\\upload\\"));
        writeInfoToFile("Общее количество файлов - " +
                Integer.toString(getCountOfFiles("\\\\FTPRES\\upload\\")));
        writeInfoToFile("Carbon - " +
                Integer.toString(getCountOfFiles("\\\\rikrz\\dalet-out")));
        writeInfoToFile("FFAStrans - " +
                Integer.toString(getCountOfFiles("\\\\rikrz\\e$\\coder_folder\\ff-dalet-in")));
        writeInfoToFile("Quantel - " +
                Integer.toString(getCountOfFiles("\\\\ftpres\\quantel$")));

        String PC1FolderYesterday = "\\\\172.18.0.184\\d$\\" +
                new SimpleDateFormat("dd-MM-yy").format(getYesterday800());
        String PC1FolderToday = "\\\\172.18.0.184\\d$\\" +
                new SimpleDateFormat("dd-MM-yy").format(getToday800());
        String PC2FolderYesterday = "\\\\172.18.0.183\\d$\\" +
                new SimpleDateFormat("dd-MM-yy").format(getYesterday800());
        String PC2FolderToday = "\\\\172.18.0.183\\d$\\" +
                new SimpleDateFormat("dd-MM-yy").format(getToday800());

        writeInfoToFile("Quantel SDI - " +
                Integer.toString(getCountOfFiles(PC1FolderYesterday) +
                        getCountOfFiles(PC1FolderToday) +
                        getCountOfFiles(PC2FolderYesterday) +
                        getCountOfFiles(PC2FolderToday)));

    }

    private final static String USER_AGENT = "\"Mozilla/5.0 (Windows NT\" +\n" +
            "          \" 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2\"";
    private static String loginFormUrl = "http://support.rfn.ru/index.php";
    private static String loginActionUrl = "http://support.rfn.ru/userreg.php";
    private static String IPperegonyUrl = "http://support.rfn.ru/team6.php";
    private static String username = "Nomad";
    private static String password = "fktrcfylhh";

    private static String getLastnames() {
        String result = "";
        try {
            Connection.Response indexForm = Jsoup
                    .connect(loginFormUrl)
                    .method(Connection.Method.GET)
                    .userAgent(USER_AGENT)
                    .execute();

            Connection.Response loginBeforeForm = Jsoup
                    .connect(loginActionUrl)
                    .method(Connection.Method.POST)
                    .userAgent(USER_AGENT)
                    .data("start", "")
                    .cookies(indexForm.cookies())
                    .execute();

            Connection.Response loginAfterForm = Jsoup
                    .connect(loginActionUrl)
                    .method(Connection.Method.POST)
                    .userAgent(USER_AGENT)
                    .data("name4", username)
                    .data("pass4", password)
                    .data("register", "")
                    .cookies(loginBeforeForm.cookies())
                    .execute();

            Connection.Response getAfterLogin = Jsoup
                    .connect(loginActionUrl)
                    .method(Connection.Method.GET)
                    .userAgent(USER_AGENT)
                    .data("myreg", "")
                    .cookies(loginAfterForm.cookies())
                    .execute();

            Connection.Response getAfterIndex = Jsoup
                    .connect(loginFormUrl)
                    .method(Connection.Method.GET)
                    .userAgent(USER_AGENT)
                    .cookies(getAfterLogin.cookies())
                    .execute();

            Connection.Response IPPeregonyForm = Jsoup
                    .connect(IPperegonyUrl)
                    .method(Connection.Method.GET)
                    .userAgent(USER_AGENT)
                    .cookies(getAfterIndex.cookies())
                    .execute();

            Document documentIP = IPPeregonyForm.parse();

            Calendar cal = Calendar.getInstance();
            cal.setTime(getToday800());
            int todayDay = cal.get(Calendar.DATE);
            int monthTableNumber;
            int dayNumberColumn;
            if (todayDay == 1) {
                monthTableNumber = 3;
                Elements elements = documentIP
                        .getElementsByClass("mainbox")
                        .first()
                        .getElementsByTag("table")
                        .get(monthTableNumber)
                        .getElementsByTag("tr")
                        .get(0)
                        .getElementsByTag("td");
                dayNumberColumn = elements.size() - 1;
            } else {
                monthTableNumber = 4;
                dayNumberColumn = todayDay - 1;
            }
            Element monthTable = documentIP
                    .getElementsByClass("mainbox")
                    .first()
                    .getElementsByTag("table")
                    .get(monthTableNumber);
            for (Element row : monthTable.getElementsByTag("tr")) {
                if (row.getElementsByTag("td").get(dayNumberColumn).text().equals("К")) {
                    result += row.getElementsByTag("td").get(0).text().split(" ")[0] + ", ";
                }
            }
            return result.substring(0, result.length() - 2);
        } catch (IOException ex) {
            Helper.log(ex);
        }
        return result;
    }

    private static int getCountOfAllFiles() {
        return getCountOfFiles("\\\\FTPRES\\upload\\");
    }

    private static int getCountOfCarbonFiles() {
        return getCountOfFiles("\\\\rikrz\\dalet-out");
    }

    private static int getCountOfFFAStansFiles() {
        return getCountOfFiles("\\\\rikrz\\e$\\coder_folder\\ff-dalet-in");
    }

    private static int getCountOfQuantelFiles() {
        return getCountOfFiles("\\\\ftpres\\quantel$");
    }

    private static int getCountOfAirManagerFiles() {
        return getCountOfFiles("\\\\vfs\\air-manager$");
    }

    private static int getCountOfQuantelSDIFiles() {
        String PC1FolderYesterday = "\\\\172.18.0.184\\d$\\" +
                new SimpleDateFormat("dd-MM-yy").format(getYesterday800());
        String PC1FolderToday = "\\\\172.18.0.184\\d$\\" +
                new SimpleDateFormat("dd-MM-yy").format(getToday800());
        String PC2FolderYesterday = "\\\\172.18.0.183\\d$\\" +
                new SimpleDateFormat("dd-MM-yy").format(getYesterday800());
        String PC2FolderToday = "\\\\172.18.0.183\\d$\\" +
                new SimpleDateFormat("dd-MM-yy").format(getToday800());
        return getCountOfFiles(PC1FolderYesterday) +
                getCountOfFiles(PC1FolderToday) +
                getCountOfFiles(PC2FolderYesterday) +
                getCountOfFiles(PC2FolderToday);
    }

    public static Message prepareEMailToSend() {
        String smtpServer = "exr.res.vgtrk";
        String to = "smena-ogs@vgtrk.com,apetrushenkov@vgtrk.com,aseyidov@vgtrk.com";
        String from = "smena-ogs@vgtrk.com";
        String subject = "Отчет за дежурство: " + getDateInfo();

        Properties properties = System.getProperties();

        properties.put("mail.smtp.host", smtpServer);
        properties.setProperty("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("smena-ogs", "200403261625");
            }
        });

        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));

            File otchetTemplate = new File("src/main/resources/template.xhtml");
            if (!otchetTemplate.exists()) {
                otchetTemplate = new File("template.xhtml");
            }
            BufferedReader buf = new BufferedReader(new FileReader(otchetTemplate));
            StringBuilder sb = new StringBuilder();
            while (buf.ready()) {
                sb.append(buf.readLine());
            }

            Document doc = Jsoup.parse(otchetTemplate, "utf-8");
            Element names = doc.getElementsByClass("T19").first();
            names.text(getLastnames());

            Element numbers = doc
                    .getElementsByClass("Таблица2_B3")
                    .first()
                    .getElementsByClass("P7")
                    .first();
            numbers.text(getCountOfAllFiles() + "");

            Element airManagerNumbers = doc
                    .getElementsByClass("Таблица6_B2")
                    .first()
                    .getElementsByClass("P7")
                    .first();
            airManagerNumbers.text(getCountOfAirManagerFiles() + "");

            int carbonNumbers = getCountOfCarbonFiles();

            int ffastransNumbers = getCountOfFFAStansFiles();

            Element carbon = doc
                    .getElementsByClass("Таблица6_B3")
                    .first()
                    .getElementsByClass("P7")
                    .first();
            carbon.text(carbonNumbers + ffastransNumbers + "");

            Element quantelNumbers = doc
                    .getElementsByClass("Таблица6_B4")
                    .first()
                    .getElementsByClass("P7")
                    .first();
            quantelNumbers.text(getCountOfQuantelFiles() + "");

            Element quantelSDINumbers = doc
                    .getElementsByClass("Таблица6_B5")
                    .first()
                    .getElementsByClass("P7")
                    .first();
            quantelSDINumbers.text(getCountOfQuantelSDIFiles() + "");

            message.setContent(doc.html(), "text/html; charset=windows-1251");
            message.setSubject(subject);

            message.setHeader(subject, "");

            message.setSentDate(new Date());

        } catch (IOException | MessagingException ex) {
            Helper.log(ex);
        }
        return message;
    }

    public static void sendEMail(Message message, String subject, String textField, String HTMLContent) throws MessagingException {
        message.setSubject(subject);
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(textField, false));
        Document doc = Jsoup.parse(HTMLContent);
        message.setContent(doc.html()
                , "text/html; charset=windows-1251");
        Transport.send(message);
    }
}