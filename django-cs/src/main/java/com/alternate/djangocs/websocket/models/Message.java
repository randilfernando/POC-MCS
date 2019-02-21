package com.alternate.djangocs.websocket.models;

public class Message {
    private String type;
    private String payload;

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public Message withType(String type) {
        this.type = type;
        return this;
    }

    public Message withPayload(String payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public String toString() {
        return this.type + "\n" + this.payload;
    }

    public static Message fromString(String s) {
        String[] parts = s.split("\n");
        return new Message()
                .withType(parts[0])
                .withPayload(parts[parts.length - 1]);
    }

    public static Message fromMessage(String type, String payload) {
        return new Message()
                .withType(type)
                .withPayload(payload);
    }
}
