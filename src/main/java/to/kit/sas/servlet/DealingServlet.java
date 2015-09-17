package to.kit.sas.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.arnx.jsonic.JSON;
import ognl.Ognl;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import to.kit.sas.control.Controller;
import to.kit.sas.control.ControllerCache;
import to.kit.sas.util.RequestUtils;

/**
 * Dealing Servlet.
 * @author Hidetaka Sasai
 */
public final class DealingServlet extends HttpServlet {
	/** extension. */
	public static final String CONTROLLER_EXTENSION = ".cont";

	@SuppressWarnings("unchecked")
	private Controller<Object> getController(HttpServletRequest request) {
		String pathInfo = RequestUtils.inferPath(request);
		int endIndex = pathInfo.length() - CONTROLLER_EXTENSION.length();

		if (1 < endIndex) {
			String name = pathInfo.substring(0, endIndex);

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
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Controller<Object> controller = getController(request);
		Object result = null;

		if (controller != null) {
			Object parameter = getParameterObject(controller);

			fillParameter(parameter, request);
			result = controller.execute(parameter);
		}
		response.setCharacterEncoding(Charset.defaultCharset().toString());
		response.setContentType("application/json;charset=UTF-8");
		try (PrintWriter out = response.getWriter()) {
			if (result == null) {
				out.println("{}");
				return;
			}
			// JSON
			out.println(JSON.encode(result));
		}
	}

	/**
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		OgnlRuntime.setSecurityManager(null);
		String controllerRoot = getInitParameter("controllerRoot");
System.out.println("controllerRoot:" + controllerRoot);
		try {
			ControllerCache.getInstance().init(controllerRoot);
		} catch (IOException e) {
			// nop
		}
	}
}
