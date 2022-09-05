
package bsuir.vlad.checkers.commands;


import java.util.UUID;

public class CheckersTurnCmd extends AbstractCheckerCommand {

    private final UUID gameID;
    private final boolean isWhite;

    private GameTurn turn;

    public CheckersTurnCmd(final UUID gameID, boolean isWhite) {
        super(CommandType.CheckersTurnCmd);

        this.gameID = gameID;
        this.isWhite = isWhite;
    }

    public UUID getGameID() {
        return gameID;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public GameTurn getTurn() {
        return turn;
    }

    public void setTurn(GameTurn turn) {
        this.turn = turn;
    }

}
