package com.alternate.ollcs.websocket.services;

import com.alternate.ollcs.mongo.services.MongoEventsStreamConnector;
import com.alternate.ollcs.mongo.services.ResumeTokenService;
import com.alternate.ollcs.websocket.models.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.BsonDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.Disposable;

import java.io.IOException;
import java.util.concurrent.CancellationException;

@Service
public class EventConsumerWebSocketAPI extends TextWebSocketHandler {

    private final Object lock = new Object();

    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumerWebSocketAPI.class);
    private final ObjectMapper objectMapper;
    private MongoEventsStreamConnector mongoEventsStreamConnector;
    private ResumeTokenService resumeTokenService;

    private WebSocketSession session;
    private Disposable disposable;

    @Autowired
    public EventConsumerWebSocketAPI(ObjectMapper objectMapper, MongoEventsStreamConnector mongoEventsStreamConnector,
                                     ResumeTokenService resumeTokenService) {
        this.objectMapper = objectMapper;
        this.mongoEventsStreamConnector = mongoEventsStreamConnector;
        this.resumeTokenService = resumeTokenService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        if (this.session != null) {
            this.session.close();
        }

        this.session = session;

        final String tokenString = this.resumeTokenService.getToken();
        final BsonDocument token = (tokenString != null) ? BsonDocument.parse(tokenString) : null;
        this.disposable = this.mongoEventsStreamConnector
                .subscribe(token)
                .subscribe(e -> {
                    try {
                        String eventString = this.objectMapper.writeValueAsString(e.getEvent());
                        Message message = Message.fromMessage("MESSAGE", eventString);

                        synchronized (this.lock) {
                            this.session.sendMessage(new TextMessage(message.toString()));
                        }

                        this.resumeTokenService.updateToken(e.getResumeToken().toJson());
                    } catch (IOException e1) {
                        throw new CancellationException();
                    }
                });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        LOGGER.info("client: {} disconnected", session.getId());

        if (this.disposable != null) {
            this.disposable.dispose();
        }

        this.disposable = null;
        this.session = null;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws IOException {
        LOGGER.info("message: {} received from client: {}", textMessage.getPayload(), session.getId());

        final Message message = Message.fromString(textMessage.getPayload());

        if ("COMMIT_OFFSET".equals(message.getType())) {
            this.resumeTokenService.updateToken(message.getPayload());
        }
    }
}
