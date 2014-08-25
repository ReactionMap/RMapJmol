package net.sourceforge.rmapviewer.jmol;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.jmol.api.*;
import org.jmol.viewer.JmolConstants;
import org.jmol.adapter.smarter.SmarterJmolAdapter;

/**
 * Created by tomohiro on 2014/08/25.
 */
public class Jmol  extends JPanel implements ComponentListener, ActionListener {
    Dimension cachedSize = new Dimension(500, 500);
    Rectangle rectClip = new Rectangle();
    JmolViewer viewer;
    int frame = 0;
    int number_of_frames = 1;

    public Jmol(String label, String xyz) {
        setBackground(Color.BLACK);
        viewer = JmolViewer.allocateViewer(this, new SmarterJmolAdapter(null));
        viewer.setScreenDimension(new Dimension(500, 500));
        viewer.setColorBackground("black");
        this.addComponentListener(this);
        JFrame frame = new JFrame(label);
        frame.setSize(cachedSize);
        frame.add(this);
        frame.setVisible(true);
        viewer.openStringInline(xyz);
        number_of_frames = viewer.getModelCount();
        System.out.println("number of frames: "+number_of_frames);
    }

    public void script(String command) {
        viewer.evalString(command);
    }

    public void actionPerformed(ActionEvent event) {
        script("frame "+frame+"\n");
        frame = frame % number_of_frames + 1;
    }

    public void componentHidden(ComponentEvent event) {
    }
    public void componentMoved(ComponentEvent event) {
    }
    public void componentResized(ComponentEvent event) {
        updateSize();
    }
    public void componentShown(ComponentEvent event) {
        updateSize();
    }
    public void updateSize() {
        viewer.setScreenDimension(getSize(cachedSize));
        viewer.setModeMouse(JmolConstants.MOUSE_ROTATE);
        viewer.setSelectionHaloEnabled(false);
    }
    public void paint(Graphics graphics) {
        graphics.getClipBounds(rectClip);
        viewer.renderScreenImage(graphics, cachedSize, rectClip);
    }
    public static void main(String[] args) {
        new Jmol("RMapJmol", "5\n\n"+
                "C -0.110421161313 0.140272143186 0.042178145159\n"+
                "C 0.632183463977 -0.953136999573 0.627366248799\n"+
                "H 0.921307544161 -1.679208250346 -0.130339618014\n"+
                "H -1.189885997096 -0.090683451528 0.038958095453\n"+
                "O 0.317919551472 1.189730533561 -0.333314327039\n"+
                "5\n\n" +
                "C -0.110421161313 0.140272143186 0.042178145159\n" +
                "C 0.632183463977 -0.953136999573 0.627366248799\n" +
                "H 0.921307544161 -1.679208250346 -0.130339618014\n" +
                "H -1.189885997096 -0.090683451528 0.038958095453\n" +
                "O 0.317919551472 1.189730533561 -0.333314327039");
    }
}
