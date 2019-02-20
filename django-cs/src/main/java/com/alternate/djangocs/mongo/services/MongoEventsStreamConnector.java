package com.alternate.djangocs.mongo.services;

import com.alternate.djangocs.mongo.models.Event;
import reactor.core.publisher.Flux;

public interface MongoEventsStreamConnector {
    void init();
    Flux<Event> connect();
}
