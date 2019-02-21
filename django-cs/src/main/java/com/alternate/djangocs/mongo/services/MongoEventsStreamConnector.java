package com.alternate.djangocs.mongo.services;

import com.alternate.djangocs.mongo.models.EventWrapper;
import org.bson.BsonDocument;
import reactor.core.publisher.Flux;

public interface MongoEventsStreamConnector {

    Flux<EventWrapper> subscribe(BsonDocument token);
}
