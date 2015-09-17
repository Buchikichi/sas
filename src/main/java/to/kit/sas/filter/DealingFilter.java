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

public final class DealingFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Nothing to do.
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		String pathInfo = RequestUtils.inferPath(request);

		if (ControllerCache.getInstance().contains(pathInfo)) {
			String path = pathInfo + DealingServlet.CONTROLLER_EXTENSION;
			request.getRequestDispatcher(path).forward(request, response);
			return;
		}
		System.out.println(request);
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// Nothing to do.
	}
}
