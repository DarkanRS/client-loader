package com.darkan;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class Loader {

	public enum Lobby {
		OFFICIAL("Official", "prod.darkan.org"),
		TEST("Community", "test.darkan.org"),
		DEV("Development", "dev.darkan.org");

		private String name, ip;

		Lobby(String name, String ip) {
			this.name = name;
			this.ip = ip;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static JFrame frame;
	private static Applet applet;

	private static final Dimension FIXED_SIZE = new Dimension(774, 588);
	public static String CLIENT_PATH = System.getProperty("user.home") + File.separator + ".darkanrs";
	public static String DOWNLOAD_URL = "https://donate.darkan.org/client.jar";
	private static BufferedImage BACKGROUND, LOGO;

	public static void main(String[] args) {
		try {
			try {
				UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			} catch (Exception e) {
				System.out.println("Look and Feel not set");
			}
			try {
				BACKGROUND = ImageIO.read(Loader.class.getResource("/bg.png"));
				LOGO = ImageIO.read(Loader.class.getResource("/logo.png"));
			} catch(Exception e) {}

			frame = new JFrame();

			try {
				frame.setIconImage(new ImageIcon(Loader.class.getResource("/icon.png")).getImage());
			} catch (Exception e) {
			}
			frame.setSize(FIXED_SIZE.width, FIXED_SIZE.height);
			frame.setMinimumSize(FIXED_SIZE);
			frame.setPreferredSize(FIXED_SIZE);
			frame.setTitle("Darkan");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setResizable(false);

			try {
				checkForUpdates();
			} catch (IOException e) {
				e.printStackTrace();
				drawLoadingText(0, "Error checking for updates: " + e.getMessage());
				while(true);
			}

			promptServer();
		} catch (Exception e) {
			e.printStackTrace();
			drawLoadingText(0, "Exception starting client: " + e.getMessage());
		}
	}

	public static void checkForUpdates() throws IOException {
		ClientPanel down = new ClientPanel(BACKGROUND, LOGO);
		down.setLayout(new BorderLayout());
		down.setBackground(Color.black);
		down.setBackground(Color.black);
		down.setMinimumSize(FIXED_SIZE);
		down.setPreferredSize(FIXED_SIZE);
		frame.add(down, BorderLayout.CENTER);
		frame.setVisible(true);
		frame.pack();
		drawLoadingText(0, "Checking for updates");
		Download download = new Download();
		download.start();
		frame.remove(down);
	}

	public static void promptServer() throws Exception {
		Class<?> clazz = new RSPSLoader().getClass("com.Loader");
		applet = (Applet) clazz.newInstance();

		ClientPanel serverSelect = new ClientPanel(BACKGROUND, LOGO);
		serverSelect.setLayout(null);
		serverSelect.setBackground(Color.black);
		serverSelect.setMinimumSize(FIXED_SIZE);
		serverSelect.setPreferredSize(FIXED_SIZE);

		int height = frame.getHeight() / 2 - 25;
		JLabel select = new JLabel("Select a lobby server to play on.", SwingConstants.CENTER);
		select.setFont(new Font("Arial", Font.BOLD, 24));
		select.setForeground(Color.lightGray);
		select.setBounds(0, height - 75, frame.getWidth(), 50);
		serverSelect.add(select);

		JLabel description = new JLabel("Note: Your account is not shared between lobby servers.", SwingConstants.CENTER);
		description.setFont(new Font("Arial", Font.ITALIC, 16));
		description.setForeground(Color.lightGray);
		description.setBounds(0, height - 50, frame.getWidth(), 50);
		serverSelect.add(description);

		JComboBox<Lobby> lobbyBox = new JComboBox<>(Lobby.values());
		DefaultListCellRenderer listRenderer = new DefaultListCellRenderer();
		listRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
		lobbyBox.setRenderer(listRenderer);
		lobbyBox.setBounds(frame.getWidth() / 2 - 90 - 100, height + 13, 180, 25);
		serverSelect.add(lobbyBox);

		JButton lobbyButton = new JButton("Play");
		lobbyButton.setBounds(FIXED_SIZE.width/2 + 10, height, 180, 50);
		serverSelect.add(lobbyButton);

		lobbyButton.addActionListener(event -> {
			try {
				Field lobbyIpField = clazz.getDeclaredField("IP_ADDRESS");
				lobbyIpField.setAccessible(true);
				lobbyIpField.set(null, lobbyBox.getItemAt(lobbyBox.getSelectedIndex()).ip);
				frame.remove(serverSelect);
				frame.setResizable(true);
				initApplet();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		frame.add(serverSelect, BorderLayout.CENTER);
		frame.setVisible(true);
		frame.pack();
	}

	public static void initApplet() {
		frame.add(applet, BorderLayout.CENTER);
		applet.init();
		applet.setPreferredSize(FIXED_SIZE);
		applet.setVisible(true);
		frame.pack();
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
		graphics.fillRect(0, 0, FIXED_SIZE.width, FIXED_SIZE.height);

		int j = FIXED_SIZE.height / 2 - 18;
		graphics.setColor(Color.red.darker());
		graphics.drawRect(FIXED_SIZE.width / 2 - 152, j, 304, 34);
		graphics.fillRect(FIXED_SIZE.width / 2 - 150, j + 2, i * 3, 30);
		graphics.setColor(Color.black);
		graphics.fillRect((FIXED_SIZE.width / 2 - 150) + i * 3, j + 2, 300 - i * 3, 30);
		graphics.setFont(font);
		graphics.setColor(Color.white);
		graphics.drawString(s, (FIXED_SIZE.width - fontmetrics.stringWidth(s)) / 2, j + 22);
	}

}