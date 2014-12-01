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
    private JFrame frame;
    private JmolViewer viewer;
    private String[] captions;
    private Font captionFont = new Font("Arial", Font.BOLD, 36);
    private Dimension cachedSize = new Dimension(500, 500);
    private Rectangle rectClip = new Rectangle();
    private Timer timer;
    private int frame_index = 1;
    private int number_of_frames = 1;
    private PathShuttlePanel shuttlePanel = new PathShuttlePanel(this);
    private Icon playIcon = new ImageIcon(getClass().getResource("/images/play.png"));
    private Icon pauseIcon = new ImageIcon(getClass().getResource("/images/pause.png"));
    private JButton rewindButton = new JButton(new ImageIcon(getClass().getResource("/images/rewind.png")));
    private JButton prevStructureButton = new JButton("");
    private JButton playPauseButton = new JButton(playIcon);
    private JButton nextStructureButton = new JButton("");
    private JButton forwardButton = new JButton(new ImageIcon(getClass().getResource("/images/fastforward.png")));
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
        playPauseButton.setIcon(pauseIcon);
    }
    public void pause() {
        timer.stop();
        playPauseButton.setIcon(playIcon);
    }
    public int getFrame() {
        return frame_index;
    }
    public void setFrame(int frame) {
        this.frame_index = frame;
        script("frame "+frame);
        setStructureButtons();
        shuttlePanel.repaint();
    }
    public String getCaption(int frame) {
        if (frame > 0 && frame <= captions.length) {
            return captions[frame-1];
        } else {
            return "";
        }
    }
    public String getCaption() {
        return getCaption(frame_index);
    }
    private void nextFrame() {
        if (frame_index < number_of_frames) {
            frame_index = frame_index + 1;
            setFrame(frame_index);
            if (getCaption().length() > 0) {
                pauseCounter = pauseDeciseconds;
            }
        } else {
            pause();
        }
    }

    private void setStructureButtons() {
        int prevCaptionFrame = prevCaptionFrame() - 1;
        prevStructureButton.setText(getCaption(prevCaptionFrame()));
        nextStructureButton.setText(getCaption(nextCaptionFrame()));
    }

    public int nextCaptionFrame(int frame) {
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

    public int nextCaptionFrame() {
        return nextCaptionFrame(frame_index);
    }

    public int prevCaptionFrame(int frame) {
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

    public int prevCaptionFrame() {
        return prevCaptionFrame(frame_index);
    }

    public String[] getCaptions() {
        return captions;
    }

    public void componentHidden(ComponentEvent event) {}
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
        if (frame_index >= 1 && frame_index <= captions.length) {
            graphics.setColor(Color.WHITE);
            graphics.setFont(captionFont);
            String caption = getCaption();
            FontMetrics fontMetrics = graphics.getFontMetrics();
            int width = fontMetrics.stringWidth(caption);
            graphics.drawString(caption, (this.getWidth() - width) / 2, this.getHeight() - 5);
        }
    }

    public void setExitOnClose(boolean exitOnClose) {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public RMapJmol(String label, String xyz, String[] cap) {
        captions = cap;
        this.setBackground(Color.BLACK);
        viewer = JmolViewer.allocateViewer(this, new SmarterJmolAdapter(null));
        viewer.setScreenDimension(new Dimension(500, 500));
        viewer.setColorBackground("black");
        this.addComponentListener(this);
        frame = new JFrame(label);
        frame.setBounds(0, 0, 500, 570);
        frame.getContentPane().add(this, BorderLayout.CENTER);
        JPanel navigationPanel = new JPanel();
        navigationPanel.setLayout(new BorderLayout());
        navigationPanel.setSize(500, 70);
        frame.getContentPane().add(navigationPanel, BorderLayout.SOUTH);
        JPanel buttonsPanel = new JPanel();
        navigationPanel.add(buttonsPanel, BorderLayout.NORTH);
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
        navigationPanel.add(shuttlePanel, BorderLayout.SOUTH);
        shuttlePanel.setPreferredSize(new Dimension(500, 30));

        this.viewer.openStringInline(xyz);
        number_of_frames = viewer.getModelCount();
        System.out.println("number of frames: "+number_of_frames);
        timer = new Timer(100, this);
        frame.setVisible(true);
        setFrame(1);
    }

    public RMapJmol(String label, File xyzFile, File capFile) throws IOException {
        this(label, readFile(xyzFile), readFileLines(capFile));
    }

    public static void main(String[] args) throws IOException {
        String label = args[0];
        String xyzFilename = args[1];
        String capFilename = args[2];
        RMapJmol jmol = new RMapJmol(label, new File(xyzFilename), new File(capFilename));
        jmol.setExitOnClose(true);
        if (args.length > 3) {
            String energyFilename = args[3];
            JFrame energyFrame = new JFrame(label+" energy");
            energyFrame.setBounds(500, 0, 1100, 570);
            energyFrame.getContentPane().add(new EnergyTreeView(jmol, new File(energyFilename)));
            energyFrame.setVisible(true);
            System.out.println("launched");
        }
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
