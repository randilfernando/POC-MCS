package com.alternate.djangocs.mongo.models;

import org.bson.BsonDocument;

public class EventWrapper {
    private Event event;
    private BsonDocument resumeToken;

    public Event getEvent() {
        return event;
    }

    public BsonDocument getResumeToken() {
        return resumeToken;
    }

    public EventWrapper withEvent(Event event) {
        this.event = event;
        return this;
    }

    public EventWrapper withResumeToken(BsonDocument resumeToken) {
        this.resumeToken = resumeToken;
        return this;
    }
}
