package to.kit.sas.control;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Scanning coltrollers.
 * @author Hidetaka Sasai
 */
public final class ControllerScanner {
	/** File name of target. */
	private static final String TARGET_NAME = "Controller.class";

	private Path getClassRoot() throws IOException {
		Path path = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		for (URL url : Collections.list(loader.getResources("./"))) {
			String protocol = url.getProtocol();

			if (!"file".equals(protocol)) {
				continue;
			}
			File current = new File(url.getPath());
			String name = current.getName();
			if ("classes".equals(name)) {
				path = current.toPath();
				break;
			}
		}
		return path;
	}

	/**
	 * List controllers.
	 * @return controllers
	 * @throws IOException I/O exception
	 */
	public List<Class<?>> getControllerList() throws IOException {
		final List<Class<?>> list = new ArrayList<>();
		final Path root = getClassRoot();

		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				String name = file.getFileName().toString();

				if (name.endsWith(TARGET_NAME)) {
					String className = StringUtils.join(root.relativize(file), ".");
					int ix = className.lastIndexOf('.');

					className = className.substring(0, ix);
					try {
						Class<?> clazz = Class.forName(className);

						list.add(clazz);
					} catch (ClassNotFoundException e) {
						// nop
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
		return list;
	}
}
