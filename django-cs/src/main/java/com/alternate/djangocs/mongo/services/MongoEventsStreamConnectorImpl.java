package com.alternate.djangocs.mongo.services;

import com.alternate.djangocs.mongo.models.Event;
import com.alternate.djangocs.common.util.Executors2;
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
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

@Service
public class MongoEventsStreamConnectorImpl implements MongoEventsStreamConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoEventsStreamConnectorImpl.class);

    private Flux<Event> eventStream;
    private FluxSink<Event> eventSink;

    private MongoDatabase mongoDatabase;
    private ResumeTokenService resumeTokenService;

    @Autowired
    public MongoEventsStreamConnectorImpl(MongoDatabase mongoDatabase, ResumeTokenService resumeTokenService) {
        this.mongoDatabase = mongoDatabase;
        this.resumeTokenService = resumeTokenService;
    }

    @Override
    public void init() {
        final DirectProcessor<Event> directProcessor = DirectProcessor.create();
        this.eventStream = directProcessor.onBackpressureBuffer();
        this.eventSink = directProcessor.sink();

        this.initChangeStreamListener();
    }

    @Override
    public Flux<Event> connect() {
        return this.eventStream;
    }

    private void initChangeStreamListener() {
        String[] operationTypes = new String[]{"insert", "update", "replace"};
        String[] eventNames = new String[]{"AddNewField", "EditField", "DeleteField", "UserContractSelection",
                "AddExample", "ImplicitSearch", "ExplicitSearch"};

        List<Bson> pipeline = Arrays.asList(
                Aggregates.match(Filters.in("operationType", operationTypes)),
                Aggregates.match(Filters.all("event_name", eventNames)),
                Aggregates.match(Filters.eq("processed", true))
        );

        Executors2.newRetrySingleThreadExecutor(5).submit(() -> {
            LOGGER.info("mongo change stream listener started");

            ChangeStreamIterable<Document> iterable = this.mongoDatabase
                    .watch(pipeline)
                    .fullDocument(FullDocument.UPDATE_LOOKUP);

            try {
                BsonDocument resumeToken = this.resumeTokenService.getToken();

                if (resumeToken != null) {
                    iterable = iterable.resumeAfter(resumeToken);
                }

                iterable.forEach((Consumer<? super ChangeStreamDocument<Document>>) this::processDocument);
            } catch (IOException e) {
                throw new CancellationException();
            }
        });
    }

    private void processDocument(ChangeStreamDocument<Document> document) {
        Map<String, Object> payload = document.getFullDocument();
        String id = document.getDocumentKey().get("_id").asObjectId().getValue().toString();
        payload.put("_id", id);

        Event event = new Event()
                .withEventName((String) payload.get("event_name"))
                .withUser((String) payload.get("user"))
                .withEventReference((String) payload.get("event_reference"))
                .withEventTime((String) payload.get("event_time"))
                .withProcessed((Boolean) payload.get("processed"));

        LOGGER.info("message published");
        this.eventSink.next(event);
        BsonDocument token = document.getResumeToken();

        try {
            this.resumeTokenService.updateToken(token);
        } catch (IOException e) {
            throw new CancellationException();
        }
    }
}
