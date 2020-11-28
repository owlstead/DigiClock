package digiclock;

import static digiclock.ClockUtil.convertFromDimensionInMM;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

public class DigiClock extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Dimension DIMENSION_IN_MM = new Dimension(60, 20);
    private static final Dimension MINIMUM_DIMENSION_IN_MM = new Dimension(40, 20);

    /**
     * Inner class that allows for dragging and closing the clock frame.
     */
    private class ClockMouseAdapter extends MouseAdapter {
        private int pointerLocationX;
        private int pointerLocationY;

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                DigiClock.this.dispose();
            }
        }
        
        @Override
        public void mousePressed(MouseEvent me) {
            pointerLocationX = me.getX();
            pointerLocationY = me.getY();
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            DigiClock.this.setLocation(DigiClock.this.getLocation().x + me.getX() - pointerLocationX,
                    DigiClock.this.getLocation().y + me.getY() - pointerLocationY);
        }
    }

    public static void main(String[] args) {
        var clock = new DigiClock();
        clock.setVisible(true);
    }
    
    public DigiClock() {
        setTitle("24-hour clock");
        // --> resizing hard to implement? setUndecorated(true); <--
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // reduces flickering during resize if set to same color as ClockPanel
        setBackground(Color.DARK_GRAY);
        setSize(convertFromDimensionInMM(DIMENSION_IN_MM));
        setMinimumSize(convertFromDimensionInMM(MINIMUM_DIMENSION_IN_MM));
        
        var clockFont = loadFont();
        var clockPanel = new ClockPanel(ClockPrecision.SECOND, Color.GREEN, Color.DARK_GRAY, clockFont);
        add(clockPanel);
        
        var clockMouseAdapter = new ClockMouseAdapter();
        addMouseListener(clockMouseAdapter);
        addMouseMotionListener(clockMouseAdapter);
    }
    
    private static Font loadFont() {
        Font font;
        try {
            var is = ClockUtil.class.getResourceAsStream("E1234.ttf");
            font = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch(@SuppressWarnings("unused") Exception e) {
            // choose a default font if the font cannot be loaded for some reason or other
            font = new Font("Sans-Serif", Font.BOLD, 20);
        }
        return font;
    }
}
