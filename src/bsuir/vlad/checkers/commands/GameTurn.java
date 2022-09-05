
package bsuir.vlad.checkers.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameTurn implements Serializable {

    private CheckerPoint fromPoint;

    private List<CheckerPoint> toPoints = new ArrayList<>();


    public GameTurn(int row, int column) {
        this.fromPoint = new CheckerPoint(row, column);
    }

    public CheckerPoint getFromPoint() {
        return fromPoint;
    }

    public List<CheckerPoint> getToPoints() {
        return toPoints;
    }

    public void addToPoint(int row, int column) {
        this.toPoints.add(new CheckerPoint(row, column));
    }

    public void reset() {
        if (toPoints.size() > 0) {
            fromPoint = toPoints.get(toPoints.size() - 1);
            toPoints.clear();
        }
    }

    @Override
    public String toString() {
        return "from i: " + fromPoint.getRow() + "; j: " + fromPoint.getColumn() +
                (toPoints.size() > 0 ? "; to i: " + toPoints.get(0).getRow() + "; j: " + toPoints.get(0).getColumn() : "");
    }

    public String toLog() {
        if (toPoints.size() == 0) {
            return "";
        }

        String str = "" + convertX(fromPoint.getRow()) + (fromPoint.getColumn() + 1);

        if (Math.abs(fromPoint.getRow() - toPoints.get(0).getRow()) > 1) {
            for (CheckerPoint point : toPoints) {
                str += " : " + convertX(point.getRow()) + (point.getColumn() + 1);
            }
        } else {
            for (CheckerPoint point : toPoints) {
                str += " - " + convertX(point.getRow()) + (point.getColumn() + 1);
            }
        }

        return str;
    }

    private String convertX(int x) {
        switch (x) {
            case 0: return "a";
            case 1: return "b";
            case 2: return "c";
            case 3: return "d";
            case 4: return "e";
            case 5: return "f";
            case 6: return "g";
            case 7: return "h";
        }
        return "";
    }


    public static GameTurn convertToServerTurn(boolean isWhite, GameTurn clientTurn) {
        GameTurn serverTurn = null;

        if (isWhite) {
            serverTurn = new GameTurn(clientTurn.getFromPoint().getColumn(), 7 - clientTurn.getFromPoint().getRow());
            for (CheckerPoint clientToPoint : clientTurn.getToPoints()) {
                serverTurn.addToPoint(clientToPoint.getColumn(), 7 - clientToPoint.getRow());
            }
        } else {
            serverTurn = new GameTurn(7 - clientTurn.getFromPoint().getColumn(), clientTurn.getFromPoint().getRow());
            for (CheckerPoint clientToPoint : clientTurn.getToPoints()) {
                serverTurn.addToPoint(7 - clientToPoint.getColumn(), clientToPoint.getRow());
            }
        }

        return serverTurn;
    }

    public static CheckerPoint convertToServerPoint(boolean isWhite, CheckerPoint clientPoint) {
        if (isWhite) {
            return new CheckerPoint(clientPoint.getColumn(), 7 - clientPoint.getRow());
        } else {
            return new CheckerPoint(7 - clientPoint.getColumn(), clientPoint.getRow());
        }
    }
}
