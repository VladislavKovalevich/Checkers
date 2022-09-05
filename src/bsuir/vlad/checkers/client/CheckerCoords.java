
package bsuir.vlad.checkers.client;


public class CheckerCoords {

    private double centerX;
    private double centerY;

    private int currI;
    private int currJ;

    private int newI;
    private int newJ;

    public CheckerCoords(int currI, int currJ, int newI, int newJ) {
        this.currI = currI;
        this.currJ = currJ;
        this.newI = newI;
        this.newJ = newJ;
    }

    public int getCurrI() {
        return currI;
    }

    public int getCurrJ() {
        return currJ;
    }

    public int getNewI() {
        return newI;
    }

    public int getNewJ() {
        return newJ;
    }

    public void setCurrI(int currI) {
        this.currI = currI;
    }

    public void setCurrJ(int currJ) {
        this.currJ = currJ;
    }

    public void setNewI(int newI) {
        this.newI = newI;
    }

    public void setNewJ(int newJ) {
        this.newJ = newJ;
    }

}
