package com.alternate.djangocs.mongo.services;

import org.bson.BsonDocument;

import java.io.IOException;

public interface ResumeTokenService {

    void updateToken(BsonDocument token) throws IOException;

    BsonDocument getToken() throws IOException;
}
