package example.threadstate.examples;

import com.google.common.base.Strings;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIDFilter implements Filter {

    private static final String CORRELATION_ID_KEY = "CorrelationID";

    private String headerName = "X-CorrelationID";

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        String correlationID = httpRequest.getHeader(headerName);
        if (Strings.isNullOrEmpty(correlationID)) {
            correlationID = UUID.randomUUID().toString();
        }

        MDC.put(CORRELATION_ID_KEY, correlationID);

        final HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader(headerName, correlationID);

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

}
