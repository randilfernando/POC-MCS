package com.alternate.ollcs.websocket.models;

public class Message {
    private String type;
    private String headers;
    private String payload;

    public String getType() {
        return type;
    }

    public String getHeaders() {
        return headers;
    }

    public String getPayload() {
        return payload;
    }

    public Message withType(String type) {
        this.type = type;
        return this;
    }

    public Message withHeaders(String headers) {
        this.headers = headers;
        return this;
    }

    public Message withPayload(String payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public String toString() {
        return this.type + "\n" + this.headers + "\n" + this.payload;
    }

    public static Message fromString(String s) {
        String[] parts = s.split("\n");
        return new Message()
                .withType(parts[0])
                .withHeaders(parts[1])
                .withPayload(parts[2]);
    }

    public static Message fromMessage(String type, String payload) {
        return new Message()
                .withType(type)
                .withHeaders("{}")
                .withPayload(payload);
    }

    public static Message fromMessage(String type, String headers, String payload) {
        return new Message()
                .withType(type)
                .withHeaders(headers)
                .withPayload(payload);
    }
}
