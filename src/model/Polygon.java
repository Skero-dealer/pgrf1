package model;

import java.util.List;

public class Polygon {
    public List<Line> lines;
    private boolean isEditing = false;

    public Polygon() {
        this(List.of());
    }

    public Polygon(List<Line> Lines) {
        this.lines = Lines;
    }

    // Demonstrace že chápu princip zapouzdření a umím ho využít (jen u lines to odmítám)
    public boolean getIsEditing() {
        return this.isEditing;
    }

    public void setIsEditing(boolean newIsEditing) {
        this.isEditing = newIsEditing;
    }
}
