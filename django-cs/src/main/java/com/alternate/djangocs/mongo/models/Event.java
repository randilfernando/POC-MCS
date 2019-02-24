package com.alternate.djangocs.mongo.models;

public class Event {
    private String id;
    private String eventName;
    private String user;
    private String eventReference;
    private String eventTime;
    private boolean processed;

    public String getId() {
        return id;
    }

    public String getEventName() {
        return eventName;
    }

    public String getUser() {
        return user;
    }

    public String getEventReference() {
        return eventReference;
    }

    public String getEventTime() {
        return eventTime;
    }

    public boolean isProcessed() {
        return processed;
    }

    public Event withId(String id) {
        this.id = id;
        return this;
    }

    public Event withEventName(String eventName) {
        this.eventName = eventName;
        return this;
    }

    public Event withUser(String user) {
        this.user = user;
        return this;
    }

    public Event withEventReference(String eventReference) {
        this.eventReference = eventReference;
        return this;
    }

    public Event withEventTime(String eventTime) {
        this.eventTime = eventTime;
        return this;
    }

    public Event withProcessed(boolean processed) {
        this.processed = processed;
        return this;
    }
}
