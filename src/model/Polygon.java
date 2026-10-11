package model;

import java.util.List;

public class Polygon {
    public final List<Line> lines;

    public Polygon() {
        this(List.of());
    }

    public Polygon(List<Line> Lines) {
        this.lines = Lines;
    }
}
