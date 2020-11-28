package digiclock;

import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

public class ClockPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final String MAX_SIZED_TIME_STRING = "00:00:00";

    // i.e. 10 percent on both sides
    private static final float MARGIN_PART_X = 0.02F;
    private static final float MARGIN_PART_Y = 0.05F;

    public class ClockTimerTask extends TimerTask {

        @Override
        public void run() {
            // --> is this how we should render? <---
            ClockPanel.this.drawDigitalClock(ClockPanel.this.getGraphics());
            var delay = calculateDelay(precision);
            timer.schedule(new ClockTimerTask(), delay);
        }
    }

    public class Repainter implements Runnable {

        @Override
        public void run() {
            ClockPanel.this.drawDigitalClock(getGraphics());
        }
    }

    private final Timer timer;
    private final ClockPrecision precision;
    private final Color foregroundColor;
    private final Font clockFont;

    private Font currentClockFont;
    private boolean sizeChanged = true;

    public ClockPanel(ClockPrecision precision, Color foregroundColor, Color backgroundColor, Font clockFont) {
        this.precision = precision;
        this.foregroundColor = foregroundColor;
        this.clockFont = clockFont;
        this.currentClockFont = clockFont;

        setBackground(backgroundColor);

        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                sizeChanged = true;
            }
        });

        // note that the Swing timer is so imprecise that it's usefulness is basically reduced to zero
        this.timer = new Timer("ClockTimer");
        var initialDelay = calculateDelay(precision);
        this.timer.schedule(new ClockTimerTask(), initialDelay);

        setIgnoreRepaint(true);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        drawDigitalClock(graphics);
    }

    /**
     * Draws the digital clock using the font field and given a graphics context.
     * 
     * @param graphics the graphics contents
     */
    // --> is synchronized sufficient to prevent deadlock in paintComponent e.g. during resizing? <--
    public synchronized void drawDigitalClock(Graphics graphics) {
        super.paintComponent(graphics);

        // var dimension = getSize();
        var componentBounds = getBounds().getBounds2D();
        var marginSizeX = componentBounds.getWidth() * MARGIN_PART_X;
        var marginSizeY = componentBounds.getHeight() * MARGIN_PART_Y;
        var marginSizeMin = Math.min(marginSizeX, marginSizeY);

        if (sizeChanged) {
            var innerBounds = new Rectangle2D.Double(0.0, 0.0, componentBounds.getWidth() - 2 * marginSizeMin,
                    componentBounds.getHeight() - 2 * marginSizeMin);

            // currentClockFont = resizeFontForDimension(graphics, clockFont, MAX_SIZED_TIME_STRING, innerDimension);
            currentClockFont = resizeFontToFitInBox(graphics, clockFont, MAX_SIZED_TIME_STRING, innerBounds);
            sizeChanged = false;
        }

        var graphics2 = (Graphics2D) graphics;
        
        graphics2.setColor(foregroundColor);

        // --> would it be a good idea to create my own graphics rendering here? <--
        // e.g. var clockGraphics = graphics.create();

        if (graphics instanceof Graphics2D) {
            ((Graphics2D) graphics).setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        }

        graphics2.setFont(currentClockFont);

        var metrics = graphics2.getFontMetrics(currentClockFont);
        var metrics2 = metrics.getLineMetrics(MAX_SIZED_TIME_STRING, graphics2);
        double xCenter = componentBounds.getWidth() * 0.5;
        double yCenter = componentBounds.getHeight() * 0.5;
        
        
        String timeString = getTimeString(precision, LocalTime.now());

        int x = (int) (xCenter - metrics.stringWidth(MAX_SIZED_TIME_STRING) / 2.0);
        int y = (int) (yCenter + metrics2.getHeight() / 2.0 - metrics.getDescent() / 2);

        graphics.drawString(timeString, x, y);

        Toolkit.getDefaultToolkit().sync();
    }

    private static int calculateDelay(ClockPrecision precission) {
        var delayNow = Instant.now();
        return precission.getInMilli() - (int) (delayNow.toEpochMilli() % precission.getInMilli());
    }

    private static String getTimeString(ClockPrecision precision, LocalTime time) {
        // TODO take care of precision

        // pattern for 24 hrs
        var pattern = DateTimeFormatter.ofPattern("HH:mm:ss");
        return time.format(pattern);
    }
    
    /**
     * Calculates the best font size to fit in a certain dimension (in pixels) given a graphics context, a font-type and
     * a maximum sized text; then returns the font.
     * 
     * @param graphics     the graphics for which to compute the dimensions
     * @param font         the font for which to compute the dimension
     * @param text         the (max sized) text to display
     * @param box the size in which the text needs to be displayed, excluding margins
     * @return the correctly sized font
     */
    private static Font resizeFontToFitInBox(Graphics graphics, Font font, String text, Rectangle2D box) {
        // get metrics from the graphics
        var orgBounds = graphics.getFontMetrics(font).getStringBounds(MAX_SIZED_TIME_STRING, graphics);
        
        // calculate font size
        double difTextWidth = box.getWidth() / orgBounds.getWidth();
        double difTextHeigth = box.getHeight() / orgBounds.getHeight();
        double minDif = Math.min(difTextWidth, difTextHeigth);

        return font.deriveFont((float) (font.getSize2D() * minDif));
    }
}
