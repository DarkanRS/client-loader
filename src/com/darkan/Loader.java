package com.darkan;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Loader {

	private static JFrame frame;
	
	public static String CLIENT_PATH = System.getProperty("user.home") + File.separator + "sasukescapecache";
	public static String DOWNLOAD_URL = "http://game.sasukescape.com/assets/uploads/files/sasukescapeclient.jar";

	public static void main(String[] args) {
		try {
			frame = new JFrame();
            frame.setIconImage(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("com/darkan/icon.png")));
			frame.setSize(765, 553);
			frame.setTitle("SasukeScape");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			JPanel down = new JPanel();
			down.setLayout(null);
			down.setBackground(Color.black);
			down.setPreferredSize(new Dimension(765, 553));
			frame.add(down, BorderLayout.CENTER);
			frame.setVisible(true);
			frame.pack();
			drawLoadingText(0, "Checking for updates");
			new Download().start();
			frame.remove(down);
			Class<?> clazz = new RSPSLoader().getClass("Loader");
			Applet applet = (Applet) clazz.newInstance();
			frame.add(applet, BorderLayout.CENTER);
			applet.init();
			applet.setPreferredSize(new Dimension(765, 553));
			applet.setVisible(true);
			frame.pack();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void drawLoadingText(int i, String s) {
		Graphics graphics = frame.getContentPane().getGraphics();
		while (graphics == null) {
			graphics = frame.getContentPane().getGraphics();
			try {
				frame.getContentPane().repaint();
			} catch (Exception _ex) {
			}
			try {
				Thread.sleep(1000L);
			} catch (Exception _ex) {
			}
		}
		Font font = new Font("Helvetica", 1, 13);
		FontMetrics fontmetrics = frame.getContentPane().getFontMetrics(font);
		frame.getContentPane().getFontMetrics(new Font("Helvetica", 0, 13));

		graphics.setColor(Color.black);
		graphics.fillRect(0, 0, 553, 765);

		int j = 553 / 2 - 18;
		graphics.setColor(Color.red.darker());
		graphics.drawRect(767 / 2 - 152, j, 304, 34);
		graphics.fillRect(767 / 2 - 150, j + 2, i * 3, 30);
		graphics.setColor(Color.black);
		graphics.fillRect((767 / 2 - 150) + i * 3, j + 2, 300 - i * 3, 30);
		graphics.setFont(font);
		graphics.setColor(Color.white);
		graphics.drawString(s, (767 - fontmetrics.stringWidth(s)) / 2, j + 22);
	}

}