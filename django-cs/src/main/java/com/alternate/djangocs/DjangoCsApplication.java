package com.alternate.djangocs;

import com.alternate.djangocs.mongo.services.MongoEventsStreamConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class DjangoCsApplication {

    private MongoEventsStreamConnector mongoEventsStreamConnector;

    @Autowired
    public DjangoCsApplication(MongoEventsStreamConnector mongoEventsStreamConnector) {

        this.mongoEventsStreamConnector = mongoEventsStreamConnector;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        this.mongoEventsStreamConnector.init();
    }

    public static void main(String[] args) {
        SpringApplication.run(DjangoCsApplication.class, args);
    }

}
