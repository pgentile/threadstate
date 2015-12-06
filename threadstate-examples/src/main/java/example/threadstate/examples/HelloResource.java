package example.threadstate.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private ExecutorService executor;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public void home(@Suspended AsyncResponse asyncResponse) {
        executor.submit(() -> {
            LOGGER.info("Sending response in other thread...");
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
