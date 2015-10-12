package to.kit.sas.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import to.kit.sas.control.ControllerCache;
import to.kit.sas.servlet.DealingServlet;
import to.kit.sas.util.RequestUtils;
import to.kit.sas.util.RequestUtils.PathInfo;

/**
 * Dealing Filter.
 * @author Hidetaka Sasai
 */
public final class DealingFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Nothing to do.
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		PathInfo pathInfo = RequestUtils.inferPath(request);
		String resource = pathInfo.getResource();

		if (ControllerCache.getInstance().contains(resource)) {
			String uri = pathInfo.getUri();

			uri = DealingServlet.DEALING_PREFIX + uri;
			request.getRequestDispatcher(uri).forward(request, response);
			return;
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// Nothing to do.
	}
}
