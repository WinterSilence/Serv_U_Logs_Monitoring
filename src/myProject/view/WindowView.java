package myProject.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.Pair;
import myProject.Helper;
import myProject.controller.FXMLController;
import myProject.model.MyModel;
import myProject.model.data.Session;
import myProject.model.data.Task;
import myProject.model.data.UploadState;
import myProject.model.infoFromFile.FileSourceFactory;
import myProject.model.infoFromFile.FtpSource;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class WindowView implements View {
    //75008042017

    private final int FILE_MENU = 0;

    private final int OPEN_BUTTON = 0;
    private final int EXIT_BUTTON = 7;

    private FXMLController fxmlController;
    private MyModel myModel;

    private Parent root;

    private TableView<Session> tableViewOnline; // Online Sessions
    private TableView<Task> tableViewRecently;  // Recently uploaded files
    private TableView<Task> tableViewUploading; // Uploading files

    private SplitPane splitPane;
    private VBox vBox;
    private HBox hBox;
    private AnchorPane anchorPaneRecently;

    private Button startButton;
    private Button stopButton;
    private Button FTPButton;
    private Button todayButton;
    private Button yesterdayButton;


    private Label startButtonText;
    private Label todayButtonText;
    private Label yesterdayButtonText;

    private Label rightStatusLabel;


    private ChoiceBox<String> recentlyTaskChoiceBox;

    private String title = "WorkProjectApp 20170628";
    private String selectedLogin = " All";

    private TableColumn<Task, ?> sortColumn = null;
    private StringProperty currentDate;
    private StringProperty yesterdayDate;

    // Отображение Онлайн сессий и пришедших файлов за checkHours часов
    // default value = 48
    private int checkHours = 48;
    private Stage stage;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM");

    public WindowView(MyModel myModel) {
        this.myModel = myModel;
    }

    public void setFxmlController(FXMLController fxmlController) {
        this.fxmlController = fxmlController;
    }

    public void startView(Stage stage) throws IOException {
        this.stage = stage;
        root = FXMLLoader.load(getClass().getResource("WorkProjectApplication.fxml"));
        Scene scene = new Scene(root, 1280, 720);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();

        init();
        setOpenMenuButton(stage);
        setExitMenuButton();
        setStartButton();
        setStopButton();
        setCheckHoursButton();
        setFTPButton();
        setHideShowSessionTable();
        setLeftStatusLabel();
        setTodayButton();
        setYesterdayButton();
        setTodayButtonText();
        setYesterdayButtonText();
        setOfflineProperty();
    }

    private void init() {
        vBox = (VBox) root.lookup("#VBox");
        splitPane = (SplitPane) vBox.lookup("#splitPane");
        hBox = (HBox) vBox.lookup("#HBox");

        anchorPaneRecently = (AnchorPane) splitPane.lookup("#anchorPaneRecently");

        tableViewOnline = (TableView<Session>) splitPane.lookup("#tableViewOnline");
        tableViewRecently = (TableView<Task>) splitPane.lookup("#recentlyUploadedFiles");
        tableViewUploading = (TableView<Task>) splitPane.lookup("#uploadingFiles");

        startButton = (Button) anchorPaneRecently.lookup("#startButton");
        stopButton = (Button) anchorPaneRecently.lookup("#stopButton");
        FTPButton = (Button) anchorPaneRecently.lookup("#FTPbutton");
        todayButton = (Button) anchorPaneRecently.lookup("#todayButton");
        yesterdayButton = (Button) anchorPaneRecently.lookup("#yesterdayButton");

        currentDate = new SimpleStringProperty(simpleDateFormat.format(new Date()));
        yesterdayDate = new SimpleStringProperty(simpleDateFormat.format(Helper.yesterday()));

        currentDate.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                todayButtonText.setText(currentDate.getValue());
            }
        });

        yesterdayDate.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                yesterdayButtonText.setText(yesterdayDate.getValue());
            }
        });

        startButtonText = (Label) anchorPaneRecently.lookup("#startButtonText");
        rightStatusLabel = (Label) hBox.lookup("#rightStatusLabel");

        recentlyTaskChoiceBox = (ChoiceBox<String>) splitPane.lookup("#recentlyTaskChoiceBox");

        showOnlineSessions();
        showRecentlyUploadedFiles();
        showUploadingFiles();
    }

    private void setOfflineProperty() {
        fxmlController.offlineProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue) {
                    setStartCircle(Color.LIGHTGREEN);
                    stopButton.setDisable(false);
                } else {
                    setStartCircle(Color.RED);
                    startButtonText.setVisible(true);
                    startButton.setDisable(false);
                    FTPButton.setDisable(false);
                    todayButton.setDisable(false);
                    yesterdayButton.setDisable(false);
                }
            }
        });
    }

    private void setTodayButtonText() {
        todayButtonText = (Label) anchorPaneRecently.lookup("#todayButtonText");
        todayButtonText.setText(currentDate.getValue());
    }

    private void setYesterdayButtonText() {
        yesterdayButtonText = (Label) anchorPaneRecently.lookup("#yesterdayButtonText");
        yesterdayButtonText.setText(yesterdayDate.getValue());
    }

    private void setHideShowSessionTable() {
        AnchorPane anchorPaneUploadingFiles = (AnchorPane) splitPane.lookup("#anchorPaneUploadingFiles");
        Button hideShowSessionTableButton = (Button) anchorPaneUploadingFiles.lookup("#hideShowSessionTableButton");
        AnchorPane anchorPaneTableViewOnline = (AnchorPane) splitPane.lookup("#anchorPaneTableViewOnline");

        hideShowSessionTableButton.setOnAction(new EventHandler<ActionEvent>() {
            private double divider = 0;

            @Override
            public void handle(ActionEvent event) {
                if (anchorPaneTableViewOnline.getWidth() > 0) {
                    anchorPaneTableViewOnline.setMinWidth(0);
                    anchorPaneTableViewOnline.setMaxWidth(0);
                    divider = splitPane.getDividerPositions()[0];
                    splitPane.setDividerPosition(0, 0);
                    hideShowSessionTableButton.setText("►");
                } else {
                    anchorPaneTableViewOnline.setMinWidth(Region.USE_COMPUTED_SIZE);
                    anchorPaneTableViewOnline.setMaxWidth(Region.USE_COMPUTED_SIZE);
                    splitPane.setDividerPosition(0, divider);
                    hideShowSessionTableButton.setText("◄");
                }
            }
        });
        hideShowSessionTableButton.fire();
    }

    private void setCheckHoursButton() {
        Button checkHoursButton = (Button) anchorPaneRecently.lookup("#checkHoursButton");

        checkHoursButton.setOnAction(event -> {
            while (true) {
                TextInputDialog textInputDialog = new TextInputDialog(String.valueOf(checkHours));
                textInputDialog.setTitle("");
                textInputDialog.setHeaderText("Период отображения");
                textInputDialog.setGraphic(null);
                Optional<String> result = textInputDialog.showAndWait();
                if (result.isPresent()) {
                    try {
                        int hours = Integer.parseInt(result.get());
                        if (hours < 1) {
                            throw new WrongInputException();
                        }
                        checkHours = hours;
                        Label numberOfHours = (Label) anchorPaneRecently.lookup("#numberOfHours");
                        numberOfHours.setText(String.valueOf(checkHours));
                        update();
                        break;
                    } catch (NumberFormatException ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Некорректный ввод, введите число!!!");
                        alert.showAndWait();
                    } catch (WrongInputException exept) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Неверное количество часов, введите от 1 часа");
                        alert.showAndWait();
                    }
                } else {
                    break;
                }
            }
        });
    }

    private void setExitMenuButton() {
        MenuBar menuBar = (MenuBar) root.lookup("#menuBar");              // Menu Bar
        Menu menuFile = menuBar.getMenus().get(FILE_MENU);                        // File Menu
        MenuItem exitButton = menuFile.getItems().get(EXIT_BUTTON);               // Exit Button
        exitButton.setOnAction(event -> Platform.exit());
    }

    private void setOpenMenuButton(Stage stage) {
        MenuBar menuBar = (MenuBar) root.lookup("#menuBar");              // Menu Bar
        Menu menuFile = menuBar.getMenus().get(FILE_MENU);                        // File Menu
        MenuItem openButton = menuFile.getItems().get(OPEN_BUTTON);               // Open Button
        openButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            File file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                fileChooser.setInitialDirectory(file.getParentFile());
                stage.setTitle(title + " - " + file.getName());
                fxmlController.setTodayFullPath(file.getAbsolutePath());
                //TODO !!!
//                myModel.reset();
            }
        });
    }

    private void setStartButton() {
        startButton.setOnAction(event -> {
            fxmlController.establishConnection(FileSourceFactory.createShareSource());
            startButtonText.setVisible(false);
            startButton.setDisable(true);
            FTPButton.setDisable(true);
            todayButton.setDisable(true);
            yesterdayButton.setDisable(true);
        });
    }

    private void setStopButton() {
        stopButton.setOnAction(event -> {
            fxmlController.closeConnection();
            stopButton.setDisable(true);
        });
    }

    private void setTodayButton() {
        todayButton.setOnAction(event -> {
            fxmlController.establishConnectionTodayOnly(FileSourceFactory.createShareSource());
            startButton.setDisable(true);
            FTPButton.setDisable(true);
            todayButton.setDisable(true);
            yesterdayButton.setDisable(true);
        });
    }

    private void setYesterdayButton() {
        yesterdayButton.setOnAction(event -> {
            fxmlController.establishConnection(FileSourceFactory.createShareSource(), Helper.yesterday());
            startButton.setDisable(true);
            FTPButton.setDisable(true);
            todayButton.setDisable(true);
            yesterdayButton.setDisable(true);
        });
    }

    private void setFTPButton() {
        FTPButton.setOnAction((ActionEvent event) -> {
            startButtonText.setVisible(false);
            startButton.setDisable(true);
            FTPButton.setDisable(true);
            todayButton.setDisable(true);
            yesterdayButton.setDisable(true);
            TextInputDialog dialog = new TextInputDialog("ftp.vgtrk.com");
            dialog.setTitle("");
            dialog.setHeaderText("Enter FTP server: ");
            dialog.setGraphic(null);
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String ftpAddress = result.get().toLowerCase();
                if (ftpAddress.startsWith("ftp:\\") || ftpAddress.startsWith("ftp:/")) {
                    ftpAddress = ftpAddress.replaceFirst("^(ftp)\\W+", "");
                }
                ftpAddress = ftpAddress.replaceAll("(\\\\|/)*$", "");
                FtpSource ftpSource = FileSourceFactory.createFtpSource();
                ftpSource.setFtpAddress(ftpAddress);

                while (passwordDialog(ftpSource)) {
                    if (ftpSource.connectToFtp()) {
                        if (ftpSource.loginOk()) {
                            ftpSource.start();
                            ftpSource.copyExistsProperty().addListener((observable, oldValue, newValue) -> {
                                Helper.print("CopyExist");
                                Helper.print("Start connection");
                                fxmlController.establishConnection(ftpSource);
                            });

/*
                            if (ftpSource.isCopyExists()) {
                                Helper.print("Copy Exist!");
                                if (fxmlController.isOffline()) {
                                    Helper.print("Start connection!");
                                    fxmlController.establishConnection(ftpSource);
                                    FTPButton.setDisable(true);
                                } else {
                                    Helper.print("End connection!");
                                    fxmlController.closeConnection();
                                    FTPButton.setDisable(false);
                                }
                            } else {
                                Helper.print("Copy Not Exist!");
                            }
*/
                            break;
                        } else {
                            Helper.print("LOGIN FAIL");
                        }
                    }
                }
/*
                    Platform.runLater(new Runnable() {
//                                Alert alert = new Alert(Alert.AlertType.ERROR);
//                                alert.setContentText("File not found or some IO error occurred!!!");
//                                alert.showAndWait();
//                  }
*/
                fxmlController.offlineProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if (newValue) {
                            ftpSource.stop();
                        }
                    }
                });
            }
        });
    }

    private boolean passwordDialog(FtpSource ftpSource) {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Введите логин и пароль: ");

// Set the button types.

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

// Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        username.setText("Logtest");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

// Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(false);

// Do some validation (using the Java 8 lambda syntax).
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(() -> password.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            ftpSource.setUsername(usernamePassword.getKey());
            ftpSource.setPassword(usernamePassword.getValue());
        });
