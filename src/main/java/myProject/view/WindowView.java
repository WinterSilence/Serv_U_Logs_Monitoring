package myProject.view;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
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
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseDragEvent;
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
import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
    private Label copyAfterInfoLabel;
    private Label rightStatusLabel;

    private TextField searchTextField;

    private ChoiceBox<String> recentlyTaskChoiceBox;

    private String title = "WorkProjectApp 20170905";
    private String selectedLogin = " All";
    private String searchText = "";
    private ArrayList<Task> selectedTasksSorted = new ArrayList<>();

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
        root = FXMLLoader.load(getClass().getClassLoader().getResource("WorkProjectApplication.fxml"));
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
        setSearchTextField();
        setOfflineProperty();
        setCopyAfterInfoLabel();
        setStartCircleAndStopButton();
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
        searchTextField = (TextField) anchorPaneRecently.lookup("#searchTextField");

        recentlyTaskChoiceBox = (ChoiceBox<String>) splitPane.lookup("#recentlyTaskChoiceBox");
        copyAfterInfoLabel = (Label) splitPane.lookup("#copyAfterInfoLabel");

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

        showOnlineSessions();
        showUploadingFiles();
        showRecentlyUploadedFiles();
    }

    private void setOfflineProperty() {
        fxmlController.startConnectionProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                } else {
//                    setStartCircle(Color.RED);
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

    private void setSearchTextField() {
        searchTextField.setOnKeyReleased(event -> {
            searchText = searchTextField.getText();
            setTableViewRecently();
        });
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
            Optional<String> resultFtpDialog = dialog.showAndWait();
            if (resultFtpDialog.isPresent()) {
                String ftpAddress = resultFtpDialog.get().toLowerCase();
                if (ftpAddress.startsWith("ftp:\\") || ftpAddress.startsWith("ftp:/")) {
                    ftpAddress = ftpAddress.replaceFirst("^(ftp)\\W+", "");
                }
                ftpAddress = ftpAddress.replaceAll("(\\\\|/)*$", "");
                FtpSource ftpSource = FileSourceFactory.createFtpSource();
                ftpSource.setFtpAddress(ftpAddress);
                boolean result;
                while ((result = passwordDialog(ftpSource))) {
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
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setContentText("File not found or some IO error occurred!!!");
                                alert.showAndWait();
                  }
*/
//                fxmlController.offlineProperty().addListener(new ChangeListener<Boolean>() {
//                    @Override
//                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                        if (newValue) {
//                            ftpSource.stop();
//                        }
//                    }
//                });
                if (!result) {
                    startButtonText.setVisible(true);
                    startButton.setDisable(false);
                    FTPButton.setDisable(false);
                    todayButton.setDisable(false);
                    yesterdayButton.setDisable(false);
                }
            } else {
                startButtonText.setVisible(true);
                startButton.setDisable(false);
                FTPButton.setDisable(false);
                todayButton.setDisable(false);
                yesterdayButton.setDisable(false);
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

    private void setStartCircleAndStopButton() {
        Circle startCircle = (Circle) anchorPaneRecently.lookup("#startCircle");
        fxmlController.connectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    startCircle.setFill(Color.LIGHTGREEN);
                    stopButton.setDisable(false);
//                    stopButton.setDisable(false);
                } else {
                    startCircle.setFill(Color.RED);
                    stopButton.setDisable(true);
                }
            }
        });
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
        int focusedIndex = tableViewRecently.getSelectionModel().getFocusedIndex();
        Task focusedTask = null;
        if (focusedIndex >= 0) {
            focusedTask = tableViewRecently.getItems().get(focusedIndex);
        }
        ArrayList<Task> selectedTasks = new ArrayList<>(tableViewRecently.getSelectionModel().getSelectedItems());

        List<Task> resultTableRecentlyFiles = filterTaskListHours(myModel.getCompletedTasks());

        resultTableRecentlyFiles.addAll(filterTaskListHours(myModel.getUncompletedTasks()));

        TreeSet<String> recentFilesLoginUpdate = new TreeSet<>();
        for (Task task : resultTableRecentlyFiles) {
            recentFilesLoginUpdate.add(task.getLogin());
            if (task.getFolder().endsWith("upload_wan\\DezhChast")) {
                recentFilesLoginUpdate.add("Dezhchast");
            }

            if (task.getFolder().endsWith("obmen-utro\\for_moscow\\Vesti_utro")) {
                recentFilesLoginUpdate.add("Utro-obmen");
            }

        }
        recentFilesLoginUpdate.add(" All");

        if (recentFilesLogins.size() == 0) {
            recentFilesLogins.addAll(recentFilesLoginUpdate);
            recentlyTaskChoiceBox.setItems(FXCollections.observableArrayList(recentFilesLogins));
            recentlyTaskChoiceBox.setValue(" All");
        }

        if (!recentFilesLogins.equals(recentFilesLoginUpdate)) {
            recentFilesLogins = recentFilesLoginUpdate;
            recentlyTaskChoiceBox.setItems(FXCollections.observableArrayList(recentFilesLogins));
            recentlyTaskChoiceBox.setValue(selectedLogin);
        }

        resultTableRecentlyFiles = filterTaskListSelectedChoiceBox(resultTableRecentlyFiles);
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
        resultTableRecentlyFiles = filterTaskListSameTasks(resultTableRecentlyFiles);
        resultTableRecentlyFiles = filterTaskListSearchText(resultTableRecentlyFiles);

        //todo Сортировка с жёсткой привязкой к отображению (нельзя изменить в окне)
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
        //todo Bugged, maybe later (text selection change color)
