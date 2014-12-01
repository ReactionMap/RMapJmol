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
    private double min_energy;
    private double max_energy;
    private RMapJmol jmol;
    private Timer timer = new Timer(100, this);

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        int frame = jmol.getFrame();
        double scale_x = (double)(getWidth() - 60) / (energies.length+1);
        double scale_y = (double)(getHeight() - 80) / (max_energy - min_energy);
        int offset_x = 50;
        int offset_y = 40;
        ((Graphics2D)graphics).setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 1; i < energies.length; i++) {
            double e1 = max_energy - energies[i-1];
            double e2 = max_energy - energies[i];
            int x1 = (int) (i * scale_x) + offset_x;
            int y1 = (int) (e1 * scale_y) + offset_y;
            int x2 = (int) ((i+1) * scale_x) + offset_x;
            int y2 = (int) (e2 * scale_y) + offset_y;
            graphics.setColor(Color.BLACK);
            graphics.drawLine(x1, y1, x2, y2);
        }
        ((Graphics2D)graphics).setStroke(new BasicStroke(1));
        int base_x = (int) scale_x + offset_x;
        int base_y = (int)((max_energy - min_energy) * scale_y) + offset_y;
        graphics.setColor(Color.BLACK);
        graphics.drawLine(base_x, offset_y, base_x, base_y);
        graphics.drawLine(base_x, (int)(max_energy*scale_y)+offset_y, getWidth()-10, (int)(max_energy*scale_y)+offset_y);
        graphics.setColor(Color.RED);
        graphics.drawLine((int)(frame*scale_x)+offset_x, 0, (int)(frame*scale_x)+offset_x, getHeight()-18);
        FontMetrics fontMetrics = graphics.getFontMetrics();
        graphics.setColor(Color.BLACK);
        for (int i = 1; i <= energies.length; i++) {
            String label = jmol.getCaption(i);
            if (label.length() > 0) {
                int width = fontMetrics.stringWidth(label);
                graphics.drawString(label, (int)(i*scale_x)+offset_x-width/2, getHeight()-5);
                double e = energies[i-1];
                int e10 = (int)(e * 2625.49962);
                String eStr = ""+e10/10+"."+e10%10;
                int w = fontMetrics.stringWidth(eStr);
                graphics.drawString(eStr, 40-w, (int)((max_energy-e)*scale_y)+offset_y+fontMetrics.getAscent()/2);
            }
        }

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
            energies[i] = (double)Double.parseDouble(lines.get(i)) - base_energy;
        }
        return energies;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
