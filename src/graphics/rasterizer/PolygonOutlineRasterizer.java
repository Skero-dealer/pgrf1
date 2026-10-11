package graphics.rasterizer;

import java.util.List;

import model.*;

public class PolygonOutlineRasterizer implements PolygonRasterizer {
    private final LineRasterizer lineRasterizer;

    public PolygonOutlineRasterizer(LineRasterizer targetLineRasterizer) {
      this.lineRasterizer = targetLineRasterizer;
   }

    @Override 
    public void rasterize(Polygon polygon) {
        List<Line> lines = polygon.lines;
        rasterizeLines(lines);

        if (lines.size() > 1) {
            Line lastLine = lines.getLast();
            Line firstLine = lines.getFirst();

            lineRasterizer.rasterize(new Line(lastLine.point2, firstLine.point1, lastLine.color));
        }
    }

    private void rasterizeLines(List<Line> lines) {
        for (Line line: lines) {
            lineRasterizer.rasterize(line);
        }
    }
}
