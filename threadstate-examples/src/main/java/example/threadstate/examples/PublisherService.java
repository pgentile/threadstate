package example.threadstate.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class PublisherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublisherService.class);

    public void publish() {
        LOGGER.info("Publish...");

        final String principalName = SecurityContextHolder.getContext().getAuthentication().getName();
        LOGGER.info("Principal is {}", principalName);
    }

}
