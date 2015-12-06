package example.threadstate.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

@Component
public class RequestInfoFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestInfoFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info("IN: {} {}", requestContext.getMethod(), requestContext.getUriInfo().getRequestUri());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        LOGGER.info("OUT: {} {}", requestContext.getMethod(), requestContext.getUriInfo().getRequestUri());
    }

}
