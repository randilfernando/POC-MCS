package com.alternate.djangocs.mongo.services;

import org.bson.BsonDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class ResumeTokenServiceImpl implements ResumeTokenService {

    @Value("${resume-token.file}")
    private String resumeTokenFile;

    @Override
    public void updateToken(BsonDocument token) throws IOException {
        final File file = new File(resumeTokenFile);

        if (!file.exists()) {
            file.createNewFile();
        }

        String json = token.toJson();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(json);
        }
    }

    @Override
    public BsonDocument getToken() throws IOException {
        final File file = new File(resumeTokenFile);

        if (!file.exists()) {
            return null;
        }

        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            line = reader.readLine();
        }

        return (line != null)
                ? BsonDocument.parse(line)
                : null;
    }
}
