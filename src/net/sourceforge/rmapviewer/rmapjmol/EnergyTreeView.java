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
        int frame = jmol.getFrame()-1;
        double scale_x = (double)(getWidth() - 100) / (captionIndices.length-1);
        double scale_y = (double)(getHeight() - 80) / (max_energy - min_energy);
        int offset_x = 50;
        int offset_y = 40;
        ((Graphics2D)graphics).setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 1; i < energies.length; i++) {
            double e1 = max_energy - energies[i-1];
            double e2 = max_energy - energies[i];
            int x1 = (int) (framePosition(i-1) * scale_x) + offset_x;
            int y1 = (int) (e1 * scale_y) + offset_y;
            int x2 = (int) (framePosition(i) * scale_x) + offset_x;
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
            String eStr = ""+Math.round(e*10)/10+"."+Math.round(e*10)%10;;
            graphics.drawString(eStr, base_x - fontMetrics.stringWidth(eStr) - 2, y + fontMetrics.getAscent()/2);
        }
        for (int i = 0; i < captionIndices.length; i++) {
            int x = (int)(i*scale_x + offset_x);
            String caption = jmol.getCaption(captionIndices[i]+1);
            graphics.setColor(Color.BLACK);
            graphics.drawString(caption, x-fontMetrics.stringWidth(caption)/2, base_y + fontMetrics.getHeight()+2);
            if (i > 0) {
                double e = energies[captionIndices[i]];
                String eStr = ""+Math.round(e*10)/10+"."+Math.round(e*10)%10;
                graphics.drawString(eStr, x-fontMetrics.stringWidth(eStr)/2, (int)((max_energy-e)*scale_y+offset_y)-2);
                graphics.setColor(Color.LIGHT_GRAY);
                graphics.drawLine(x, offset_y, x, base_y);
            }
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
            if (this.jmol.getCaption(i+1).length() > 0)
                ++number_of_captions;
        }
        this.captionIndices = new int[number_of_captions];
        System.out.println(number_of_captions);
        number_of_captions = 0;
        for (int i = 0; i < energies.length; i++) {
            if (this.jmol.getCaption(i+1).length() > 0)
                this.captionIndices[number_of_captions++] = i;
        }
        timer.start();
    }

    public Component createEnergyTable() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        String[] columnNames = { "name", "Energy (kJ/mol)"};
        String[] captions = jmol.getCaptions();
        final String[][] rowData;
        int number_of_captions = 0;
        for (int i = 0; i < captions.length; i++) {
            if (!captions[i].isEmpty())
                number_of_captions++;
        }
        int rowIndex = 0;
        rowData = new String[number_of_captions][];
        for (int i = 0; i < captions.length; i++) {
            if (!captions[i].isEmpty()) {
                String[] row = new String[2];
                row[0] = captions[i];
                row[1] = ""+energies[i];
                rowData[rowIndex++] = row;
            }
        }
        JTable table = new JTable(rowData, columnNames);
        table.setGridColor(Color.BLACK);
        table.setShowGrid(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(200, 300));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    String[] ext = {"*.csv", "*"};
                    final FileDialog dialog = new FileDialog(new Frame(),"Save energy table",FileDialog.SAVE);
                    dialog.setVisible(true);
                    final String fullpath = dialog.getDirectory() + dialog.getFile();
                    dialog.dispose();
                    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(fullpath))));
                    for (String[] row : rowData) {
                        pw.println("\""+row[0]+"\" , "+row[1]);
                    }
                    pw.close();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(panel, "Can't write a file", "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(saveButton, BorderLayout.SOUTH);
        return panel;
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
            energies[i] = ((double)Double.parseDouble(lines.get(i)) - base_energy) * 2625.49962;
        }
        return energies;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
