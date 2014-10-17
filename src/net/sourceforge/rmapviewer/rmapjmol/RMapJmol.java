package net.sourceforge.rmapviewer.rmapjmol;
/**
 * Created by tomohiro on 14/10/17.
 */

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

import org.jmol.api.*;
import org.jmol.viewer.JmolConstants;
import org.jmol.adapter.smarter.SmarterJmolAdapter;


public class RMapJmol extends JPanel implements ComponentListener, ActionListener {
    private JmolViewer viewer;
    private String[] captions;
    private Font captionFont = new Font("Arial", Font.BOLD, 36);
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
    private int pauseCounter = 0;
    private int pauseDeciseconds = 10;

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == timer) {
            if (pauseCounter == 0) {
                nextFrame();
            } else {
                --pauseCounter;
            }
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
        } else if (source == prevStructureButton) {
            prevStructure();
        } else if (source == nextStructureButton) {
            nextStructure();
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
    public void prevStructure() {
        int prevFrame = prevCaptionFrame();
        if (prevFrame >= 0) {
            setFrame(prevFrame);
        }
        pause();
    }
    public void nextStructure() {
        int nextFrame = nextCaptionFrame();
        if (nextFrame >= 0) {
            setFrame(nextFrame);
        }
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
        script("frame "+frame);
        setStructureButtons();
    }
    private void nextFrame() {
        if (frame < number_of_frames) {
            frame = frame + 1;
            setFrame(frame);
            if (frame >= 1 && frame <= captions.length && captions[frame-1].length() > 0) {
                pauseCounter = pauseDeciseconds;
            }
        } else {
            pause();
        }
    }

    private void setStructureButtons() {
        int prevCaptionFrame = prevCaptionFrame() - 1;
        if (prevCaptionFrame >= 0 && prevCaptionFrame < captions.length) {
            prevStructureButton.setText(captions[prevCaptionFrame]);
        } else {
            prevStructureButton.setText("");
        }
        int nextCaptionFrame = nextCaptionFrame() - 1;
        if (nextCaptionFrame >= 0 && nextCaptionFrame < captions.length) {
            nextStructureButton.setText(captions[nextCaptionFrame]);
        } else {
            nextStructureButton.setText("");
        }
    }

    private int nextCaptionFrame() {
        int nextCaptionFrame = frame + 1;
        while (nextCaptionFrame > 0 && nextCaptionFrame <= captions.length && captions[nextCaptionFrame-1].length() == 0) {
            ++nextCaptionFrame;
        }
        if (nextCaptionFrame > 0 && nextCaptionFrame <= captions.length) {
            return nextCaptionFrame;
        } else {
            return -1;
        }
    }

    private int prevCaptionFrame() {
        int prevCaptionFrame = frame - 1;
        while (prevCaptionFrame > 0 && prevCaptionFrame <= captions.length && captions[prevCaptionFrame-1].length() == 0) {
            --prevCaptionFrame;
        }
        if (prevCaptionFrame > 0 && prevCaptionFrame <= captions.length) {
            return prevCaptionFrame;
        } else {
            return -1;
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
        viewer.evalString(command+"\n");
    }

    @Override
    public void paint(Graphics graphics) {
        graphics.getClipBounds(rectClip);
        viewer.renderScreenImage(graphics, cachedSize, rectClip);
        if (frame >= 1 && frame <= captions.length) {
            graphics.setColor(Color.WHITE);
            graphics.setFont(captionFont);
            graphics.drawString(captions[frame-1], 0,400);
        }
    }

    public RMapJmol(String label, String xyz, String[] cap) {
        captions = cap;
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
        setFrame(1);
    }

    public RMapJmol(String label, File xyzFile, File capFile) throws IOException {
        this(label, readFile(xyzFile), readFileLines(capFile));
    }

    public static void main(String[] args) throws IOException {
        String label = args[0];
        String xyzFilename = args[1];
        String capFilename = args[2];
        new RMapJmol(label, new File(xyzFilename), new File(capFilename));
    }

    private static String readFile(File file) throws IOException {
        StringBuilder xyzBuilder = new StringBuilder();
        BufferedReader xyzReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line = xyzReader.readLine();
        while (line != null) {
            xyzBuilder.append(line);
            xyzBuilder.append("\n");
            line = xyzReader.readLine();
        }
        return xyzBuilder.toString();
    }

    private static String[] readFileLines(File file) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        BufferedReader capReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line = capReader.readLine();
        while (line != null) {
            lines.add(line);
            line = capReader.readLine();
        }
        return lines.toArray(new String[0]);
    }
}