//        if (!result.isPresent()) throw new SocketException();
        return result.isPresent();
    }

    private void setStartCircle(Color color) {
        Circle startCircle = (Circle) anchorPaneRecently.lookup("#startCircle");
        startCircle.setFill(color);
    }

    public void update() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                setTableViewOnline();

                setTableViewRecently();

                setTableViewUploading();

                currentDate.set(simpleDateFormat.format(new Date()));

                yesterdayDate.set(simpleDateFormat.format(Helper.yesterday()));
            }
        });
    }

    private TreeSet<String> recentFilesLogins = new TreeSet<>();

    private void setTableViewRecently() {
        List<Task> resultTableRecentlyFiles = filterTaskListHours(myModel.getCompletedTasks());

        resultTableRecentlyFiles.addAll(filterTaskListHours(myModel.getUncompletedTasks()));

        TreeSet<String> recentFilesLoginUpdate = new TreeSet<>();
        for (Task task : resultTableRecentlyFiles) {
            recentFilesLoginUpdate.add(task.getLogin());
        }
        recentFilesLoginUpdate.add(" All");

        if (recentFilesLogins.size() == 0) {
            recentFilesLogins.addAll(recentFilesLoginUpdate);
            recentlyTaskChoiceBox.setItems(FXCollections.observableArrayList(recentFilesLogins));
        }

        if (!recentFilesLogins.equals(recentFilesLoginUpdate)) {
            recentFilesLogins = recentFilesLoginUpdate;
            recentlyTaskChoiceBox.setItems(FXCollections.observableArrayList(recentFilesLogins));
        }
        resultTableRecentlyFiles = filterTaskListSelectedChoiceBox(resultTableRecentlyFiles);
        resultTableRecentlyFiles = filterTaskListSameTasks(resultTableRecentlyFiles);

        resultTableRecentlyFiles.sort(new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                Date dateTask1;
                Date dateTask2;
                if (task1.getTimeEnd() != null) dateTask1 = task1.getTimeEnd();
                else dateTask1 = task1.getTimeStart();
                if (task2.getTimeEnd() != null) dateTask2 = task2.getTimeEnd();
                else dateTask2 = task2.getTimeStart();
                return dateTask2.compareTo(dateTask1);
            }
        });

        tableViewRecently.setItems(FXCollections.observableArrayList(resultTableRecentlyFiles));
        // Сортировка с жёсткой привязкой к отображению (нельзя изменить в окне)
