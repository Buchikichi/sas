package to.kit.sas.util;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public final class RequestUtils {
	private RequestUtils() {
		//
	}

	/**
	 * Infer path info from request.
	 * @param request request
	 * @return path
	 */
	public static String inferPath(final ServletRequest request) {
		String path = null;

		if (request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest) request;

			path = req.getPathInfo();
			if (StringUtils.isBlank(path)) {
				String contextPath = StringUtils.defaultString(req.getContextPath());
				int beginIndex = contextPath.length() + 1;
				String uri = req.getRequestURI();

				path = uri.substring(beginIndex);
			}
		}
		return path;
	}
}
