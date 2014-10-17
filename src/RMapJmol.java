/**
 * Created by tomohiro on 14/10/17.
 */
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.jmol.api.*;
import org.jmol.viewer.JmolConstants;
import org.jmol.adapter.smarter.SmarterJmolAdapter;


public class RMapJmol extends JPanel implements ComponentListener, ActionListener {
    private JmolViewer viewer;
    private Dimension cachedSize = new Dimension(500, 500);
    private Rectangle rectClip = new Rectangle();
    private Timer timer;
    private int frame = 0;
    private int number_of_frames = 1;

    public void actionPerformed(ActionEvent event) {
        frame = frame % number_of_frames + 1;
        script("frame "+frame+"\n");
    }
    public void componentHidden(ComponentEvent event) {
        System.exit(0);
    }
    public void componentMoved(ComponentEvent event) {}
    public void componentResized(ComponentEvent event) {
        this.updateSize();
    }
    public void componentShown(ComponentEvent event) {
        this.updateSize();
    }

    private void updateSize() {
        viewer.setScreenDimension(getSize(cachedSize));
        viewer.setModeMouse(JmolConstants.MOUSE_ROTATE);
        viewer.setSelectionHaloEnabled(false);
    }

    public void script(String command) {
        System.out.println(command);
        viewer.evalString(command);
    }

    @Override
    public void paint(Graphics graphics) {
        graphics.getClipBounds(rectClip);
        viewer.renderScreenImage(graphics, cachedSize, rectClip);
    }

    public RMapJmol(String label, String xyz) {
        this.setBackground(Color.BLACK);
        viewer = JmolViewer.allocateViewer(this, new SmarterJmolAdapter(null));
        viewer.setScreenDimension(new Dimension(500, 500));
        viewer.setColorBackground("black");
        this.addComponentListener(this);
        JFrame frame = new JFrame(label);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(this.cachedSize);
        frame.add(this);
        frame.setVisible(true);
        this.viewer.openStringInline(xyz);
        number_of_frames = viewer.getModelCount();
        System.out.println("number of frames: "+number_of_frames);
        timer = new Timer(100, this);
        timer.start();
    }

    public RMapJmol(String label, File xyzFile) throws IOException {
        this(label, readFile(xyzFile));
    }

    public static void main(String[] args) throws IOException {
        String label = args[0];
        String xyzFilename = args[1];
        new RMapJmol(label, new File(xyzFilename));
    }

    private static String readFile(File xyzFile) throws IOException {
        StringBuilder xyzBuilder = new StringBuilder();
        BufferedReader xyzReader = new BufferedReader(new InputStreamReader(new FileInputStream(xyzFile), "UTF-8"));
        String line = xyzReader.readLine();
        while (line != null) {
            xyzBuilder.append(line);
            xyzBuilder.append("\n");
            line = xyzReader.readLine();
        }
        return xyzBuilder.toString();
    }
}
