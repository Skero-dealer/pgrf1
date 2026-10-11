package graphics.rasterizer;

import graphics.Raster;
import model.Line;

public class TrivialLineRasterizer implements LineRasterizer {

    private final Raster raster;

    public TrivialLineRasterizer(Raster Raster) {
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

    private void rasterize(int x1, int y1, int x2, int y2, int color) {

        // TODO: X2 = X1- vertikální čára

        if (x1 == x2) {
            int startY = Math.min (y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = startY; y <= endY; y++) {
                raster.setPixel(x1,y,color);
            }
            return;
        }

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        // TODO: X2 < X1 je nunté prohodit X2 a X1

        if (x1>x2){
            int tempX = x1; x1 = x2; x2 = tempX;
            int tempY = y1; y1 = y2; y2 = tempY;
        }

        // TODO: Pokud je (y2 - y1) > (x2- x1) - jdeme po y

        if (dy > dx) {
            if(y1>y2) {
                int tempX = x1; x1 = x2; x2 = tempX;
                int tempY = y1; y1 = y2; y2 = tempY;
            }
            float k = (x2 - x1)/ (float) (y2 - y1);
            float q = x1 - k * y1;

            for (int y = y1; y <= y2; y++) {
                float x = k * y + q;
                raster.setPixel(Math.round(x), y, color);
            }
        }

        float k = (y2 - y1) / (float) (x2 - x1);
        float q = y1 - k * x1;

        for (int x = x1; x <= x2; x++) {
            float y = k * x + q;
            raster.setPixel(x, Math.round(y), color);
        }

    }

}
