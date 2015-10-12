package to.kit.sas.servlet;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.Map;

import javax.imageio.ImageIO;
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
import to.kit.sas.util.RequestUtils.PathInfo;

/**
 * Dealing Servlet.
 * @author Hidetaka Sasai
 */
public final class DealingServlet extends HttpServlet {
	/** extension. */
	public static final String DEALING_PREFIX = "/DEAL";
	/** A default method name. */
	public static final String DEFAULT_METHOD = "execute";

	private Object[] fillParameter(Class<?> type, PathInfo pathInfo, HttpServletRequest request) {
		if (type == null) {
			return null;
		}
		if (String.class.isAssignableFrom(type)) {
			return pathInfo.getParams();
		}
		Object[] args;
		Object obj;

		try {
			obj = type.newInstance();
			args = new Object[] { obj };
		} catch (@SuppressWarnings("unused") InstantiationException | IllegalAccessException e) {
			return null;
		}
		for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue()[0];
			try {
				Ognl.setValue(name, obj, value);
			} catch (@SuppressWarnings("unused") OgnlException e) {
				// nop
			}
		}
		return args;
	}

	private Object invokeController(Controller<Object> controller, PathInfo pathInfo, HttpServletRequest request) {
		String methodName = pathInfo.getMethod();
		Class<?> clazz = controller.getClass();
		Method method = null;
		Class<?> type = null;

		if (methodName.isEmpty()) {
			methodName = DEFAULT_METHOD;
		}
		for (Method targetMethod : clazz.getDeclaredMethods()) {
			if (targetMethod.getDeclaringClass() == Object.class) {
				continue;
			}
			int numOfParams = targetMethod.getParameterTypes().length;
			if (1 < numOfParams) {
				continue;
			}
			if (!targetMethod.getName().equals(methodName)) {
				continue;
			}
			int mod = targetMethod.getModifiers();
			if (Modifier.isVolatile(mod)) {
				continue;
			}
			if (0 < numOfParams) {
				type = targetMethod.getParameterTypes()[0];
			}
			method = targetMethod;
			break;
		}
		if (method == null) {
			return null;
		}
		Object[] args = fillParameter(type, pathInfo, request);
		Object result = null;
		try {
			result = method.invoke(controller, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// nop
			e.printStackTrace();
		}
		return result;
	}

	private void responseImage(HttpServletResponse response, RenderedImage img) throws IOException {
		response.setContentType("image/png");
		try (OutputStream output = response.getOutputStream()) {
			ImageIO.write(img, "PNG", output);
		}
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PathInfo pathInfo = RequestUtils.inferPath(request);
		String resource = pathInfo.getResource();
		Controller<Object> controller = (Controller<Object>) ControllerCache.getInstance().get(resource);
		Object result = null;

		if (controller != null) {
			result = invokeController(controller, pathInfo, request);
		}
		if (result instanceof RenderedImage) {
			responseImage(response, (RenderedImage) result);
			return;
		}
		response.setCharacterEncoding(Charset.defaultCharset().toString());
		response.setContentType("application/json;charset=UTF-8");
		try (PrintWriter out = response.getWriter()) {
			if (result == null) {
				out.println("{}");
				return;
			}
			if (result instanceof String) {
				out.println(result);
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

		if (controllerRoot == null) {
			controllerRoot = "";
		}
		System.out.println("controllerRoot:" + controllerRoot);
		try {
			ControllerCache.getInstance().init(controllerRoot);
		} catch (@SuppressWarnings("unused") IOException e) {
			// nop
		}
	}
}
