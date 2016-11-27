package example.threadstate.examples;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static example.threadstate.core.concurrent.CompletableFutures.withUnwrappedException;

@Component
@Path("/")
public class HelloResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloResource.class);

    @Autowired
    private ExecutorService executor;

    @Autowired
    @Qualifier("asyncTaskExecutor")
    private ExecutorService asyncExecutor;

    @Autowired
    private PublisherService publisherService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public void home(@Suspended AsyncResponse asyncResponse) {
        executor.submit(() -> {
            LOGGER.info("Sending response in other thread...");
            LOGGER.info("Request content: {}", RequestContextHolder.getRequestAttributes());

            CompletableFuture.runAsync(publisherService::publish, asyncExecutor)
                .exceptionally(withUnwrappedException(e -> {
                    LOGGER.error("Publish failed", e);
                    return null;
                }));

            asyncResponse.resume("Home");
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
