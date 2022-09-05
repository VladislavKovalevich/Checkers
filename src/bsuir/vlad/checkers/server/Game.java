
package bsuir.vlad.checkers.server;


import bsuir.vlad.checkers.commands.GameStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Game {

    private final UUID gameID;

    private Player player1;
    private Player player2;

    private boolean isWriteTurn;

    private GameStatus status;

    private List<TurnPair> turns = new ArrayList();


    public Game() {
        gameID = UUID.randomUUID();
    }

    public UUID getGameID() {
        return gameID;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public void setWriteTurn(boolean writeTurn) {
        isWriteTurn = writeTurn;
    }

    public boolean isWriteTurn() {
        return isWriteTurn;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public List<TurnPair> getTurns() {
        return turns;
    }

}
