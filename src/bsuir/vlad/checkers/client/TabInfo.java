package bsuir.vlad.checkers.client;

import bsuir.vlad.checkers.commands.GameInfo;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.shape.Rectangle;

public class TabInfo {

    // game tab
    private Tab tab;

    // group to display
    private Group group;

    // board
    private final Rectangle[][] chboard = new Rectangle[8][8];

    // array of checkers
    private final CheckerInfo[][] checkerInfos = new CheckerInfo[8][8];

    // game info
    private GameInfo gameInfo;

    private Label statusInfoLabel = new Label("");

    private Label gameInfoLabel = new Label("Game ID: \nWhite: \nBlack: ");

    private ListView<String> gameTurnesView = new ListView<>();

    private boolean isWaiting;

    private boolean isWhite;


    public TabInfo() {
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public Tab getTab() {
        return tab;
    }

    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }

    public CheckerInfo[][] getCheckerInfos() {
        return checkerInfos;
    }

    public Rectangle[][] getChboard() {
        return chboard;
    }

    public Label getGameInfoLabel() {
        return gameInfoLabel;
    }

    public ListView<String> getGameTurnesView() {
        return gameTurnesView;
    }

    public Label getStatusInfoLabel() {
        return statusInfoLabel;
    }

    public void setWaiting(boolean waiting) {
        isWaiting = waiting;
    }

    public boolean isWaiting() {
        return isWaiting;
    }

    public void setWhite(boolean white) {
        isWhite = white;
    }

    public boolean isWhite() {
        return isWhite;
    }

}

