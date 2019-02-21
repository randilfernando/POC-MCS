package com.alternate.ollcs.mongo.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class ResumeTokenServiceImpl implements ResumeTokenService {

    @Value("${resume-token.file}")
    private String resumeTokenFile;

    @Override
    public void updateToken(String token) throws IOException {
        final File file = new File(resumeTokenFile);

        if (!file.exists()) {
            file.createNewFile();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(token);
        }
    }

    @Override
    public String getToken() throws IOException {
        final File file = new File(resumeTokenFile);

        if (!file.exists()) {
            return null;
        }

        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            line = reader.readLine();
        }

        return line;
    }
}
