package to.kit.sas.util;

import java.util.Arrays;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * The Utility for request.
 * @author Hidetaka Sasai
 */
public final class RequestUtils {
	private RequestUtils() {
		//
	}

	/**
	 * Infer path info from request.
	 * @param request request
	 * @return path
	 */
	public static PathInfo inferPath(final ServletRequest request) {
		PathInfo pathInfo = null;

		if (request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest) request;
			String uri = req.getPathInfo();

			if (uri == null || uri.isEmpty()) {
				String contextPath = req.getContextPath();
				int beginIndex = contextPath.length();

				uri = req.getRequestURI().substring(beginIndex);
			}
			pathInfo = new PathInfo(uri);
		}
		return pathInfo;
	}

	/**
	 * The Information of request path.
	 * @author Hidetaka Sasai
	 */
	public static class PathInfo {
		private final String uri;
		private final String resource;
		private final String method;
		private final String[] params;

		/**
		 * @return the URI
		 */
		public String getUri() {
			return this.uri;
		}
		/**
		 * @return the resource
		 */
		public String getResource() {
			return this.resource;
		}
		/**
		 * @return the method
		 */
		public String getMethod() {
			return this.method;
		}
		/**
		 * @return the params
		 */
		public String[] getParams() {
			return this.params;
		}

		/**
		 * Create a instance.
		 * @param uri the URI
		 */
		PathInfo(String uri) {
			String[] args = uri.substring(1).split("/");

			this.uri = uri;
			this.resource = args[0];
			if (1 < args.length) {
				this.method = args[1];
			} else {
				this.method = "";
			}
			if (2 < args.length) {
				this.params = Arrays.copyOfRange(args, 2, args.length);
			} else {
				this.params = new String[0];
			}
		}
	}
}
