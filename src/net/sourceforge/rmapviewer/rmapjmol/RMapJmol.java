package net.sourceforge.rmapviewer.rmapjmol;
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
    private int frame = 1;
    private int number_of_frames = 1;
    private JButton rewindButton = new JButton("|←");
    private JButton prevStructureButton = new JButton("");
    private JButton playPauseButton = new JButton("▶︎");
    private JButton nextStructureButton = new JButton("");
    private JButton forwardButton = new JButton("→|");

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == timer) {
            tick();
        } else if (source == playPauseButton) {
            if (timer.isRunning()) {
                pause();
            } else {
                play();
            }
        } else if (source == rewindButton) {
            rewind();
        } else if (source == forwardButton) {
            forward();
        }
    }
    public void rewind() {
        setFrame(1);
        pause();
    }
    public void forward() {
        setFrame(number_of_frames);
        pause();
    }
    public void play() {
        timer.start();
        playPauseButton.setText("‖");
    }
    public void pause() {
        timer.stop();
        playPauseButton.setText("▶︎");
    }
    private void setFrame(int frame) {
        this.frame = frame;
        script("frame "+frame+"\n");
    }
    private void tick() {
        if (frame < number_of_frames) {
            frame = frame + 1;
            setFrame(frame);
        } else {
            pause();
        }
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
        frame.setSize(500, 530);
        frame.getContentPane().add(this, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel();
        frame.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        buttonsPanel.setLayout(new GridLayout(1, 5));
        buttonsPanel.setSize(500, 30);
        buttonsPanel.add(rewindButton);
        buttonsPanel.add(prevStructureButton);
        buttonsPanel.add(playPauseButton);
        buttonsPanel.add(nextStructureButton);
        buttonsPanel.add(forwardButton);
        rewindButton.addActionListener(this);
        prevStructureButton.addActionListener(this);
        playPauseButton.addActionListener(this);
        nextStructureButton.addActionListener(this);
        forwardButton.addActionListener(this);
        frame.setVisible(true);
        this.viewer.openStringInline(xyz);
        number_of_frames = viewer.getModelCount();
        System.out.println("number of frames: "+number_of_frames);
        timer = new Timer(100, this);
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
