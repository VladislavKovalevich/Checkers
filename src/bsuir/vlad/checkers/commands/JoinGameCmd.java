package bsuir.vlad.checkers.commands;


import java.util.UUID;

public class JoinGameCmd extends AbstractCheckerCommand {

    private final UUID gameID;
    private final String userName;

    private boolean isGameJoined;
    private GameInfo gameInfo;

    public JoinGameCmd(final UUID gameID, final String userName) {
        super(CommandType.JoinGameCmd);

        this.gameID = gameID;
        this.userName = userName;
    }

    public UUID getGameID() {
        return gameID;
    }

    public String getUserName() {
        return userName;
    }

    public void setGameJoined(boolean gameJoined) {
        isGameJoined = gameJoined;
    }

    public boolean isGameJoined() {
        return isGameJoined;
    }

    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

}