/*
        if (sortColumn != null) {
            tableViewRecently.getSortOrder().add(sortColumn);
        }
        tableViewRecently.sortPolicyProperty().set(new Callback<TableView<Task>, Boolean>() {
            @Override
            public Boolean call(TableView<Task> param) {
                Comparator<Task> comparator = new Comparator<Task>() {
                    @Override
                    public int compare(Task task1, Task task2) {
                        Date dateTask1;
                        Date dateTask2;
                        if (task1.getTimeEnd() != null) dateTask1 = task1.getTimeEnd();
                        else dateTask1 = task1.getTimeStart();
                        if (task2.getTimeEnd() != null) dateTask2 = task2.getTimeEnd();
                        else dateTask2 = task2.getTimeStart();
                        return dateTask2.compareTo(dateTask1);
                    }
                };
                FXCollections.sort(tableViewRecently.getItems(), comparator);
                return true;
            }
        });
*/

        tableViewRecently.setRowFactory(new Callback<TableView<Task>, TableRow<Task>>() {
            @Override
            public TableRow<Task> call(TableView<Task> tableView) {
                return new TableRow<Task>() {
                    @Override
                    public void updateItem(Task task, boolean empty) {
                        if (!empty) {
                            if (task.getTimeEndToString().equals("") || task.getTimeEnd().before(Helper.yesterday())) {
                                for (Node node : getChildren()) {
                                    ((TableCell) node).setTextFill(Color.GREY);
                                }
                            } else {
                                for (Node node : getChildren()) {
                                    ((TableCell) node).setTextFill(Color.BLACK);
                                }
                            }

                        }
                        super.updateItem(task, empty);
                    }
                };
            }
        });
        setRightStatusLabelText("Size - " + resultTableRecentlyFiles.size());
    }

    private List<Task> filterTaskListSelectedChoiceBox(List<Task> list) {
        if (selectedLogin.equals(" All")) return list;
        List<Task> result = new ArrayList<>();
        for (Task task : list) {
            if (task.getLogin().equals(selectedLogin)) {
                result.add(task);
            }
        }
        return result;
    }

    private List<Task> filterTaskListHours(List<Task> list) {
        Date currentDate = new Date();
        List<Task> result = new ArrayList<>();
        for (Task task : list) {
            long currTime;
            if (task.getTimeEnd() != null) {
                currTime = currentDate.getTime() - task.getTimeEnd().getTime();
            } else {
                currTime = currentDate.getTime() - task.getTimeStart().getTime();
            }
            if (currTime < checkHours * 3_600_000L) {
                result.add(task);
            }
        }
        return result;
    }

    private List<Task> filterTaskListSameTasks(List<Task> list) {
        List<Task> result = new ArrayList<>();
        result.addAll(list);
        Iterator<Task> iterator = result.iterator();
        while (iterator.hasNext()) {
            Task currentTask = iterator.next();
            for (Task task : list) {
                if (!currentTask.equals(task)
                        && currentTask.getFilename().equals(task.getFilename())
                        && currentTask.getLogin().equals(task.getLogin())
                        && currentTask.getFolder().equals(task.getFolder())) {
                    Date dateTask1;
                    Date dateTask2;
                    if (currentTask.getTimeEnd() != null) dateTask1 = currentTask.getTimeEnd();
                    else dateTask1 = currentTask.getTimeStart();
                    if (task.getTimeEnd() != null) dateTask2 = task.getTimeEnd();
                    else dateTask2 = task.getTimeStart();
                    if (dateTask2.compareTo(dateTask1) > 0) {
                        iterator.remove();
                    }
                    break;
                }
            }
        }
        return result;
    }

    private void setTableViewOnline() {
        List<Session> resultListOnlineSessions = new ArrayList<>();
        for (Session session : myModel.getOnlineSessionsMap().values()) {
            if (!session.isEmpty()) {
                for (Task task : session.getTasks()) {
                    if (task.getState() == UploadState.START_UPLOAD) {
                        resultListOnlineSessions.add(session);
                        break;
                    }
                }
            }
        }
        Collections.sort(resultListOnlineSessions, new Comparator<Session>() {
            @Override
            public int compare(Session session1, Session session2) {
                return session1.getConnectionTime().compareTo(session2.getConnectionTime());
            }
        });
        tableViewOnline.setItems(FXCollections.observableArrayList(resultListOnlineSessions));

        tableViewOnline.sortPolicyProperty().set(new Callback<TableView<Session>, Boolean>() {
            @Override
            public Boolean call(TableView<Session> param) {
                Comparator<Session> comparator = new Comparator<Session>() {
                    @Override
                    public int compare(Session o1, Session o2) {
                        return -o1.getConnectionTime().compareTo(o2.getConnectionTime());
                    }
                };
                FXCollections.sort(tableViewOnline.getItems(), comparator);
                return true;
            }
        });

        tableViewOnline.setRowFactory(new Callback<TableView<Session>, TableRow<Session>>() {
            @Override
            public TableRow<Session> call(TableView<Session> tableView) {
                return new TableRow<Session>() {
                    @Override
                    public void updateItem(Session session, boolean empty) {

                        if (!empty) {
                            if (session.getConnectionTime().before(Helper.yesterday())) {
                                for (Node node : getChildren()) {
                                    ((TableCell) node).setTextFill(Color.GREY);
                                }
                            } else {
                                for (Node node : getChildren()) {
                                    ((TableCell) node).setTextFill(Color.BLACK);
                                }
                            }
                        }
                        super.updateItem(session, empty);
                    }
                };

            }
        });
    }

    private void setTableViewUploading() {
        tableViewUploading.setItems(FXCollections.observableArrayList(myModel.getUploadingTasks()));
        tableViewUploading.sortPolicyProperty().set(new Callback<TableView<Task>, Boolean>() {
            @Override
            public Boolean call(TableView<Task> param) {
                Comparator<Task> comparator = new Comparator<Task>() {
                    @Override
                    public int compare(Task task1, Task task2) {
                        return -task1.getTimeStart().compareTo(task2.getTimeStart());
                    }
                };
                FXCollections.sort(tableViewUploading.getItems(), comparator);
                return true;
            }
        });
    }

    private void showOnlineSessions() {
        TableColumn<Session, String> onlineConnectionTimeColumn =
                (TableColumn<Session, String>) tableViewOnline.getColumns().get(0);
        TableColumn<Session, String> onlineLoginColumn =
                (TableColumn<Session, String>) tableViewOnline.getColumns().get(1);
        TableColumn<Session, String> onlineIPAddressColumn =
                (TableColumn<Session, String>) tableViewOnline.getColumns().get(2);
        TableColumn<Session, String> onlineClientColumn =
                (TableColumn<Session, String>) tableViewOnline.getColumns().get(3);

        onlineConnectionTimeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        onlineConnectionTimeColumn.setStyle("-fx-alignment: CENTER;");

        onlineLoginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        onlineLoginColumn.setStyle("-fx-alignment: CENTER;");

        onlineIPAddressColumn.setCellValueFactory(new PropertyValueFactory<>("IPAddress"));
        onlineIPAddressColumn.setStyle("-fx-alignment: CENTER;");

        onlineClientColumn.setCellValueFactory(new PropertyValueFactory<>("client"));
        onlineClientColumn.setStyle("-fx-alignment: CENTER;");
    }


    private void showRecentlyUploadedFiles() {
        TableColumn<Task, String> recentlyEndColumn =
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(0);
        TableColumn<Task, String> recentlyFilenameColumn =
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(1);
        TableColumn<Task, String> recentlySizeColumn =
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(2);
        TableColumn<Task, String> recentlyStartColumn =
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(3);
        TableColumn<Task, String> recentlySpeedColumn =
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(4);
        TableColumn<Task, String> recentlyLoginColumn =
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(5);
        TableColumn<Task, String> recentlyFolderColumn =
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(6);

        sortColumn = recentlyEndColumn;

        tableViewRecently.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (event.isSecondaryButtonDown()) {
                    Helper.print("Right Clicked");

                    EventTarget eventTarget = event.getTarget();
                    TableCell tableCell = null;
                    if (eventTarget instanceof TableCell) {
                        tableCell = (TableCell) eventTarget;
                    } else if (eventTarget instanceof Text) {
                        tableCell = (TableCell) ((Text) eventTarget).getParent();
                    }
                    if (tableCell != null) {
                        contextMenuRecentlyUploadedFiles(tableCell);
                    }
                }
            }
        });

        tableViewRecently.setOnSort(new EventHandler<SortEvent<TableView<Task>>>() {
            @Override
            public void handle(SortEvent<TableView<Task>> event) {
                if (tableViewRecently.getSortOrder().size() > 0) {
                    sortColumn = tableViewRecently.getSortOrder().get(0);
                }
            }
        });

        recentlyTaskChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    selectedLogin = newValue;
                }
                recentlyTaskChoiceBox.getSelectionModel().select(selectedLogin);
                update();
            }
        });

        recentlyEndColumn.setCellValueFactory(new PropertyValueFactory<>("timeEndToString"));
        recentlyEndColumn.setStyle("-fx-alignment: CENTER;");

        recentlyFilenameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        recentlyFilenameColumn.setStyle("-fx-alignment: CENTER;");

        recentlySizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        recentlySizeColumn.setStyle("-fx-alignment: CENTER;");

        recentlyStartColumn.setCellValueFactory(new PropertyValueFactory<>("timeStartToString"));
        recentlyStartColumn.setStyle("-fx-alignment: CENTER;");

        recentlySpeedColumn.setCellValueFactory(new PropertyValueFactory<>("speed"));
        recentlySpeedColumn.setStyle("-fx-alignment: CENTER;");

        recentlyLoginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        recentlyLoginColumn.setStyle("-fx-alignment: CENTER;");

        recentlyFolderColumn.setCellValueFactory(new PropertyValueFactory<>("folder"));
        recentlyFolderColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void contextMenuRecentlyUploadedFiles(TableCell tableCell) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem openFolder = new MenuItem("Открыть папку с файлом");
        MenuItem inQuantel = new MenuItem("в Quantel");
        MenuItem inDalet = new MenuItem("в Dalet основной");
        MenuItem inDaletFFAStrans = new MenuItem("в Dalet FFAStrans");
        MenuItem inDaletReserv = new MenuItem("в Dalet через резерв");
        MenuItem quantelNaPryamkiPC1 = new MenuItem("в Quantel на PC1");
        MenuItem quantelNaPryamkiPC2 = new MenuItem("в Quantel на PC2");
        MenuItem copyToCulture = new MenuItem("в ТК Культура");
        MenuItem copyToDezhchast = new MenuItem("в ДЧ");
        MenuItem copyToUploadFolder = new MenuItem("Выберете папку ->");

        contextMenu.getItems().addAll(inQuantel, inDalet, inDaletFFAStrans, inDaletReserv, openFolder,
                quantelNaPryamkiPC1, quantelNaPryamkiPC2, copyToCulture, copyToDezhchast, copyToUploadFolder);
        tableCell.setContextMenu(contextMenu);
        Task task = (Task) tableCell.getTableRow().getItem();
        if (task != null) {
            File folderFile = task.getUnitFile().getFile().getParentFile();
            String pathToFolder = Helper.renameFolder(folderFile.getAbsolutePath().toLowerCase());

            File fileFrom = new File(pathToFolder + File.separator + task.getFilename());

            openFolder.setOnAction(event1 -> {
                try {
                    Desktop.getDesktop().open(new File(pathToFolder));
                } catch (IOException ex) {
                    Helper.log(ex);
                }
            });

            inQuantel.setOnAction(event1 -> {
                String quantelFolderPath = "\\\\ftpres\\quantel$\\";
                File fileTo = new File(quantelFolderPath + Helper.renameFromCirrilic(task.getFilename()));
                fireCopyFile(fileFrom, fileTo, "Quantel");
            });

            inDalet.setOnAction(event1 -> {
                String rikrzFolderPath = "\\\\rikrz\\dalet-in\\";
                File fileTo = new File(rikrzFolderPath + Helper.renameFromCirrilic(task.getFilename()));
                fireCopyFile(fileFrom, fileTo, "Dalet основной");
            });

            inDaletFFAStrans.setOnAction(event1 -> {
                String rikrzFFAStrans = "\\\\rikrz\\WF-RIK\\";
                File fileTo = new File(rikrzFFAStrans + Helper.renameFromCirrilic(task.getFilename()));
                fireCopyFile(fileFrom, fileTo, "Dalet FFAStrans");
            });

            inDaletReserv.setOnAction(event1 -> {
                String rikrzReserv = "\\\\172.27.68.118\\storages\\CARBONCODER\\IN_FTP\\";
                File fileTo = new File(rikrzReserv + Helper.renameFromCirrilic(task.getFilename()));
                fireCopyFile(fileFrom, fileTo, "Dalet Резерв");
            });

            quantelNaPryamkiPC1.setOnAction(event1 -> {
                String PC1Address = "\\\\172.18.0.184\\d$\\";
                String currDateFolderPath = new SimpleDateFormat("dd-MM-yy").format(new Date()) + "\\";
                File currDateFolder = new File(PC1Address + currDateFolderPath);
                if (!currDateFolder.exists()) {
                    try {
                        Files.createDirectories(currDateFolder.toPath());
                    } catch (IOException ex) {
                        Helper.log(ex);
                    }
                }

                File fileTo = new File(currDateFolder.getAbsolutePath() + File.separator
                        + task.getFilename());
                Helper.print(fileTo);
                fireCopyFile(fileFrom, fileTo, "на PC1");

            });

            quantelNaPryamkiPC2.setOnAction(event1 -> {
                String PC2Address = "\\\\172.18.0.183\\d$\\";
                String currDateFolderPath = new SimpleDateFormat("dd-MM-yy").format(new Date()) + "\\";
                File currDateFolder = new File(PC2Address + currDateFolderPath);
                if (!currDateFolder.exists()) {
                    try {
                        Files.createDirectories(currDateFolder.toPath());
                    } catch (IOException ex) {
                        Helper.log(ex);
                    }
                }

                File fileTo = new File(currDateFolder.getAbsolutePath() + File.separator
                        + task.getFilename());
                Helper.print(fileTo);
                fireCopyFile(fileFrom, fileTo, "на PC2");
            });

            copyToCulture.setOnAction(event1 -> {
                String cultureFolderPath = "\\\\ftpres\\culture$\\";
                File fileTo = new File(cultureFolderPath + Helper.renameFromCirrilic(task.getFilename()));
                fireCopyFile(fileFrom, fileTo, "Культуру");
            });
            copyToDezhchast.setOnAction(event1 -> {
                String dezhchastFolderPath = "\\\\ftpres\\upload\\upload_wan\\DezhChast\\";
                File fileTo = new File(dezhchastFolderPath + task.getFilename());
                fireCopyFile(fileFrom, fileTo, "ДЧ");
            });
            copyToUploadFolder.setOnAction(event1 -> {
                String dezhchastFolderPath = "\\\\ftpres\\upload\\upload_wan\\DezhChast\\";
                File fileTo = new File(dezhchastFolderPath + task.getFilename());
                fireCopyFile(fileFrom, fileTo, "ДЧ");
            });

            copyToUploadFolder.setOnAction(event -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File("\\\\ftpres\\upload\\upload_wan\\"));
                File folderTo = directoryChooser.showDialog(stage);

                if (folderTo != null) {
                    System.out.println(folderTo);
                    File fileTo = new File(folderTo + File.separator + task.getFilename());
                    fireCopyFile(fileFrom, fileTo, " в " + folderTo.getName());
                }
            });
        }
    }

    private void fireCopyFile(File fileFrom, File fileTo, String text) {
        final BooleanProperty booleanPropertyTransferComplete = new SimpleBooleanProperty();

        Thread copyThread = new Thread(new Runnable() {
            @Override
            public void run() {

                boolean complete = Helper.transferFile(fileFrom, fileTo);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        booleanPropertyTransferComplete.set(complete);
                    }
                });
            }
        });
        copyThread.start();

        booleanPropertyTransferComplete.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    Alert alert = new Alert(Alert.AlertType.NONE);
                    alert.setHeaderText("");
                    alert.setGraphic(null);
                    alert.setContentText("Файл " + fileTo.getName() + " скопирован в " + text);
                    alert.initModality(Modality.NONE);
                    Timeline idleStage = new Timeline(new KeyFrame(Duration.seconds(10), new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            alert.setResult(ButtonType.CANCEL);
                            alert.hide();
                        }
                    }));
                    idleStage.setCycleCount(1);
                    idleStage.play();
                    alert.showAndWait();
                }
            }
        });
    }

    private void showUploadingFiles() {
        TableColumn<Task, String> uploadingStartTimeColumn =
                (TableColumn<Task, String>) tableViewUploading.getColumns().get(0);
        TableColumn<Task, String> uploadingFilenameColumn =
                (TableColumn<Task, String>) tableViewUploading.getColumns().get(1);
        TableColumn<Task, String> uploadingLoginColumn =
                (TableColumn<Task, String>) tableViewUploading.getColumns().get(2);
        TableColumn<Task, String> uploadingFolderColumn =
                (TableColumn<Task, String>) tableViewUploading.getColumns().get(3);

        uploadingStartTimeColumn.setCellValueFactory(new PropertyValueFactory<>("timeStartToString"));
        uploadingStartTimeColumn.setStyle("-fx-alignment: CENTER;");

        uploadingFilenameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        uploadingFilenameColumn.setStyle("-fx-alignment: CENTER;");

        uploadingLoginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        uploadingLoginColumn.setStyle("-fx-alignment: CENTER;");

        uploadingFolderColumn.setCellValueFactory(new PropertyValueFactory<>("folder"));
        uploadingFolderColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void setLeftStatusLabel() {
        Label leftStatusLabel = (Label) hBox.lookup("#leftStatusLabel");

        System.setOut(new PrintStream(System.out) {
            @Override
            public void println(Object s) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        leftStatusLabel.setText(s.toString());
                    }
                });
                super.println(s);
            }
        });
    }

    private void setRightStatusLabelText(String text) {
        rightStatusLabel.setText(text);
    }
}