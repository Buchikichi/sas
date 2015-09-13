package to.kit.sas.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.arnx.jsonic.JSON;
import ognl.Ognl;
import ognl.OgnlException;
import to.kit.sas.control.Controller;
import to.kit.sas.control.ControllerCache;

/**
 * Dealing Servlet.
 * @author Hidetaka Sasai
 */
@WebServlet(urlPatterns = { "/*" }, loadOnStartup = 1)
public final class DealingServlet extends HttpServlet {
	/** logger. */
	private static final Logger LOG = LogManager.getLogger();

	@SuppressWarnings("unchecked")
	private Controller<Object> getController(HttpServletRequest request) {
		String pathInfo = request.getPathInfo();

		LOG.debug("path[{}]", pathInfo);
		if (1 < pathInfo.length()) {
			String name = pathInfo.substring(1);

			return (Controller<Object>) ControllerCache.getInstance().get(name);
		}
		return null;
	}

	private Object getParameterObject(Controller<?> controller) {
		Object obj = null;
		Class<?> clazz = controller.getClass();

		for (Method method : clazz.getMethods()) {
			if (method.getDeclaringClass() == Object.class) {
				continue;
			}
			if (!"execute".equals(method.getName())) {
				continue;
			}
			if (method.getParameterTypes().length != 1) {
				continue;
			}
			Class<?> type = method.getParameterTypes()[0];
			if (type == Object.class) {
				continue;
			}
			LOG.info("form[{}]", type.getName());
			try {
				obj = type.newInstance();
			} catch (SecurityException | InstantiationException | IllegalAccessException e) {
				// nop
			}
			break;
		}
		return obj;
	}

	private void fillParameter(Object object, HttpServletRequest request) {
		if (object == null) {
			return;
		}
		for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue()[0];
			try {
				Ognl.setValue(name, object, value);
			} catch (OgnlException e) {
				// nop
			}
		}
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		Controller<Object> controller = getController(request);
		Object result = null;

		if (controller != null) {
			Object parameter = getParameterObject(controller);

			fillParameter(parameter, request);
			result = controller.execute(parameter);
		}
		try (PrintWriter out = resp.getWriter()) {
			if (result == null) {
				out.println("{}");
				return;
			}
			out.println(JSON.encode(result));
		}
	}

	/**
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		LOG.info("init");
		try {
			ControllerCache.getInstance().init();
		} catch (IOException e) {
			LOG.error(ExceptionUtils.getStackTrace(e));
		}
	}
}
