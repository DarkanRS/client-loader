package com.darkan;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Download {

	public void start() throws IOException {
		int update = updateExists();
		if (update == 0) {
			return;
		} else if (update == 2) {
			Loader.drawLoadingText(0, "Error checking for updates. Try the direct client.");
		} else {
			URL url = new URL(Loader.DOWNLOAD_URL);
			int length = getFileSize(url);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("content-type", "binary/data");
			InputStream in = conn.getInputStream();
			File dir = new File(Loader.CLIENT_PATH);
			if (!dir.exists())
				dir.mkdirs();
			FileOutputStream out = new FileOutputStream(Loader.CLIENT_PATH + File.separator + "darkanclient.jar");
			byte[] b = new byte[1024];
			int count;
			int down = 0;
			while ((count = in.read(b)) > 0) {
				out.write(b, 0, count);
				down += count;
				Loader.drawLoadingText(percentage(down, length), "Downloading Darkan - " + percentage(down, length) + "%");
			}
			out.close();
			in.close();
		}
	}

	private static int percentage(int current, int length) {
		return (current * 100) / length;
	}

	private int getFileSize(URL url) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.getInputStream();
			return conn.getContentLength();
		} catch (IOException e) {
			Loader.drawLoadingText(0, "Error checking for updates. Try the direct client.");
			return -1;
		} finally {
			conn.disconnect();
		}
	}

	public static int updateExists() {
		File file = new File(Loader.CLIENT_PATH + File.separator + "darkanclient.jar");
		if (!file.exists())
			return 1;

		String localCheck = getLocalChecksum();
		String remoteCheck = getRemoteChecksum();

		if (remoteCheck == null || localCheck == null)
			return 2;

		if (!remoteCheck.equalsIgnoreCase(localCheck))
			return 3;

		return 0;
	}

	public static String getLocalChecksum() {
		File local = new File(Loader.CLIENT_PATH + File.separator + "darkanclient.jar");
		try (FileInputStream fis = new FileInputStream(local)) {
			Loader.drawLoadingText(0, "Retrieving local checksum...");
			return calculateMd5(fis);
		} catch (Exception e) {
			Loader.drawLoadingText(0, "Error checking for updates. Try the direct client.");
		}
		return null;
	}

	public static String getRemoteChecksum() {
		try (InputStream stream = new URL(Loader.DOWNLOAD_URL).openStream()) {
			Loader.drawLoadingText(0, "Checking remote checksum...");
			return calculateMd5(stream);
		} catch (Exception e) {
			Loader.drawLoadingText(0, "Error checking for updates. Try the direct client.");
			return null;
		}
	}

	public static String calculateMd5(final InputStream instream) {
		return calculateDigest(instream, "MD5");
	}

	private static String calculateDigest(final InputStream instream, final String algorithm) {
		final byte[] buffer = new byte[4096];
		final MessageDigest messageDigest = getMessageDigest(algorithm);
		messageDigest.reset();
		int bytesRead;
		try {
			while ((bytesRead = instream.read(buffer)) != -1) {
				messageDigest.update(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			Loader.drawLoadingText(0, "Error checking for updates. Try the direct client.");
			System.err.println("Error making an '" + algorithm + "' digest on the inputstream");
		}
		return toHex(messageDigest.digest());
	}

	public static String toHex(final byte[] ba) {
		int baLen = ba.length;
		char[] hexchars = new char[baLen * 2];
		int cIdx = 0;
		for (int i = 0; i < baLen; ++i) {
			hexchars[cIdx++] = HEX_DIGIT[(ba[i] >> 4) & 0x0F];
			hexchars[cIdx++] = HEX_DIGIT[ba[i] & 0x0F];
		}
		return new String(hexchars);
	}

	public static MessageDigest getMessageDigest(final String algorithm) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			Loader.drawLoadingText(0, "Error checking for updates. Try the direct client.");
		}
		return messageDigest;
	}

	private static final char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
}