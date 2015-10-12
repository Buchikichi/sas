package to.kit.sas.control;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Scanning coltrollers.
 * @author Hidetaka Sasai
 */
public final class ControllerScanner {
	/** File name of target. */
	private static final String TARGET_NAME = "Controller.class";
	/** Directory of classes. */
	private static final String CLASSES_ROOT = ".classes.";

	private File getClassRoot(final String controllerRoot) throws IOException {
		File root = null;
		String name = controllerRoot;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		name = name.replace(".", "/");
		name = "./" + name;
System.out.println("name:" + name);
		for (URL url : Collections.list(loader.getResources(name))) {
System.out.println("url:" + url);
			String protocol = url.getProtocol();

			if (!"file".equals(protocol)) {
				continue;
			}
			try {
				root = new File(url.toURI());
			} catch (@SuppressWarnings("unused") URISyntaxException e) {
				//
			}
		}
		return root;
	}

	private void crawl(List<File> list, File origin) {
		for (File file : origin.listFiles()) {
			if (file.isDirectory()) {
				crawl(list, file);
				continue;
			}
			String name = file.getName();
			if (!name.endsWith(TARGET_NAME)) {
				continue;
			}
			list.add(file);
		}
	}

	private Class<?> getClazz(File file) {
		Class<?> clazz = null;
		String name = file.getAbsolutePath().replace(File.separator, ".");
		int beginIndex = name.lastIndexOf(CLASSES_ROOT);
		String className = name.substring(beginIndex + CLASSES_ROOT.length());
		int lastIndex = className.lastIndexOf('.');

		className = className.substring(0, lastIndex);
System.out.println("className:" + className);
		try {
			clazz = Class.forName(className);
		} catch (@SuppressWarnings("unused") ClassNotFoundException e) {
			// nop
		}
		return clazz;
	}

	/**
	 * List controllers.
	 * @param controllerRoot Root package of controller
	 * @return controllers List of controllers
	 * @throws IOException I/O exception
	 */
	public List<Class<?>> getControllerList(String controllerRoot) throws IOException {
		List<Class<?>> list = new ArrayList<>();
		File root = getClassRoot(controllerRoot);
		List<File> pathList = new ArrayList<>();

		if (root == null) {
			return list;
		}
		crawl(pathList, root);
		for (File file : pathList) {
System.out.println("file[" + file.getAbsolutePath() + "]");
			Class<?> clazz = getClazz(file);

			list.add(clazz);
		}
		return list;
	}
}
