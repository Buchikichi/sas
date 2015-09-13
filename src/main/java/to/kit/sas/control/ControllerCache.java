package to.kit.sas.control;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Cache of controllers.
 * @author Hidetaka Sasai
 */
public final class ControllerCache {
	/** instance. */
	private static final ControllerCache ME = new ControllerCache();
	/** logger. */
	private static final Logger LOG = LogManager.getLogger();
	/** target suffix. */
	private static final String TARGET_SUFFIX = "controller";
	/** cache. */
	private Map<String, Class<?>> cache = new HashMap<>();

	private ControllerCache() {
		//
	}

	/**
	 * Get a instance.
	 * @return instance
	 */
	public static ControllerCache getInstance() {
		return ME;
	}

	/**
	 * initialize.
	 * @throws IOException I/O exception
	 */
	public void init() throws IOException {
		for (Class<?> clazz : new ControllerScanner().getControllerList()) {
			String simpleName = StringUtils.lowerCase(clazz.getSimpleName());
			String name = StringUtils.lowerCase(clazz.getName());
			int fromIndex = name.length() - simpleName.length();
			int beginIndex = name.lastIndexOf('.', fromIndex - 2);

			// simpleName
			this.cache.put(simpleName, clazz);
			// shortName
			if (beginIndex != -1) {
				name = name.substring(beginIndex + 1);
				this.cache.put(name, clazz);
			}
			LOG.debug(name);
		}
	}

	/**
	 * Get a controller.
	 * @param name name of controller
	 * @return controller
	 */
	public Controller<?> get(final String name) {
		Controller<?> controller = null;
		String controllerName = StringUtils.lowerCase(name);

		if (!controllerName.endsWith(TARGET_SUFFIX)) {
			controllerName += TARGET_SUFFIX;
		}
		if (this.cache.containsKey(controllerName)) {
			Class<?> clazz = this.cache.get(controllerName);

			if (Controller.class.isAssignableFrom(clazz)) {
				try {
					controller = (Controller<?>) clazz.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					// nop
				}
			}
		}
		return controller;
	}
}
