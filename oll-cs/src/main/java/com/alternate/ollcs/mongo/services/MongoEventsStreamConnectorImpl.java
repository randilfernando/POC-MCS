package com.alternate.ollcs.mongo.services;

import com.alternate.ollcs.mongo.models.Event;
import com.alternate.ollcs.mongo.models.EventWrapper;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class MongoEventsStreamConnectorImpl implements MongoEventsStreamConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoEventsStreamConnectorImpl.class);

    private MongoDatabase mongoDatabase;

    @Autowired
    public MongoEventsStreamConnectorImpl(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    public Flux<EventWrapper> subscribe(BsonDocument token) {
        List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.in("operationType", Arrays.asList("insert", "update", "replace"))),
                Aggregates.match(Filters.in("fullDocument.event_name", Arrays.asList("AddNewField", "EditField", "DeleteField",
                        "UserContractSelection", "AddExample", "ImplicitSearch", "ExplicitSearch"))),
                Aggregates.match(Filters.eq("fullDocument.processed", false))
        );

        return Flux.create(s -> {
            LOGGER.info("mongo change stream listener started");

            ChangeStreamIterable<Document> iterable = this.mongoDatabase.getCollection("events")
                    .watch(pipeline)
                    .fullDocument(FullDocument.UPDATE_LOOKUP);

            if (token != null) {
                iterable = iterable.resumeAfter(token);
            }

            iterable.forEach((Consumer<? super ChangeStreamDocument<Document>>) d -> {
                EventWrapper eventWrapper = this.processDocument(d);
                s.next(eventWrapper);
            });
        });
    }

    private EventWrapper processDocument(ChangeStreamDocument<Document> document) {
        String id = document.getDocumentKey().get("_id").asObjectId().getValue().toString();
        Map<String, Object> payload = document.getFullDocument();

        Event event = new Event()
                .withId(id)
                .withEventName((String) payload.get("event_name"))
                .withUser((String) payload.get("user"))
                .withEventReference((String) payload.get("event_reference"))
                .withEventTime((String) payload.get("event_time"))
                .withProcessed((Boolean) payload.get("processed"));

        return new EventWrapper()
                .withEvent(event)
                .withResumeToken(document.getResumeToken());
    }
}
