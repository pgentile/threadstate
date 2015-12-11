package example.threadstate.examples;

import example.threadstate.core.concurrent.CompletableFutures;
import example.threadstate.core.retry.AsyncRetryExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ExecutorService;

@Component
@Path("/")
public class HelloResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloResource.class);

    @Autowired
    @Qualifier("defaultExecutor")
    private ExecutorService executor;

    @Autowired
    private AsyncRetryExecutor asyncRetryExecutor;

    @Autowired
    private PublisherService publisherService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public void home(@Suspended AsyncResponse asyncResponse) {
        executor.submit(() -> {
            LOGGER.info("Sending response in other thread...");
            LOGGER.info("Request content: {}", RequestContextHolder.getRequestAttributes());
            asyncResponse.resume("Home");
        });

        asyncRetryExecutor.submit(publisherService::publish)
            .whenComplete((ignored, e) -> {
                if (e != null) {
                    LOGGER.error("Publish failed", CompletableFutures.unwrapException(e));
                }
            });

        LOGGER.info("Detached!");
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello!";
    }

}
