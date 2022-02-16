package com.darkan;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class ClientPanel extends JPanel {
    private BufferedImage bg, logo;

    public ClientPanel(BufferedImage bg, BufferedImage logo) {
        this.bg = bg;
        this.logo = logo;
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (bg != null)
            g.drawImage(bg, (getWidth() - bg.getWidth()) / 2, (getHeight() - bg.getHeight()) / 2, null);
        if (logo != null)
            g.drawImage(logo, (getWidth() - logo.getWidth()) / 2, 40, null);
    }
}
