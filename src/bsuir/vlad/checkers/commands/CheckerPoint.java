
package bsuir.vlad.checkers.commands;


import java.io.Serializable;


public class CheckerPoint implements Serializable {

    private int row;
    private int column;

    public CheckerPoint(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

}
