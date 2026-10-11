package model;

public class Line {
    public int color;
    public Point point1;
    public Point point2;

    public Line(Point Point1, Point Point2, int Color) {
        this.color = Color;
        this.point1 = Point1;
        this.point2 = Point2;
    }
}