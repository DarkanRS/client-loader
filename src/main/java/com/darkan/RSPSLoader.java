package com.darkan;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public final class RSPSLoader {
	private ClassLoader classLoader;
	private final Map<String, Class<?>> classMap;

	public RSPSLoader() throws Exception {
		classMap = new HashMap<>();
		final URL url = new File(Loader.CLIENT_PATH + File.separator + "darkanclient.jar").toURI().toURL();
		classLoader = new URLClassLoader(new URL[] { url });
	}

	public Class<?> getClass(final String name) {
		if (classMap.containsKey(name))
			return classMap.get(name);
		try {
			final Class<?> clazz = classLoader.loadClass(name);
			classMap.put(name, clazz);
			return clazz;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}