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

    // Funkce pro kreslení celé úsečky podle osy X
    private void plot_x(int x1, int y1, int x2, int y2, int color) {
        // Prohodíme souřadnice, aby dominantní souřadnice (x) rostla
        // Děláme to kvůli minus operace.
        if(x1 > x2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;

            temp = y1;
            y1 = y2;
            y2 = temp;
        }

        // Zvolíme jestli jedem nahoru anebo dolů podle osy Y.
        int increment;
        if (y2 >= y1) {
            increment = 1;
        }  else {
            increment = -1;
        }

        // Vypočítáme si rozhodovací veličinu "d", ta určuje jestli se máme posunout o vedlejší souřadnici
        // tedy bez ní bychom měli jenom rovnou úsečky
        int dx = x2 - x1;
        int dy = Math.abs(y2 - y1);
        int y = y1;
        int d = 2 * dy - dx;
        // Loopnem přes všechny souřadnice x a začneme u x1
        for (int x = x1; x <= x2; x++) {
            raster.setPixel(x, y, color);

            // Tady rozhodneme jestli se máme posunout nahoru nebo dolů
            if (d > 0) {
                y = y + increment;
                d = d + 2 * (dy - dx);
            } else {
                // Není třeba se nikam posunovat, jdeme prostě dál ve stejné výšce
                d = d + 2 * dy;
            }
        }
    }

    // Funkce pro kreslení celé úsečky podle osy Y
    private void plot_y(int x1, int y1, int x2, int y2, int color) {
        // Prohodíme souřadnice, aby dominantní souřadnice (y) rostla
        // Děláme to kvůli minus operace.
        if(y1 > y2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;

            temp = y1;
            y1 = y2;
            y2 = temp;
        }

        // Zvolíme jestli jedem vlevo anebo doprava podle osy X.
        int increment;
        if (x2 >= x1) {
            increment = 1;
        }  else {
            increment = -1;
        }

        // Vypočítáme si rozhodovací veličinu "d", ta určuje jestli se máme posunout o vedlejší souřadnici
        // tedy bez ní bychom měli jenom rovnou úsečky
        int dy = y2 - y1;
        int dx = Math.abs(x2 - x1);
        int x = x1;
        int d = 2 * dx - dy;
        // Loopnem přes všechny souřadnice y a začneme u y1
        for (int y = y1; y <= y2; y++) {
            raster.setPixel(x, y, color);

            // Tady rozhodneme jestli se máme posunout vlevo nebo doprava
            if (d > 0) {
                x = x + increment;
                d = d + 2 * (dx - dy);
            } else {
                // Není třeba se nikam posunovat, jdeme prostě dál ve stejném X
                d = d + 2 * dx;
            }
        }
    }

    // Tato funkce rozhoduje o tom, jak budeme postupovat při vykreslování
    private void rasterize(int x1, int y1, int x2, int y2, int color) {
        // Pokud je to prostě jen bod, tak ho vykreslíme a nepočítáme nic dalšího.
        if(x1 == x2 && y1 == y2) {
            raster.setPixel(x1, y1, color);
            return;
        }

        // Podle vzdáleností, zvolíme dominantní osu
        // Např. dx = 13 (pixelů), dy = 9px   ==> zvolíme plot_x
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        if (dx > dy) {
            // Kreslíme podle osy x
            plot_x(x1, y1, x2, y2, color);
        } else {
            // Kreslíme podle osy y
            plot_y(x1, y1, x2, y2, color);
        }
    }
}