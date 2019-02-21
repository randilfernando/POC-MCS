package com.alternate.djangocs.mongo.services;

import java.io.IOException;

public interface ResumeTokenService {

    void updateToken(String token) throws IOException;

    String getToken() throws IOException;
}
