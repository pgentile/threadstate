package example.threadstate.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PublisherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublisherService.class);

    public void publish() {
        LOGGER.info("Publish...");
        throw new RuntimeException("Publish failed");
    }

}
