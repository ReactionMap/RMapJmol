package net.sourceforge.rmapviewer.rmapjmol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by tomohiro on 14/10/24.
 */
public class EnergyTreeView extends JPanel implements ActionListener {
    private double[] energies;
    private int[] captionIndices;
    private double min_energy;
    private double max_energy;
    private RMapJmol jmol;
    private Timer timer = new Timer(100, this);

    private double framePosition(int index) {
        int i;
        for (i = 0; i < captionIndices.length - 1; i++) {
            if (captionIndices[i+1] >= index)
                break;
        }
        if (i >= captionIndices.length - 1)
            return (double)(captionIndices.length - 1);
        return (double)i + (double)(index - captionIndices[i]) / (double)(captionIndices[i+1]-captionIndices[i]);
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int frame = jmol.getFrame();
        double scale_x = (double)(getWidth() - 60) / (captionIndices.length);
        double scale_y = (double)(getHeight() - 80) / (max_energy - min_energy);
        int offset_x = 50;
        int offset_y = 40;
        ((Graphics2D)graphics).setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 1; i < energies.length; i++) {
            double e1 = max_energy - energies[i-1];
            double e2 = max_energy - energies[i];
            int x1 = (int) (framePosition(i) * scale_x) + offset_x;
            int y1 = (int) (e1 * scale_y) + offset_y;
            int x2 = (int) (framePosition(i+1) * scale_x) + offset_x;
            int y2 = (int) (e2 * scale_y) + offset_y;
            graphics.setColor(Color.BLACK);
            graphics.drawLine(x1, y1, x2, y2);
        }
        ((Graphics2D)graphics).setStroke(new BasicStroke(1));
        int base_x = (int) offset_x;
        int base_y = (int)((max_energy - min_energy) * scale_y) + offset_y;
        double magnitude = Math.pow(10.0, Math.floor(Math.log10(max_energy - min_energy)));
        graphics.setColor(Color.BLACK);
        graphics.drawString("kJ/mol", 10, 30);
        for (double e = 0.0; e <= max_energy-min_energy; e += magnitude) {
            int y = (int)((max_energy - e - min_energy) * scale_y) + offset_y;
            graphics.drawLine(base_x, y, getWidth()-10, y);
            String eStr = ""+e;
            graphics.drawString(eStr, base_x - fontMetrics.stringWidth(eStr) - 2, y + fontMetrics.getAscent()/2);
        }
        for (int i = 0; i < captionIndices.length; i++) {
            int x = (int)(i*scale_x + offset_x);
            String caption = jmol.getCaption(captionIndices[i]);
            graphics.setColor(Color.BLACK);
            graphics.drawString(caption, x-fontMetrics.stringWidth(caption)/2, base_y + fontMetrics.getHeight()+2);
            graphics.setColor(Color.LIGHT_GRAY);
            graphics.drawLine(x, offset_y, x, base_y);
        }
        /* shall draw a grid on Y axis */
        graphics.setColor(Color.BLACK);
        graphics.drawLine(base_x, offset_y, base_x, base_y);
        graphics.drawLine(base_x, (int)(max_energy*scale_y)+offset_y, getWidth()-10, (int)(max_energy*scale_y)+offset_y);
        graphics.setColor(Color.RED);
        graphics.drawLine((int)(framePosition(frame)*scale_x)+offset_x, 0, (int)(framePosition(frame)*scale_x)+offset_x, getHeight()-18);
    }

    public EnergyTreeView(RMapJmol jmol, double[] energies) {
        setBackground(Color.WHITE);
        this.setFont(new Font("Arial", Font.PLAIN, 12));
        this.jmol = jmol;
        this.energies = energies;
        this.min_energy = energies[0];
        this.max_energy = energies[0];
        for (double e : energies) {
            if (e < this.min_energy)
                this.min_energy = e;
            if (e > this.max_energy)
                this.max_energy = e;
        }
        int number_of_captions = 0;
        for (int i = 0; i < energies.length; i++) {
            if (this.jmol.getCaption(i).length() > 0)
                ++number_of_captions;
        }
        this.captionIndices = new int[number_of_captions];
        number_of_captions = 0;
        for (int i = 0; i < energies.length; i++) {
            if (this.jmol.getCaption(i).length() > 0)
                this.captionIndices[number_of_captions++] = i;
        }
        timer.start();
    }

    public EnergyTreeView(RMapJmol jmol, File energyFile) throws IOException {
        this(jmol, readFileLines(energyFile));
    }

    private static double[] readFileLines(File file) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        BufferedReader capReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line = capReader.readLine();
        while (line != null) {
            lines.add(line);
            line = capReader.readLine();
        }
        double[] energies = new double[lines.size()];
        double base_energy = (double)Double.parseDouble(lines.get(0));
        for (int i = 0; i < energies.length; i++) {
            energies[i] = ((double)Double.parseDouble(lines.get(i)) - base_energy) * 262.549962;
        }
        return energies;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
