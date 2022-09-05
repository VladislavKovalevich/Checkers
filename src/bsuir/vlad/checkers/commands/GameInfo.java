package bsuir.vlad.checkers.commands;


import java.io.Serializable;
import java.util.UUID;


public class GameInfo implements Serializable {

    private final UUID gameID;
    private final String player1;
    private final String player2;
    private GameStatus status;

    public GameInfo(final UUID gameID, final String player1, final String player2, final GameStatus status) {
        this.gameID = gameID;
        this.player1 = player1;
        this.player2 = player2;
        this.status = status;
    }

    public UUID getGameID() {
        return gameID;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "White: " + (player1 != null ? player1 : "<none>") +
                " - Black: " + (player2 != null ? player2 : "<none>") +
                " (" + gameID + ")";
    }

}
