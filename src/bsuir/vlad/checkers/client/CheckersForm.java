package bsuir.vlad.checkers.client;


import bsuir.vlad.checkers.commands.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.geometry.Insets;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;
import java.util.List;
import java.util.UUID;


public class CheckersForm extends Application implements ClientCommandsListener {

    private  Stage parrStage;

    private  MenuBar mainMenu = new MenuBar();
    private  AnchorPane root = new AnchorPane();
    private  TabPane tabPane = new TabPane();

    private String host;
    private int    port;
    private String user;
    private String password;

    private Socket clientSocket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private TabInfo[] tabInfos = new TabInfo[5];
    private int tabCounter = 0;

    private boolean isConnected = false;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene mainScene = new Scene(root, 1200, 900);
        tabPane.setMinWidth(1200);

        root.setTopAnchor(tabPane, 25.0);
        root.getChildren().add(tabPane);

        parrStage = primaryStage;

        mainMenu.setMinWidth(1200);
        getMenu(root);

        parrStage.setOnCloseRequest(event -> {
            stopSubscription();
            if (isConnected()) {
                disconnect();
            }
            Platform.exit();
        });

        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Checkers");
        primaryStage.show();
    }

    private void getMenu(AnchorPane root){
        Menu serverMenu = new Menu("Сервер");
        Menu gameMenu = new Menu("Игра");

        MenuItem connectToServer = new MenuItem("Присоединиться к серверу");
        MenuItem disconnectBtn = new MenuItem("Отключиться");
        MenuItem exit = new MenuItem("Выход");

        MenuItem createGame = new MenuItem("Создать игру");
        MenuItem getMatchList = new MenuItem("Присоединиться к игре");
        SeparatorMenuItem separator1 = new SeparatorMenuItem();

        //MenuItem getMyCheckers = new MenuItem("Добавить шашки");
        //MenuItem getSaveGame = new MenuItem("Список сохраненных игр");
        //MenuItem getGameHistory = new MenuItem("Показать историю игр");

        disconnectBtn.setDisable(true);
        createGame.setDisable(true);
        getMatchList.setDisable(true);

        serverMenu.getItems().addAll(connectToServer, disconnectBtn, separator1, exit);
        gameMenu.getItems().addAll(createGame, getMatchList);
        mainMenu.getMenus().addAll(serverMenu, gameMenu);
        root.getChildren().addAll(mainMenu);

        final ClientCommandsListener listener = this;

        connectToServer.setOnAction(event -> {
            getConnectWindow(parrStage);

            if (isConnected()) {
                connectToServer.setDisable(true);
                disconnectBtn.setDisable(false);
                createGame.setDisable(false);
                getMatchList.setDisable(false);

                subscribeForCommands(listener);
            } else {
                connectToServer.setDisable(false);
                disconnectBtn.setDisable(true);
                createGame.setDisable(true);
                getMatchList.setDisable(true);
            }
        });

        createGame.setOnAction(event -> {
            createGameAction();
        });

        getMatchList.setOnAction(event -> {
            joinGameAction();
        });

        exit.setOnAction(event -> {
            System.exit(-1);
        });

        disconnectBtn.setOnAction(event -> {
            parrStage.setTitle("Checkers");

            stopSubscription();

            disconnect();

            connectToServer.setDisable(false);
            disconnectBtn.setDisable(true);
            createGame.setDisable(true);
            getMatchList.setDisable(true);
        });
    }

    private void joinGameAction() {
        if (tabPane.getTabs().size() == 5) {
            showError("Can't create/join more then 5 games");
            return;
        }

        AbstractCheckerCommand cmd = new GetGamesListCmd();
        if (!sendCommand(cmd)) {
            showError("Error to get games list from server.");
            return;
        }

        cmd = getCommand();
        if ((cmd == null) || (cmd.getType() != CommandType.GetGamesListCmd)) {
            showError("Unknown command from server to get games list");
            return;
        }

        GetGamesListCmd listCmd = (GetGamesListCmd) cmd;
        List<GameInfo> infos = listCmd.getGameInfos();
        if (infos.size() == 0) {
            showError("No games on server to join.");
            return;
        }

        Stage joinWindow = new Stage();
        BorderPane bp = new BorderPane();
        Scene sc = new Scene(bp, 500, 300);

        bp.setPadding(new Insets(15, 20, 20, 20));

        VBox listVbox = new VBox();
        VBox btnBox = new VBox();

        Label lbList = new Label("Список игр");

        ListView<GameInfo> gamesListView = new ListView<>();
        for (GameInfo info : infos) {
            gamesListView.getItems().add(info);
        }

        Button btnConnect = new Button("Подключиться");

        lbList.setPadding(new Insets(0,0,10,20));
        btnBox.setPadding(new Insets(20,0,0,20));

        listVbox.getChildren().addAll(lbList, gamesListView);
        btnBox.getChildren().add(btnConnect);
        bp.setCenter(listVbox);
        bp.setRight(btnBox);

        joinWindow.setTitle("Присоединение к игре");
        joinWindow.setScene(sc);
        joinWindow.initModality(Modality.WINDOW_MODAL);
        joinWindow.initOwner(parrStage);

        btnConnect.setDisable(true);

        gamesListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        gamesListView.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<GameInfo>() {
                    @Override
                    public void changed(ObservableValue<? extends GameInfo> observable, GameInfo oldValue, GameInfo newValue) {
                        btnConnect.setDisable(false);
                    }
        });

        btnConnect.setOnAction(event -> {
            GameInfo info = gamesListView.getSelectionModel().getSelectedItem();

            AbstractCheckerCommand cmd2 = new JoinGameCmd(info.getGameID(), user);

            if (!sendCommand(cmd2)) {
                showError("Error to join game.");
                return;
            }

            cmd2 = getCommand();
            if ((cmd2 == null) || (cmd2.getType() != CommandType.JoinGameCmd)) {
                showError("Unknown command from server to join game.");
                return;
            }

            JoinGameCmd joinCmd = (JoinGameCmd) cmd2;
            if (!joinCmd.isGameJoined()) {
                showError("Error to join game on server.");
                joinWindow.close();
                return;
            }

            GameInfo joinedGameInfo = joinCmd.getGameInfo();
            TabInfo tabInfo = getTabInfoByGameID(joinedGameInfo.getGameID());
            if ((tabInfo != null) && (tabInfo.getTab() != null)) {
                showInfo("Tab with game already opened.");
                tabPane.getSelectionModel().select(tabInfo.getTab());
                joinWindow.close();
                return;
            }

            boolean isWhite = (joinedGameInfo.getPlayer1().equals(user) ? true : false);

            tabInfo = createGameTabInfo(joinedGameInfo, isWhite);
            tabPane.getTabs().add(tabInfo.getTab());
            tabPane.getSelectionModel().select(tabInfo.getTab());

            tabInfo.getGameInfo().setStatus(GameStatus.GameJoined);
            CheckersStatusCmd statusCmd = new CheckersStatusCmd(tabInfo.getGameInfo());
            sendCommand(statusCmd);

            joinWindow.close();
        });

        joinWindow.showAndWait();
    }

    private TabInfo getTabInfoByGameID(final UUID gameID) {
        for (int i = 0; i < tabInfos.length; i++) {
            TabInfo tabInfo = tabInfos[i];
            if (tabInfo != null) {
                if ((tabInfo.getGameInfo() != null) &&
                        (tabInfo.getGameInfo().getGameID().equals(gameID))) {
                    return tabInfo;
                }
            }
        }
        return null;
    }

    private void createGameAction() {
        if (tabPane.getTabs().size() == 5) {
            showError("Can't create/join more then 5 games");
            return;
        }

        Stage colorCheckersSelection = new Stage();
        AnchorPane rt = new AnchorPane();
        Scene colorSelection = new Scene(rt, 500,300);

        Label lbMain = new Label("Выберите цвет шашек");

        RadioButton rbWhite = new RadioButton("Белые");
        RadioButton rbBlack = new RadioButton("Черные");

        Button btnGo = new Button("Вперед!");

        ToggleGroup tg = new ToggleGroup();

        rbWhite.setToggleGroup(tg);
        rbBlack.setToggleGroup(tg);

        rbWhite.setSelected(true);

        rt.setTopAnchor(lbMain, 30.0);
        rt.setLeftAnchor(lbMain, 200.0);

        rt.setTopAnchor(rbWhite, 90.0);
        rt.setLeftAnchor(rbWhite, 220.0);

        rt.setTopAnchor(rbBlack, 130.0);
        rt.setLeftAnchor(rbBlack, 220.0);

        rt.setTopAnchor(btnGo, 200.0);
        rt.setLeftAnchor(btnGo, 220.0);

        rt.getChildren().addAll(lbMain, rbWhite, rbBlack, btnGo);

        btnGo.setOnAction(event1 -> {
            AbstractCheckerCommand cmd = new CreateNewGameCmd(user, rbWhite.isSelected());

            if (!sendCommand(cmd)) {
                showError("Error to open new game.");
                return;
            }

            cmd = getCommand();
            if ((cmd == null) || (cmd.getType() != CommandType.CreateNewGameCmd)) {
                showError("Unknown command from server to create new game.");
                return;
            }

            CreateNewGameCmd newCmd = (CreateNewGameCmd) cmd;
            if (!newCmd.isGameCreated()) {
                showError("Error to create new game on server.");
                return;
            }

            TabInfo tabInfo = createGameTabInfo(newCmd.getGameInfo(), newCmd.isWhite());
            tabPane.getTabs().add(tabInfo.getTab());
            tabPane.getSelectionModel().select(tabInfo.getTab());

            colorCheckersSelection.close();
        });

        colorCheckersSelection.setScene(colorSelection);
        colorCheckersSelection.initModality(Modality.WINDOW_MODAL);
        colorCheckersSelection.initOwner(parrStage);
        colorCheckersSelection.showAndWait();
    }

    private TabInfo createGameTabInfo(GameInfo gameInfo, boolean isWhite) {
        TabInfo tabInfo = new TabInfo();
        tabInfos[tabCounter] = tabInfo;

        tabInfo.setGameInfo(gameInfo);
        tabInfo.setWaiting(true);
        tabInfo.setWhite(isWhite);

        Tab gameTab = new Tab();
        gameTab.setText("Game #" + (tabCounter + 1));
        tabInfo.setTab(gameTab);

        gameTab.setOnClosed(event -> {
            tabCounter--;
        });

        BorderPane borderPane = new BorderPane();
        gameTab.setContent(borderPane);
        borderPane.setPadding(new Insets(20, 20, 20, 20));

        VBox historyPanel = new VBox(10);

        tabInfo.getGameInfoLabel().setText(
                "Game ID: " + tabInfo.getGameInfo().getGameID() +
                "\nWhite: " + (tabInfo.getGameInfo().getPlayer1() != null ? tabInfo.getGameInfo().getPlayer1().toUpperCase() : "<none>") +
                "\nBlack: " + (tabInfo.getGameInfo().getPlayer2() != null ? tabInfo.getGameInfo().getPlayer2().toUpperCase() : "<none>"));

        historyPanel.setPrefSize(350, 800);
        tabInfo.getGameTurnesView().setPrefSize(350, 600);
        historyPanel.getChildren().add(tabInfo.getGameInfoLabel());
        historyPanel.getChildren().add(tabInfo.getGameTurnesView());

        Group gameGroup = new Group();
        tabInfo.setGroup(gameGroup);

        final Rectangle rect = new Rectangle(1,1, 800, 800);
        rect.setFill(Color.WHITE);
        gameGroup.getChildren().add(rect);

        borderPane.setRight(historyPanel);
        borderPane.setCenter(gameGroup);

        borderPane.setTop(tabInfo.getStatusInfoLabel());
        tabInfo.getStatusInfoLabel().setText("Game created. Waiting for another player to join ...");

        createCheckersBoard(tabInfo);
        createOwnCheckers(tabInfo);
        createOppositeCheckers(tabInfo);

        tabCounter++;

        return tabInfo;
    }

    private void getConnectWindow(Stage parrentStage) {
        Stage connectWindow = new Stage();
        AnchorPane root = new AnchorPane();
        Scene connectScene = new Scene(root, 500,400);

        Label lbMain = new Label("Присоединение");

        Label lbhost = new Label("host");
        Label lbport = new Label("port");
        Label lbUser = new Label("user");
        Label lbPassword = new Label("password");

        TextField hostField = new TextField("localhost");
        TextField portField = new TextField("5000");
        TextField userField = new TextField("vlad");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("vlad");

        Button connect = new Button("Подключиться");

        root.setLeftAnchor(lbMain, 200.0);
        root.setTopAnchor(lbMain, 30.0);

        root.setLeftAnchor(lbhost, 90.0);
        root.setTopAnchor(lbhost, 80.0);

        root.setLeftAnchor(lbport, 90.0);
        root.setTopAnchor(lbport, 120.0);

        root.setLeftAnchor(lbUser, 90.0);
        root.setTopAnchor(lbUser, 190.0);

        root.setLeftAnchor(lbPassword, 90.0);
        root.setTopAnchor(lbPassword, 230.0);

        root.setLeftAnchor(hostField, 170.0);
        root.setTopAnchor(hostField, 80.0);

        root.setLeftAnchor(portField, 170.0);
        root.setTopAnchor(portField, 120.0);

        root.setLeftAnchor(userField, 170.0);
        root.setTopAnchor(userField, 190.0);

        root.setLeftAnchor(passwordField, 170.0);
        root.setTopAnchor(passwordField, 230.0);

        root.setLeftAnchor(connect, 195.0);
        root.setTopAnchor(connect, 300.0);

        root.getChildren().addAll(connect, lbMain, lbhost, lbport, lbUser, lbPassword, hostField, portField, userField, passwordField);

        connectWindow.initOwner(parrentStage);
        connectWindow.initModality(Modality.WINDOW_MODAL);
        connectWindow.setScene(connectScene);
        connectWindow.setTitle("Connect");

        connect.setOnAction(event -> {
            if ((portField.getText().length() == 0) ||
                (hostField.getText().length() == 0) ||
                (userField.getText().length() == 0) ||
                (passwordField.getText().length() == 0)) {

                showError("Заполните все поля!");
            } else {
                this.host = hostField.getText().trim();
                this.port = Integer.parseInt(portField.getText().trim());
                this.user = userField.getText().trim();
                this.password = passwordField.getText().trim();

                if (!connectToServer()) {
                    showError("Error to connect to server.");
                    disconnect();
                }

                connectWindow.close();
            }
        });

        connectWindow.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private boolean connectToServer() {
        try{
            this.clientSocket = new Socket(this.host, this.port);

            this.objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

            isConnected = true;

            AbstractCheckerCommand cmd = new LogonCmd(this.user, this.password);
            if (!sendCommand(cmd)) {
                showError("Error to send command to server.");
                return false;
            }

            cmd = readCommand();
            if ((cmd != null) && (cmd.getType() == CommandType.LogonCmd)) {
                LogonCmd logonCmd = (LogonCmd) cmd;

                if (logonCmd.isLoggedIn()) {
                    parrStage.setTitle(parrStage.getTitle() + " - Connected to " + host + ":" + port + " (" + user.toUpperCase() + ")");
                    return true;
                }
            } else {
                // disconnected
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private void disconnect() {
        try {
            this.objectInputStream.close();
            this.objectOutputStream.close();
            this.clientSocket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        isConnected = false;
    }

    private AbstractCheckerCommand readCommand() {
        try {
            AbstractCheckerCommand cmd = (AbstractCheckerCommand) objectInputStream.readUnshared();
            if (cmd != null) {
                System.out.println(cmd.getType() + "; " + cmd.getClass().getName());
            }
            return cmd;
        } catch (Exception ex) {
            ex.printStackTrace();
            disconnect();
        }

        return null;
    }

    private boolean sendCommand(AbstractCheckerCommand command) {
        try {
            if (isConnected()) {
                objectOutputStream.writeUnshared(command);
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            disconnect();
        }

        return false;
    }

    private boolean isConnected() {
        if (isConnected) {
            return true;
        }
        return false;
    }

    private void createOwnCheckers(TabInfo tabInfo) {
        Color color;

        if (tabInfo.isWhite()) {
            color = Color.WHITE;
        } else {
            color = Color.BLACK;
        }

        for (int i = 5; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i != 6) {
                    if (j == 0 || j % 2 == 0) {
                        createNewOwnChecker(tabInfo, color, i, j);
                    }
                } else {
                    if (j % 2 != 0) {
                        createNewOwnChecker(tabInfo, color, i, j);
                    }
                }
            }
        }
    }

    private void createOppositeCheckers(TabInfo tabInfo) {
        Color color;

        if (!tabInfo.isWhite()) {
            color = Color.WHITE;
        } else {
            color = Color.BLACK;
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                if (i == 1) {
                    if (j == 0 || j % 2 == 0) {
                        createNewOppositeChecker(tabInfo, color, i, j);
                    }
                } else {
                    if (j % 2 != 0) {
                        createNewOppositeChecker(tabInfo, color, i, j);
                    }
                }
            }
        }
    }

    private void createNewOwnChecker(final TabInfo tabInfo, Color color, int i, int j) {
        Group group = tabInfo.getGroup();
        CheckerInfo[][] checkerInfos = tabInfo.getCheckerInfos();
        Rectangle[][] chboard = tabInfo.getChboard();

        Circle shape = new Circle();

        double x = chboard[i][j].getX() + 40.0;
        double y = chboard[i][j].getY() + 40.0;

        shape.setCenterX(x);
        shape.setCenterY(y);
        shape.setRadius(30.0);
        shape.setFill(color);
        shape.setStroke(Color.BLACK);
        shape.setUserData(new GameTurn(i, j));

        CheckerInfo checkerInfo = new CheckerInfo(shape, x, y, true);

        checkerInfos[i][j] = checkerInfo;

        shape.setOnMousePressed(event -> {
            if (tabInfo.isWaiting()) {
                return;
            }

            Circle circle = (Circle) event.getTarget();
            GameTurn turn = (GameTurn) circle.getUserData();

            CheckerPoint fromPoint = (turn.getToPoints().size() == 0 ?
                    turn.getFromPoint():
                    turn.getToPoints().get(turn.getToPoints().size() - 1));

            markCellsForMove(tabInfo, fromPoint.getRow(), fromPoint.getColumn());

            System.out.println("originJ = " + fromPoint.getColumn() + "; originI = " + fromPoint.getRow());

            circle.setStrokeWidth(circle.getStrokeWidth() + 5);
            circle.toFront();
        });

        shape.setOnMouseDragged(event -> {
            if (tabInfo.isWaiting()) {
                return;
            }

            Circle circle = (Circle) event.getTarget();

            circle.setCenterX(event.getX());
            circle.setCenterY(event.getY());
        });

        shape.setOnMouseReleased(event -> {
            if (tabInfo.isWaiting()) {
                return;
            }

            Circle circle = (Circle) event.getTarget();
            GameTurn turn = (GameTurn) circle.getUserData();

            CheckerPoint fromPoint = (turn.getToPoints().size() == 0 ?
                    turn.getFromPoint():
                    turn.getToPoints().get(turn.getToPoints().size() - 1));

            CheckerInfo info = checkerInfos[fromPoint.getRow()][fromPoint.getColumn()];

            if (isCheckerMovedToCorrectCell(tabInfo, circle, fromPoint)) {
                circle.setCenterX(info.getX());
                circle.setCenterY(info.getY());

                checkerInfos[fromPoint.getRow()][fromPoint.getColumn()] = null;

                setCoords(tabInfo, circle);

                CheckerPoint toPoint = turn.getToPoints().get(turn.getToPoints().size() - 1);
                checkerInfos[toPoint.getRow()][toPoint.getColumn()] = info;

                if (chboard[toPoint.getRow()][toPoint.getColumn()].getFill() == Color.RED) {
                    killCheckerInStep(tabInfo,
                            GameTurn.convertToServerPoint(tabInfo.isWhite(), fromPoint),
                            GameTurn.convertToServerPoint(tabInfo.isWhite(), toPoint));
                }

                if ((chboard[toPoint.getRow()][toPoint.getColumn()].getFill() == Color.RED) &&
                    (canBeatChecker(tabInfo, toPoint.getRow(), toPoint.getColumn()))) {
                    tabInfo.getStatusInfoLabel().setText("You can beat more checkers. Please continue turn.");

                } else {
                    // todo - send turn command
                    CheckersTurnCmd turnCmd = new CheckersTurnCmd(tabInfo.getGameInfo().getGameID(), tabInfo.isWhite());
                    turnCmd.setTurn(GameTurn.convertToServerTurn(tabInfo.isWhite(), turn));

                    sendCommand(turnCmd);

                    turn.reset();

                    tabInfo.setWaiting(true);

                    updateGameTurnesView(tabInfo, turnCmd.getTurn(), false);
                }

            } else {
                circle.setCenterX(info.getX());
                circle.setCenterY(info.getY());
            }

            markCellNormal(tabInfo);

            circle.setStrokeWidth(circle.getStrokeWidth() - 5);
        });

        group.getChildren().add(shape);
    }

    private boolean isCheckerMovedToCorrectCell(TabInfo tabInfo, Circle circle, CheckerPoint fromPoint) {
        Rectangle[][] chboard = tabInfo.getChboard();
        CheckerInfo[][] checkerInfos = tabInfo.getCheckerInfos();
        CheckerInfo info = checkerInfos[fromPoint.getRow()][fromPoint.getColumn()];

        for(int i  = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if((circle.getCenterX() > chboard[i][j].getX()) && (circle.getCenterY() > chboard[i][j].getY()) &&
                   (circle.getCenterX() < chboard[i][j].getX() + 80) && (circle.getCenterY() < chboard[i][j].getY()+ 80)){
                   if (chboard[i][j].getFill() == Color.GREEN) {
                       info.setX(chboard[i][j].getX() + 40);
                       info.setY(chboard[i][j].getY() + 40);
                       return true;
                   } else {
                       if(chboard[i][j].getFill() == Color.RED){
                           info.setX(chboard[i][j].getX() + 40);
                           info.setY(chboard[i][j].getY() + 40);
                           return true;
                       }
                   }
                }
            }
        }
        return false;
    }

    private void markCellNormal(TabInfo tabInfo) {
        Rectangle[][] chboard = tabInfo.getChboard();

        for(int i  = 0; i < 8; i++){
            for(int j = 0; j < 8; j++) {
                if(chboard[i][j].getFill() == Color.GREEN || chboard[i][j].getFill() == Color.RED){
                    chboard[i][j].setFill(Color.rgb(125, 42, 0));
                }
            }
        }
    }

    private boolean canBeatChecker(TabInfo tabInfo, int orI, int orJ) {
        CheckerInfo[][] checkerInfos = tabInfo.getCheckerInfos();

        for (int i = orI - 1; i <= orI + 1; i++) {
            for (int j = orJ - 1; j <= orJ + 1; j++) {
                if ((i >= 0) && (j >= 0) && (i < 8) && (j < 8) &&
                        ((orI - orJ == i - j) || (orJ + orI == j + i))) {
                    if (checkerInfos[i][j] != null) {
                        if (!checkerInfos[i][j].getIsOwn()) {
                            if (orI - orJ == i - j) {
                                if (orI < i) {
                                    if ((i + 1 < 8) && (j + 1 < 8)) {
                                        if (checkerInfos[i + 1][j + 1] == null) {
                                            //chboard[i + 1][j + 1].setFill(Color.RED);
                                            return true;
                                        }
                                    }
                                } else {
                                    if (orI > i) {
                                        if ((i - 1 >= 0) && (j - 1 >= 0)) {
                                            if (checkerInfos[i - 1][j - 1] == null) {
                                                //chboard[i - 1][j - 1].setFill(Color.RED);
                                                return true;
                                            }
                                        }
                                    }
                                }
                            } else {
                                if( orJ + orI == i + j) {
                                    if (orI < i) {
                                        if ((i + 1 < 8) && (j - 1 >= 0)) {
                                            if (checkerInfos[i + 1][j - 1] == null) {
                                                //chboard[i + 1][j - 1].setFill(Color.RED);
                                                return true;
                                            }
                                        }
                                    } else {
                                        if (orI > i) {
                                            if ((i - 1 >= 0) && (j + 1 < 8)) {
                                                if (checkerInfos[i - 1][j + 1] == null) {
                                                    //chboard[i - 1][j + 1].setFill(Color.RED);
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
        return false;
    }

    private void markCellsForMove(TabInfo tabInfo, int orI, int orJ) {
        Rectangle[][] chboard = tabInfo.getChboard();
        CheckerInfo[][] checkerInfos = tabInfo.getCheckerInfos();

        for (int i = orI - 1; i <= orI + 1; i++) {
           for (int j = orJ - 1; j <= orJ + 1; j++) {
               if ((i >= 0) && (j >= 0) && (i < 8) && (j < 8) &&
                  ((orI - orJ == i - j) || (orJ + orI == j + i))) {
                   if (checkerInfos[i][j] != null) {
                       if (!checkerInfos[i][j].getIsOwn()) {
                           if (orI - orJ == i - j) {
                               if (orI < i) {
                                    if ((i + 1 < 8) && (j + 1 < 8)) {
                                        if (checkerInfos[i + 1][j + 1] == null) {
                                            chboard[i + 1][j + 1].setFill(Color.RED);
                                        }
                                    }
                               } else {
                                   if (orI > i) {
                                      if ((i - 1 >= 0) && (j - 1 >= 0)) {
                                          if (checkerInfos[i - 1][j - 1] == null) {
                                              chboard[i - 1][j - 1].setFill(Color.RED);
                                          }
                                      }
                                   }
                               }
                           } else {
                               if( orJ + orI == i + j) {
                                   if (orI < i) {
                                       if ((i + 1 < 8) && (j - 1 >= 0)) {
                                           if (checkerInfos[i + 1][j - 1] == null) {
                                               chboard[i + 1][j - 1].setFill(Color.RED);
                                           }
                                       }
                                   } else {
                                       if (orI > i) {
                                           if ((i - 1 >= 0) && (j + 1 < 8)) {
                                               if (checkerInfos[i - 1][j + 1] == null) {
                                                   chboard[i - 1][j + 1].setFill(Color.RED);
                                               }
                                           }
                                       }
                                   }
                               }
                           }

                       }
                   } else {
                       if ((i < orI + 1)) {
                           chboard[i][j].setFill(Color.GREEN);
                       }
                   }

               }
           }
        }
    }

    private void setCoords(TabInfo tabInfo, Circle circle) {
        Rectangle[][] chboard = tabInfo.getChboard();
        GameTurn turn = (GameTurn) circle.getUserData();

        for(int i  = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                if(((circle.getCenterX() - 40) == chboard[i][j].getX()) &&
                   ((circle.getCenterY() - 40) == chboard[i][j].getY())) {
                    turn.addToPoint(i, j);
                    return;
                }
            }
        }
    }

    private void createNewOppositeChecker(TabInfo tabInfo, Color color, int i, int j){
        Group group = tabInfo.getGroup();
        CheckerInfo[][] checkerInfos = tabInfo.getCheckerInfos();
        Rectangle[][] chboard = tabInfo.getChboard();

        Circle shape = new Circle();

        double x = chboard[i][j].getX() + 40.0;
        double y = chboard[i][j].getY() + 40.0;

        shape.setCenterX(x);
        shape.setCenterY(y);
        shape.setRadius(30.0);
        shape.setFill(color);
        shape.setStroke(Color.BLACK);

        CheckerInfo checkerInfo = new CheckerInfo(shape, x, y, false);

        checkerInfos[i][j] = checkerInfo;

        group.getChildren().add(shape);
    }

    private void createCheckersBoard(TabInfo tabInfo) {
        int CurrentI;

        if (!tabInfo.isWhite()) {
            CurrentI = 0;
        } else {
            CurrentI = 7;
        }

        Group group = tabInfo.getGroup();
        CheckerInfo[][] checkerInfos = tabInfo.getCheckerInfos();
        Rectangle[][] chboard = tabInfo.getChboard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                checkerInfos[i][j] = null;

                chboard[i][j] = new Rectangle();
                chboard[i][j].setX((j + 1) * 80);
                chboard[i][j].setY((i + 1) * 80);
                chboard[i][j].setHeight(80);
                chboard[i][j].setWidth(80);
                chboard[i][j].setStroke(Color.BLACK);
                chboard[i][j].setStrokeWidth(1);

                if ((i % 2 == 0 && j % 2 == 1) || (i % 2 == 1 && j % 2 == 0)) {
                    chboard[i][j].setFill(Color.rgb(125, 42, 0));
                } else if ((i % 2 == 0 && j % 2 == 0) || (i % 2 == 1 && j % 2 == 1)) {
                    chboard[i][j].setFill(Color.rgb(255, 187, 79));
                }

                group.getChildren().add(chboard[i][j]);

                if (i == CurrentI){
                    String str = "";
                    int currJ;
                    int currI;

                    if (!tabInfo.isWhite()) {
                        currJ = 7 - j;
                        currI = i;
                    } else {
                        currJ = j;
                        currI = i + 2;
                    }

                    switch (currJ) {
                        case (0): str = "A"; break;
                        case (1): str = "B"; break;
                        case (2): str = "C"; break;
                        case (3): str = "D"; break;
                        case (4): str = "E"; break;
                        case (5): str = "F"; break;
                        case (6): str = "G"; break;
                        case (7): str = "H"; break;
                    }

                    Text text = new Text(str);
                    text.setX((j + 1) * 80.0 + 40);
                    text.setY((currI) * 80.0 + 50);

                    group.getChildren().add(text);
                }
            }

            int currIndex = 8 - i;
            double X = 40.0;

            if (!tabInfo.isWhite()){
                currIndex = i + 1;
                X = 760;
            }

            Text lb = new Text(Integer.toString(currIndex));

            lb.setY((i+1)*80.0 + 40.0);
            lb.setX(X);

            group.getChildren().add(lb);
        }
    }


    private boolean isSubscribed = false;
    private AbstractCheckerCommand responseCommand = null;

    private void subscribeForCommands(ClientCommandsListener listener) {
        this.isSubscribed = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while ((isConnected()) && (isSubscribed)) {
                    AbstractCheckerCommand command = readCommand();

                    if (command != null) {
                        listener.onCommand(command);
                    }
                }
            }
        }).start();
    }

    private void stopSubscription() {
        this.isSubscribed = false;
    }


    private synchronized AbstractCheckerCommand getCommand() {
        if (!isConnected()) {
            return null;
        }

        try {
            while (responseCommand == null) {
                wait(100);
            }

            AbstractCheckerCommand cmd = responseCommand;
            responseCommand = null;

            return cmd;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }


    @Override
    public synchronized void onCommand(AbstractCheckerCommand command) {
        switch (command.getType()) {
            case CreateNewGameCmd:
                responseCommand = command;
                break;
            case JoinGameCmd:
                responseCommand = command;
                break;
            case GetGamesListCmd:
                responseCommand = command;
                break;
            case CheckersStatusCmd:
                processStatusCmd((CheckersStatusCmd)command);
                break;
        }
    }

    private void processStatusCmd(CheckersStatusCmd statusCmd) {
        TabInfo tabInfo = getTabInfoByGameID(statusCmd.getGameInfo().getGameID());
        if (tabInfo == null) {
            return;
        }

        makeMove(tabInfo, statusCmd);

        setStatusInfo(tabInfo, statusCmd.getGameInfo());
        setGameInfo(tabInfo, statusCmd.getGameInfo());

        tabInfo.setGameInfo(statusCmd.getGameInfo());

        setWaiting(tabInfo);
    }

    private void setStatusInfo(final TabInfo tabInfo, GameInfo newGameInfo) {
        String text = null;

        if (((tabInfo.getGameInfo().getStatus().equals(GameStatus.GameCreated)) ||
            (tabInfo.getGameInfo().getStatus().equals(GameStatus.GameJoined))) &&
            (newGameInfo.getStatus().equals(GameStatus.GameWhiteTurn))) {
            if (tabInfo.isWhite()) {
                text = "Game joined. Your turn!";
            } else {
                text = "Game joined. Please wait for white turn.";
            }
        } else if (newGameInfo.getStatus().equals(GameStatus.GameBlackTurn)) {
            if (tabInfo.isWhite()) {
                text = "Waiting for black turn ...";
            } else {
                text = "Your turn!";
            }
        } else if (newGameInfo.getStatus().equals(GameStatus.GameWhiteTurn)) {
            if (!tabInfo.isWhite()) {
                text = "Waiting for white turn ...";
            } else {
                text = "Your turn!";
            }
        }

        setStatusInfoLabel(tabInfo, text);
    }

    private void setStatusInfoLabel(final TabInfo tabInfo, final String text) {
        if (text != null) {
            Platform.runLater(() -> {
                tabInfo.getStatusInfoLabel().setText(text);
            });
        }
    }

    private void setGameInfo(TabInfo tabInfo, GameInfo newGameInfo) {
        Platform.runLater(() -> {
            tabInfo.getGameInfoLabel().setText(
                    "Game ID: " + newGameInfo.getGameID() +
                    "\nWhite: " + (newGameInfo.getPlayer1() != null ? newGameInfo.getPlayer1().toUpperCase() : "<none>") +
                    "\nBlack: " + (newGameInfo.getPlayer2() != null ? newGameInfo.getPlayer2().toUpperCase() : "<none>"));
        });
    }

    private void setWaiting(TabInfo tabInfo) {
        if (tabInfo.getGameInfo().getStatus().equals(GameStatus.GameWhiteTurn)) {
            if (tabInfo.isWhite()) {
                tabInfo.setWaiting(false);
            } else {
                tabInfo.setWaiting(true);
            }
        } else if (tabInfo.getGameInfo().getStatus().equals(GameStatus.GameBlackTurn)) {
            if (tabInfo.isWhite()) {
                tabInfo.setWaiting(true);
            } else {
                tabInfo.setWaiting(false);
            }
        }
    }

    private void makeMove(TabInfo tabInfo, CheckersStatusCmd statusCmd) {
        GameTurn prevTurn = statusCmd.getPrevTurn();

        if (prevTurn != null) {
            updateGameTurnesView(tabInfo, prevTurn, true);

            Rectangle[][] chboard = tabInfo.getChboard();
            CheckerInfo[][] checkerInfos = tabInfo.getCheckerInfos();

            CheckerPoint fromPoint = prevTurn.getFromPoint();
            CheckerPoint toPoint = prevTurn.getToPoints().get(prevTurn.getToPoints().size() - 1);

            CheckerInfo info = tabInfo.isWhite() ?
                    checkerInfos[7 - fromPoint.getColumn()][fromPoint.getRow()] :
                    checkerInfos[fromPoint.getColumn()][7 - fromPoint.getRow()];

            if (info == null) {
                return;
            }

            Circle circle = info.getShape();

            System.out.println(prevTurn.toString());

            if (tabInfo.isWhite()) {
                checkerInfos[7 - fromPoint.getColumn()][fromPoint.getRow()] = null;
                checkerInfos[7 - toPoint.getColumn()][toPoint.getRow()] = info;

                circle.setCenterX(chboard[7 - toPoint.getColumn()][toPoint.getRow()].getX() + 40.0);
                circle.setCenterY(chboard[7 - toPoint.getColumn()][toPoint.getRow()].getY() + 40.0);

            } else {
                checkerInfos[fromPoint.getColumn()][7 - fromPoint.getRow()] = null;
                checkerInfos[toPoint.getColumn()][7 - toPoint.getRow()] = info;

                circle.setCenterX(chboard[toPoint.getColumn()][7 - toPoint.getRow()].getX() + 40.0);
                circle.setCenterY(chboard[toPoint.getColumn()][7 - toPoint.getRow()].getY() + 40.0);
            }

            checkKilledInTurn(tabInfo, prevTurn);

            info.setX(circle.getCenterX());
            info.setY(circle.getCenterY());
        }
    }

    private void checkKilledInTurn(TabInfo tabInfo, GameTurn turn) {
        if (Math.abs(turn.getFromPoint().getRow() - turn.getToPoints().get(0).getRow()) > 1) {
            killCheckerInStep(tabInfo, turn.getFromPoint(), turn.getToPoints().get(0));

            for (int i = 0; i < turn.getToPoints().size() - 1; i++) {
                killCheckerInStep(tabInfo, turn.getToPoints().get(i), turn.getToPoints().get(i + 1));
            }
        }
    }

    private void killCheckerInStep(TabInfo tabInfo, CheckerPoint fromPoint, CheckerPoint toPoint) {
        CheckerInfo[][] checkerInfos = tabInfo.getCheckerInfos();

        if (tabInfo.isWhite()) {
            int killedJ = 7 - ((fromPoint.getColumn() + toPoint.getColumn()) / 2);
            int killedI = (fromPoint.getRow() + toPoint.getRow()) / 2;

            CheckerInfo killed = checkerInfos[killedJ][killedI];

            checkerInfos[killedJ][killedI] = null;

            killed.getShape().setVisible(false);
        } else {
            int killedJ = (fromPoint.getColumn() + toPoint.getColumn()) / 2;
            int killedI = 7 - ((fromPoint.getRow() + toPoint.getRow()) / 2);

            CheckerInfo killed = checkerInfos[killedJ][killedI];

            checkerInfos[killedJ][killedI] = null;

            killed.getShape().setVisible(false);
        }
    }

    private void updateGameTurnesView(final TabInfo tabInfo, GameTurn gameTurn, boolean isPrev) {
        Platform.runLater(() -> {
            ListView<String> view = tabInfo.getGameTurnesView();

            if (((tabInfo.isWhite()) && !isPrev) || ((!tabInfo.isWhite()) && (isPrev))) {
                view.getItems().add((view.getItems().size() + 1) + ". " + gameTurn.toLog());
            } else {
                view.getItems().set((view.getItems().size() - 1),
                        view.getItems().get((view.getItems().size() - 1)) + "  /  " + gameTurn.toLog());
            }
        });
    }

}
