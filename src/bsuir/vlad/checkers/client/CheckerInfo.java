
package bsuir.vlad.checkers.client;


import javafx.scene.shape.Circle;


public class CheckerInfo {

    private Circle shape;
    private double x;
    private double y;
    private boolean isOwn;

    public CheckerInfo(Circle shape, double x, double y, boolean isOwn) {
        this.shape = shape;
        this.x = x;
        this.y = y;
        this.isOwn = isOwn;
    }

    public Circle getShape() {
        return shape;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean getIsOwn() {
        return isOwn;
    }

    public void setShape(Circle shape){
        this.shape = shape;
    }

    public void setX(double x){
        this.x = x;
    }

    public void setY(double y){
        this.y = y;
    }

    public void setisOwn(boolean isOwn){
        this.isOwn = isOwn;
    }
}
