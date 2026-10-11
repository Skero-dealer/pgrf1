package graphics.rasterizer;

import graphics.Raster;
import model.Line;

public class BresenhamLineRasterizer implements LineRasterizer {

    private final Raster raster;

    public BresenhamLineRasterizer(Raster Raster) {
        this.raster = Raster;
    }

    @Override
    public void rasterize(Line line) {
        rasterize(
                line.point1.x,
                line.point1.y,
                line.point2.x,
                line.point2.y,
                line.color
        );
    }

    private void plot_x(int x1, int y1, int x2, int y2, int color) {
        if(x1 > x2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;

            temp = y1;
            y1 = y2;
            y2 = temp;
        }

        int increment;
        int dx = x2 - x1;
        int dy = Math.abs(y2 - y1);
        if (y2 >= y1) {
            increment = 1;
        }  else {
            increment = -1;
        }

        int y = y1;
        int d = 2 * dy - dx;
        for (int x = x1; x <= x2; x++) {
            raster.setPixel(x, y, color);

            if (d > 0) {
                y = y + increment;
                d = d + 2 * (dy - dx);
            } else {
                d = d + 2 * dy;
            }
        }
    }

    private void plot_y(int x1, int y1, int x2, int y2, int color) {
        if(y1 > y2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;

            temp = y1;
            y1 = y2;
            y2 = temp;
        }

        int increment;
        int dy = y2 - y1;
        int dx = Math.abs(x2 - x1);
        if (x2 >= x1) {
            increment = 1;
        }  else {
            increment = -1;
        }

        int x = x1;
        int d = 2 * dx - dy;
        for (int y = y1; y <= y2; y++) {
            raster.setPixel(x, y, color);

            if (d > 0) {
                x = x + increment;
                d = d + 2 * (dx - dy);
            } else {
                d = d + 2 * dx;
            }
        }
    }

    private void rasterize(int x1, int y1, int x2, int y2, int color) {
        if(x1 == x2 && y1 == y2) {
            raster.setPixel(x1, y1, color);
            return;
        }

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        if (dx > dy) {
            plot_x(x1, y1, x2, y2, color);
        } else {
            plot_y(x1, y1, x2, y2, color);
        }
    }
}