/*
        TableColumn<Task, String> recentlyFilenameColumn =
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(1);
        recentlyFilenameColumn.setCellFactory(new Callback<TableColumn<Task, String>, TableCell<Task, String>>() {
            @Override
            public TableCell<Task, String> call(TableColumn<Task, String> param) {
                return new TableCell<Task, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                            setStyle("");
                        } else {

                            setGraphic(null);
                            if (!searchText.equals("") && item.toLowerCase().contains(searchText.toLowerCase())) {
                                Text text1 = new Text(item);
                                Text text2 = new Text(" -111");
                                TextFlow buildTextFlow = new TextFlow(text1, text2);
                                buildTextFlow.setTextAlignment(TextAlignment.CENTER);
                                buildTextFlow.setPrefWidth(Region.USE_PREF_SIZE);
                                buildTextFlow.setPrefHeight(Region.USE_PREF_SIZE);
                                setGraphic(buildTextFlow);
                                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            } else {
                                setText(item);
                                setTextFill(Color.BLACK);
                                setStyle("");
                                setContentDisplay(ContentDisplay.TEXT_ONLY);
                            }
                        }
                    }
                };
            }
        });
*/
        tableViewRecently.setItems(FXCollections.observableArrayList(resultTableRecentlyFiles));

        if (resultTableRecentlyFiles.contains(focusedTask)) {
            focusedIndex = resultTableRecentlyFiles.indexOf(focusedTask);
        } else {
            focusedIndex = -1;
        }

        selectedTasksSorted = new ArrayList<>();
        for (Task task : selectedTasks) {
            if (resultTableRecentlyFiles.contains(task)) {
                selectedTasksSorted.add(task);
            }
        }

        if (selectedTasksSorted.size() > 0) {
            int[] selectedIndexes = new int[selectedTasksSorted.size() - 1];
            for (int i = 0; i < selectedIndexes.length; i++) {
                selectedIndexes[i] = resultTableRecentlyFiles.indexOf(selectedTasksSorted.get(i + 1));
            }
            if (selectedIndexes.length > 0) {
                tableViewRecently.getSelectionModel().selectIndices(resultTableRecentlyFiles.indexOf(
                        selectedTasksSorted.get(0)), selectedIndexes);
            } else {
                tableViewRecently.getSelectionModel().focus(focusedIndex);
            }
        } else {
            tableViewRecently.getSelectionModel().focus(focusedIndex);
        }
        setRightStatusLabelText("Size - " + resultTableRecentlyFiles.size());
    }

    private void setSelectedCheckboxColumn() {
        for (Task task : selectedTasksSorted) {
            task.setCheckbox(true);
        }
    }

    private void clearCheckboxColumn() {
        for (Task task : tableViewRecently.getItems()) {
            task.setCheckbox(false);
        }
    }

    private List<Task> filterTaskListSelectedChoiceBox(List<Task> list) {
        if (selectedLogin.equals(" All")) return list;
        List<Task> result = new ArrayList<>();
        if (selectedLogin.equals("Dezhchast")) {
            for (Task task : list) {
                if (task.getFolder().endsWith("upload_wan\\DezhChast")) {
                    result.add(task);
                }
            }
            return result;
        }
        if (selectedLogin.equals("Utro-obmen")) {
            for (Task task : list) {
                if (task.getFolder().endsWith("obmen-utro\\for_moscow\\Vesti_utro")) {
                    result.add(task);
                }
            }
        }
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
        List<Task> sortList = new ArrayList<>();
        sortList.addAll(list);
        List<Task> result = new ArrayList<>();

        for (Task currentTask : list) {
            Iterator<Task> iterator = sortList.iterator();
            while (iterator.hasNext()) {
                Task task = iterator.next();
                if (currentTask.equals(task)) {
                    result.add(currentTask);
                } else if (currentTask.getFilename().equals(task.getFilename())
                        && currentTask.getLogin().equals(task.getLogin())
                        && currentTask.getFolder().equals(task.getFolder())) {
                    iterator.remove();
                }
            }
        }
        return result;
    }

    private List<Task> filterTaskListSearchText(List<Task> list) {
        if (searchText.equals("")) return list;
        List<Task> result = new ArrayList<>();
        for (Task task : list) {
            if (task.getFilename().toLowerCase().contains(searchText.toLowerCase())) {
                result.add(task);
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
        resultListOnlineSessions.sort(new Comparator<Session>() {
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
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(1);
        TableColumn<Task, String> recentlyFilenameColumn =
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(2);
        TableColumn<Task, String> recentlySizeColumn =
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(3);
        TableColumn<Task, String> recentlyStartColumn =
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(4);
        TableColumn<Task, String> recentlySpeedColumn =
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(5);
        TableColumn<Task, String> recentlyLoginColumn =
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(6);
        TableColumn<Task, String> recentlyFolderColumn =
                (TableColumn<Task, String>) tableViewRecently.getColumns().get(7);

        recentlyStartColumn.setVisible(false);
        recentlySpeedColumn.setVisible(false);
        Label recentlyCheckBoxColumnLabel = new Label("□");
        TableColumn<Task, Boolean> recentlyCheckBoxColumn = new TableColumn<>();
        recentlyCheckBoxColumn.setGraphic(recentlyCheckBoxColumnLabel);
        tableViewRecently.getColumns().set(0, recentlyCheckBoxColumn);
        recentlyCheckBoxColumn.setSortable(false);
        recentlyCheckBoxColumn.setResizable(false);
        recentlyCheckBoxColumn.setMinWidth(30);
        recentlyCheckBoxColumn.setPrefWidth(30);
        recentlyCheckBoxColumn.setMaxWidth(30);
        recentlyCheckBoxColumn.setStyle("-fx-alignment: CENTER;");

        recentlyCheckBoxColumn.setCellValueFactory(new PropertyValueFactory<>("checkbox"));

        recentlyCheckBoxColumn.setCellFactory(new Callback<TableColumn<Task, Boolean>, TableCell<Task, Boolean>>() {
            @Override
            public TableCell<Task, Boolean> call(TableColumn<Task, Boolean> param) {
                return new CheckBoxTableCell<>();
            }
        });

        recentlyCheckBoxColumnLabel.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown()) {
                    System.out.println("click");
                    for (Task task : selectedTasksSorted) {
                        task.setCheckbox(false);
                    }
                }
            }
        });

        tableViewRecently.setEditable(true);

        recentlyTaskChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    selectedLogin = newValue;
                    clearCheckboxColumn();
                    update();
                }
            }
        });

        tableViewRecently.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tableViewRecently.setStyle("-fx-selection-bar: DODGERBLUE; -fx-selection-bar-non-focused: DEEPSKYBLUE;");

        tableViewRecently.setRowFactory(new Callback<TableView<Task>, TableRow<Task>>() {
            @Override
            public TableRow<Task> call(TableView<Task> tableView) {
                final TableRow<Task> row = new TableRow<Task>() {
                    {
                        TableRow<Task> row1 = this;
                        setOnDragDetected(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent event) {
                                startFullDrag();
                                getTableView().getSelectionModel().select(getIndex());
                                contextMenuRecentlyUploadedFiles(row1);
                            }
                        });
                        setOnMouseDragEntered(new EventHandler<MouseDragEvent>() {
                            @Override
                            public void handle(MouseDragEvent event) {
                                getTableView().getSelectionModel().select(getIndex());
                                contextMenuRecentlyUploadedFiles(row1);
                                clearCheckboxColumn();
                                selectedTasksSorted.clear();
                                selectedTasksSorted.addAll(getTableView().getSelectionModel().getSelectedItems());
                                setSelectedCheckboxColumn();
                            }
                        });
                        setOnMousePressed(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent event) {
                                getTableView().getSelectionModel().select(getIndex());
                                if (event.isPrimaryButtonDown() || (row1.getItem() != null && !row1.getItem().checkboxProperty().get())) {
                                    clearCheckboxColumn();
                                }
                                selectedTasksSorted.clear();
                                selectedTasksSorted.addAll(getTableView().getSelectionModel().getSelectedItems());
                                contextMenuRecentlyUploadedFiles(row1);
                                setSelectedCheckboxColumn();
                            }
                        });
                        setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) {
                                int focusedIndex = tableViewRecently.getSelectionModel().getFocusedIndex();
                                Task focusedTask = null;
                                if (focusedIndex >= 0) {
                                    focusedTask = tableViewRecently.getItems().get(focusedIndex);
                                    try {
                                        Desktop.getDesktop().open(new File(Helper.renameFolder(focusedTask.getUnitFile().getFile().toString())));
                                    } catch (IOException ex) {
                                        Helper.log(ex);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void updateItem(Task task, boolean empty) {
                        if (!empty) {
                            if (task.getTimeEndToString().equals("")) {
                                for (Node node : getChildren()) {
                                    ((TableCell) node).setTextFill(Color.BLACK);
                                    ((TableCell) node).setBackground(new Background(new BackgroundFill(Color.LIGHTPINK, new CornerRadii(2),
                                            new Insets(0.0, 0.0, 0.0, 0.0))));
                                }
                            } else if (task.getTimeEnd().before(Helper.yesterday())) {
                                for (Node node : getChildren()) {
                                    ((TableCell) node).setTextFill(Color.GREY);
                                    ((TableCell) node).setBackground(new Background(new BackgroundFill(null, null,
                                            null)));
                                }
                            } else {
                                for (Node node : getChildren()) {
                                    ((TableCell) node).setTextFill(Color.BLACK);
                                    ((TableCell) node).setBackground(new Background(new BackgroundFill(null, null, null)));
                                }
                            }
                        }
                        super.updateItem(task, empty);
                    }
                };
                return row;
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

    private void contextMenuRecentlyUploadedFiles(TableRow<Task> row) {
        if (row.getItem() == null) return;
        ContextMenu contextMenu = new ContextMenu();
        MenuItem inQuantel = new MenuItem("в Quantel");
        MenuItem inDalet = new MenuItem("в Dalet основной");
        MenuItem inAirManager = new MenuItem("в Air-manager");
        MenuItem inDaletReserv = new MenuItem("в Dalet через резерв");
        MenuItem quantelNaPryamkiPC1 = new MenuItem("в Quantel на PC1");
        MenuItem quantelNaPryamkiPC2 = new MenuItem("в Quantel на PC2");
        MenuItem copyToEMG = new MenuItem("в EMG");
        MenuItem copyToCulture = new MenuItem("в ТК Культура");
        MenuItem copyToDezhchast = new MenuItem("в ДЧ");
        MenuItem copyToObmenUtro = new MenuItem("ДУРам");

        Menu soundTo = new Menu("Извлечь звук в ->");
        MenuItem soundToQuantel = new MenuItem("Quantel");
        MenuItem soundToDalet = new MenuItem("Dalet");
        MenuItem soundToUtro = new MenuItem("ДУРам");
        MenuItem soundToFolder = new MenuItem("Выберете папку ->");

        MenuItem copyToUploadFolder = new MenuItem("Копировать в ->");
        MenuItem openFolder = new MenuItem("Открыть папку с файлом");

        contextMenu.getItems().addAll(
                inQuantel, inDalet, inAirManager, inDaletReserv,
                quantelNaPryamkiPC1, quantelNaPryamkiPC2, copyToEMG,
                copyToCulture, copyToDezhchast, copyToObmenUtro, soundTo);
        soundTo.getItems().addAll(soundToQuantel, soundToDalet, soundToUtro, soundToFolder);

        Task task = row.getItem();
        boolean checkIfArchive = task.getFilename().endsWith(".rar") || task.getFilename().endsWith(".zip");
        if (checkIfArchive) {
            MenuItem unZIP = new MenuItem("Распаковать файл в ->");
            contextMenu.getItems().add(unZIP);
            unZIP.setOnAction(event -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File("\\\\ftpres\\upload\\upload_wan\\"));
                File folderTo = directoryChooser.showDialog(stage);
                if (folderTo != null) {
                    unZIP(folderTo.getAbsolutePath(), task);
                }
            });
        }
        contextMenu.getItems().addAll(copyToUploadFolder, openFolder);

        inQuantel.setOnAction(event1 -> {
            File folderTo = new File("\\\\ftpres\\quantel$\\");
            fireCopyFiles(folderTo, "Quantel", true);
        });

        inDalet.setOnAction(event1 -> {
            File folderTo = new File("\\\\rikrz\\dalet-in\\");
            fireCopyFiles(folderTo, "Dalet основной", true);
        });

        inAirManager.setOnAction(event1 -> {
            File folderTo = new File("\\\\vfs\\air-manager$\\");
            fireCopyFiles(folderTo, "Air-manager", true);
        });

        inDaletReserv.setOnAction(event1 -> {
            File folderTo = new File("\\\\172.27.68.118\\storages\\CARBONCODER\\IN_FTP\\");
            fireCopyFiles(folderTo, "Dalet Резерв", true);
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
            File folderTo = new File(currDateFolder.getAbsolutePath());
            fireCopyFiles(folderTo, "PC1", false);
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

            File folderTo = new File(currDateFolder.getAbsolutePath());
            fireCopyFiles(folderTo, "PC2", false);
        });

        copyToEMG.setOnAction(event1 -> {
            File folderTo = new File("\\\\ftpres3\\emg$\\");
            fireCopyFiles(folderTo, "EMG", false);
        });

        copyToCulture.setOnAction(event1 -> {
            File folderTo = new File("\\\\ftpres\\culture$\\");
            fireCopyFiles(folderTo, "Культуру", true);
        });

        copyToDezhchast.setOnAction(event1 -> {
            File folderTo = new File("\\\\ftpres\\upload\\upload_wan\\DezhChast\\");
            fireCopyFiles(folderTo, "ДЧ", false);
        });

        copyToObmenUtro.setOnAction(event1 -> {
            File folderTo = new File("\\\\ftpres\\obmen-utro$\\for_moscow\\");
            fireCopyFiles(folderTo, "ДУР", false);
        });

        soundToQuantel.setOnAction(event -> {
            File folderTo = new File("\\\\ftpres\\quantel$\\");
            encodeSound(folderTo, "Quantel (звук)", true);
        });

        soundToDalet.setOnAction(event -> {
            File folderTo = new File("\\\\rikrz\\dalet-in\\");
            encodeSound(folderTo, "Dalet (звук)", true);
        });

        soundToUtro.setOnAction(event -> {
            File folderTo = new File("\\\\ftpres\\obmen-utro$\\for_moscow\\");
            encodeSound(folderTo, "Utro-Obmen (звук)", false);
        });

        soundToFolder.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File("\\\\ftpres\\upload\\upload_wan\\"));
            File folderTo = directoryChooser.showDialog(stage);
            if (folderTo != null) {
                encodeSound(folderTo, folderTo.getName(), false);
            }
        });

        copyToUploadFolder.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File("\\\\ftpres\\upload\\upload_wan\\"));
            File folderTo = directoryChooser.showDialog(stage);
            if (folderTo != null) {
                fireCopyFiles(folderTo, folderTo.getName(), false);
            }
        });

        openFolder.setOnAction(event1 -> {
            try {
                Task focusTask = tableViewRecently.getItems()
                        .get(tableViewRecently.getSelectionModel().getFocusedIndex());
                if (System.getProperty("os.name").startsWith("Windows")) {
                    Runtime.getRuntime().exec("explorer.exe /select,"
                            + Helper.renameFolder(focusTask.getUnitFile().getFile().getAbsolutePath()));
                }
            } catch (IOException ex) {
                Helper.log(ex);
            }
        });

        row.setContextMenu(contextMenu);
    }

    private void unZIP(String folderTo, Task task) {
        if (!new File("C:\\Program Files\\WinRAR\\").isDirectory()) return;
        String pathToFolder = Helper.renameFolder(task.getUnitFile().getFile().getParent().toLowerCase());
        String fileFrom = pathToFolder + File.separator + task.getFilename();
        String command = "C:\\Program Files\\WinRAR\\"
                + "winrar x " + fileFrom + " " + folderTo;
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Helper.log(ex);
        }
    }

    private void encodeSound(File folderTo, String text, boolean rename) {
        List<Task> selected = new ArrayList<>();
        for (Task task : tableViewRecently.getItems()) {
            if (task.checkboxProperty().get()) {
                selected.add(task);
            }
        }
        for (Task task : selected) {
            File folderFile = task.getUnitFile().getFile().getParentFile();
            String pathToFolder = Helper.renameFolder(folderFile.getAbsolutePath().toLowerCase());
            File fileFrom = new File(pathToFolder + File.separator + task.getFilename());
            File fileToTemp = new File("D:/video/temp/" +
                    fileFrom.getName().replaceAll("\\.\\w*$", "") +
                    ".wav");

            File fileTo = rename ?
                    new File((folderTo +
                            File.separator +
                            Helper.renameFromCirrilic(task.getFilename())).replaceAll("\\.\\w*$", "") +
                            ".wav") :
                    new File(folderTo +
                            File.separator +
                            task.getFilename().replaceAll("\\.\\w*$", "") +
                            ".wav");
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("pcm_s16le");
            audio.setChannels(2);
            audio.setSamplingRate(48000);

            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setFormat("wav");
            attrs.setAudioAttributes(audio);
            Encoder encoder = new Encoder();

            Thread encodeThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        encoder.encode(fileFrom, fileToTemp, attrs);
                        fireCopyFile(fileToTemp, text, fileTo);
                    } catch (EncoderException ex) {
                        Helper.log(ex);
                    }
                }
            });
            encodeThread.start();
        }
    }

    private void fireCopyFile(File fileFrom, String text, File fileTo) {
        final BooleanProperty booleanPropertyTransferComplete = new SimpleBooleanProperty();

        Thread copyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Helper.transferFile(fileFrom, fileTo);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            booleanPropertyTransferComplete.set(true);
                        }
                    });
                } catch (FileSystemException ex) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText("");
                            alert.setGraphic(null);
                            alert.setContentText(ex.getReason());
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
                    });
                } catch (IOException ioex) {
                    Helper.log(ioex);
                }
            }
        });
        copyThread.start();

        booleanPropertyTransferComplete.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("");
                    alert.setTitle(fileFrom.getName());
                    alert.setGraphic(null);
                    alert.setContentText("Файл " + fileFrom.getName() + " скопирован в " + text);
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

    private void fireCopyFiles(File folderTo, String text, boolean rename) {
        List<Task> selected = new ArrayList<>();
        for (Task task : tableViewRecently.getItems()) {
            if (task.checkboxProperty().get()) {
                selected.add(task);
            }
        }
        for (Task task : selected) {
            File folderFile = task.getUnitFile().getFile().getParentFile();
            String pathToFolder = Helper.renameFolder(folderFile.getAbsolutePath().toLowerCase());
            File fileFrom = new File(pathToFolder + File.separator + task.getFilename());
            File fileTo = rename ?
                    new File(folderTo + File.separator + Helper.renameFromCirrilic(task.getFilename())) :
                    new File(folderTo + File.separator + task.getFilename());
            fireCopyFile(fileFrom, text, fileTo);
        }
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

        tableViewUploading.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (event.isSecondaryButtonDown()) {
                    Helper.print("Right Button Clicked in tableViewUploading");

                    EventTarget eventTarget = event.getTarget();
                    TableCell tableCell = null;
                    if (eventTarget instanceof TableCell) {
                        tableCell = (TableCell) eventTarget;
                    } else if (eventTarget instanceof Text) {
                        tableCell = (TableCell) ((Text) eventTarget).getParent();
                    }
                    if (tableCell != null) {
                        contextMenuUploadingFiles(tableCell);
                    }
                }
            }
        });
    }

    private void contextMenuUploadingFiles(TableCell tableCell) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem inQuantel = new MenuItem("в Quantel по прибытию");
        MenuItem inDalet = new MenuItem("в Dalet основной по прибытию");
        MenuItem quantelNaPryamkiPC1 = new MenuItem("в Quantel на PC1 по прибытию");
        MenuItem quantelNaPryamkiPC2 = new MenuItem("в Quantel на PC2 по прибытию");
        MenuItem copyToCulture = new MenuItem("в ТК Культура по прибытию");
        MenuItem copyToDezhchast = new MenuItem("в ДЧ по прибытию");
        MenuItem copyToUploadFolder = new MenuItem("Выберете папку для копирования по прибытию:");
        MenuItem openFolder = new MenuItem("Открыть папку с файлом");

        contextMenu.getItems().addAll(inQuantel, inDalet,
                quantelNaPryamkiPC1, quantelNaPryamkiPC2, copyToCulture, copyToDezhchast, copyToUploadFolder, openFolder);
        tableCell.setContextMenu(contextMenu);
        Task task = (Task) tableCell.getTableRow().getItem();
        if (task != null) {
            File folderFile = task.getUnitFile().getFile().getParentFile();
            String pathToFolder = Helper.renameFolder(folderFile.getAbsolutePath().toLowerCase());

            File fileFrom = new File(pathToFolder + File.separator + task.getFilename());
            inQuantel.setOnAction(event1 -> {
                String quantelFolderPath = "\\\\ftpres\\quantel$\\";
                File fileTo = new File(quantelFolderPath + Helper.renameFromCirrilic(task.getFilename()));
                fireCopyFileOnComplete(fileFrom, fileTo, "в Quantel");
            });

            inDalet.setOnAction(event1 -> {
                String rikrzFolderPath = "\\\\rikrz\\dalet-in\\";
                File fileTo = new File(rikrzFolderPath + Helper.renameFromCirrilic(task.getFilename()));
                fireCopyFileOnComplete(fileFrom, fileTo, "в Dalet основной");
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
                fireCopyFileOnComplete(fileFrom, fileTo, "на PC1");

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
                fireCopyFileOnComplete(fileFrom, fileTo, "на PC2");
            });

            copyToCulture.setOnAction(event1 -> {
                String cultureFolderPath = "\\\\ftpres\\culture$\\";
                File fileTo = new File(cultureFolderPath + Helper.renameFromCirrilic(task.getFilename()));
                fireCopyFileOnComplete(fileFrom, fileTo, "в Культуру");
            });
            copyToDezhchast.setOnAction(event1 -> {
                String dezhchastFolderPath = "\\\\ftpres\\upload\\upload_wan\\DezhChast\\";
                File fileTo = new File(dezhchastFolderPath + task.getFilename());
                fireCopyFileOnComplete(fileFrom, fileTo, "в ДЧ");
            });

            copyToUploadFolder.setOnAction(event -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File("\\\\ftpres\\upload\\upload_wan\\"));
                File folderTo = directoryChooser.showDialog(stage);

                if (folderTo != null) {
                    File fileTo = new File(folderTo + File.separator + task.getFilename());
                    fireCopyFileOnComplete(fileFrom, fileTo, "в " + folderTo);
                }
            });

            openFolder.setOnAction(event1 -> {
                try {
                    Desktop.getDesktop().open(new File(pathToFolder));
                } catch (IOException ex) {
                    Helper.log(ex);
                }
            });
        }
    }

    private Map<File, Set<ControlUploadProcess>> mapOfProcesses = new ConcurrentHashMap<>();

    private void fireCopyFileOnComplete(File fileFrom, File fileTo, String text) {
        if (fileFrom.exists()) {
            Thread waitForEndUploadThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Set<ControlUploadProcess> setOfProcesses;
                    if (!mapOfProcesses.containsKey(fileFrom)) {
                        setOfProcesses = new HashSet<>();
                    } else {
                        setOfProcesses = mapOfProcesses.get(fileFrom);
                    }
                    setOfProcesses = Collections.synchronizedSet(setOfProcesses);
                    ControlUploadProcess controlUploadProcess = new ControlUploadProcess();
                    controlUploadProcess.setFile(fileFrom);
                    controlUploadProcess.setProcess(text);
                    setOfProcesses.add(controlUploadProcess);
                    mapOfProcesses.put(fileFrom, setOfProcesses);
                    Helper.print("SetOfProcesses size - " + setOfProcesses.size());
                    boolean running = true;
                    while (running) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                int count = 0;
                                for (Set<ControlUploadProcess> setOfCup : mapOfProcesses.values()) {
                                    count = count + setOfCup.size();
                                }
                                copyAfterInfoLabel.setText(String.valueOf(count));
                                copyAfterInfoLabel.setVisible(true);
                            }
                        });
                        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(fileFrom))) {
                            running = false;
                        } catch (FileNotFoundException ex) {
                            // wait for file accessible
                        } catch (IOException ioException) {
                            Helper.log(ioException);
                        }
                        Helper.pause(5);
                    }
                    fireCopyFile(fileFrom, text, fileTo);
                    setOfProcesses.remove(controlUploadProcess);
                    if (setOfProcesses.size() == 0) {
                        mapOfProcesses.remove(fileFrom);
                    }

                    if (mapOfProcesses.size() == 0) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                copyAfterInfoLabel.setVisible(false);
                            }
                        });

                    }

                }
            });
            waitForEndUploadThread.start();
        }
    }

    private void setCopyAfterInfoLabel() {
        copyAfterInfoLabel.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown()) {
                    Dialog<Pair<String, String>> dialog = new Dialog<>();
                    dialog.setTitle("Ожидают окончания загрузки");
                    dialog.setHeaderText("");

                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new Insets(20, 150, 10, 10));

                    int rowIndex = 0;
                    for (Set<ControlUploadProcess> setOfCups : mapOfProcesses.values()) {
                        for (ControlUploadProcess cup : setOfCups) {
                            grid.add(new Label("Файл " + cup.getFile().getName() + " будет скопирован "
                                    + cup.getProcess() + " после окончания загрузки"), 0, rowIndex++);
                        }
                    }
                    dialog.getDialogPane().setContent(grid);
                    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

                    dialog.showAndWait();
                }
            }
        });
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

    private class ControlUploadProcess {
        private File file;
        private String process;

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public String getProcess() {
            return process;
        }

        public void setProcess(String process) {
            this.process = process;
        }
    }
}