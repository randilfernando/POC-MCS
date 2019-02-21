package com.alternate.ollcs.mongo.services;

import com.alternate.ollcs.mongo.models.EventWrapper;
import org.bson.BsonDocument;
import reactor.core.publisher.Flux;

public interface MongoEventsStreamConnector {

    Flux<EventWrapper> subscribe(BsonDocument token);
}
