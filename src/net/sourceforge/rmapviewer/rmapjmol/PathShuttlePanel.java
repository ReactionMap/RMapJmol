package net.sourceforge.rmapviewer.rmapjmol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * Created by tomohiro on 2014/10/18.
 */
public class PathShuttlePanel extends JPanel implements MouseMotionListener {
    private RMapJmol jmol;
    private int gap = 4;
    private Point drag_start;
    private double mouseNotch = 20.0d;

    @Override
    public void paint(Graphics graphics) {
        int frame = jmol.getFrame();
        super.paint(graphics);
        graphics.setColor(this.getBackground());
        graphics.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        graphics.setFont(this.getFont());
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int left = this.getWidth() / 2 - 1;
        int right = this.getWidth() / 2 + 1;
        String caption = jmol.getCaption(frame);
        int width = fontMetrics.stringWidth(caption);
        int y = (this.getHeight() - fontMetrics.getHeight()) / 2 + fontMetrics.getHeight();
        graphics.setColor(Color.RED);
        graphics.drawRect((this.getWidth() - width) / 2 - 1, 0, width + 1, getHeight()-1);
        graphics.setColor(Color.BLACK);
        graphics.drawString(caption, (this.getWidth() - width) / 2, y);
        left -= width / 2;
        right += width / 2 + gap;
        while (left >= 0) {
            frame = jmol.prevCaptionFrame(frame);
            caption = jmol.getCaption(frame);
            if (caption.length() == 0) break;
            width = fontMetrics.stringWidth(caption);
            left -= width + gap;
            graphics.drawString(caption, left, y);
        }
        frame = jmol.getFrame();
        while (right <= getWidth()) {
            frame = jmol.nextCaptionFrame(frame);
            caption = jmol.getCaption(frame);
            if (caption.length() == 0) break;
            width = fontMetrics.stringWidth(caption);
            graphics.drawString(caption, right, y);
            right += width + gap;
        }
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        Point point = event.getPoint();
        if (drag_start == null) {
            drag_start = point;
        } else {
            double dx = point.getX() - drag_start.getX();
            if (dx < mouseNotch * -1.0d) {
                jmol.prevStructure();
                drag_start = point;
            } else if (dx > mouseNotch) {
                jmol.nextStructure();
                drag_start = point;
            }
        }
    }
    @Override
    public void mouseMoved(MouseEvent event) {
        drag_start = null;
    }

    public PathShuttlePanel(RMapJmol jmol) {
        this.jmol = jmol;
        this.setBackground(Color.WHITE);
        this.setFont(new Font("Arial", Font.BOLD, 12));
        this.addMouseMotionListener(this);
    }
}
