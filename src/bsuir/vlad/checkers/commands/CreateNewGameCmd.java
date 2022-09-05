
package bsuir.vlad.checkers.commands;


import java.util.UUID;


public class CreateNewGameCmd extends AbstractCheckerCommand {

    private final String userName;
    private final boolean isWhite;

    private boolean isGameCreated;
    private GameInfo gameInfo;


    public CreateNewGameCmd(final String userName, boolean isWhite) {
        super(CommandType.CreateNewGameCmd);

        this.userName = userName;
        this.isWhite = isWhite;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isWhite() {
        return isWhite;
    }


    public void setGameCreated(boolean gameCreated) {
        isGameCreated = gameCreated;
    }

    public boolean isGameCreated() {
        return isGameCreated;
    }

    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

}
