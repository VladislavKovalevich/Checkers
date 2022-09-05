
package bsuir.vlad.checkers.commands;


import java.util.UUID;


public class CheckersStatusCmd extends AbstractCheckerCommand {

    private final GameInfo gameInfo;

    private GameTurn prevTurn;

    public CheckersStatusCmd(final GameInfo gameInfo) {
        super(CommandType.CheckersStatusCmd);

        this.gameInfo = gameInfo;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public GameTurn getPrevTurn() {
        return prevTurn;
    }

    public void setPrevTurn(GameTurn prevTurn) {
        this.prevTurn = prevTurn;
    }

}
