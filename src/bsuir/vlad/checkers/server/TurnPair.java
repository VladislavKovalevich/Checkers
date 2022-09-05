package bsuir.vlad.checkers.server;

import bsuir.vlad.checkers.commands.GameTurn;

public class TurnPair {

    private GameTurn whiteTurn;
    private GameTurn blackTurn;

    public TurnPair() {

    }

    public void setWhiteTurn(GameTurn whiteTurn) {
        this.whiteTurn = whiteTurn;
    }

    public void setBlackTurn(GameTurn blackTurn) {
        this.blackTurn = blackTurn;
    }

    public GameTurn getWhiteTurn() {
        return whiteTurn;
    }

    public GameTurn getBlackTurn() {
        return blackTurn;
    }

}
