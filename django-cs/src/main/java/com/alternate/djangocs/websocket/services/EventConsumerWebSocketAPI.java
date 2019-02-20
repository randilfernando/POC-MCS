package com.alternate.djangocs.websocket.services;

import com.alternate.djangocs.mongo.models.Event;
import com.alternate.djangocs.mongo.services.MongoEventsStreamConnector;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumerWebSocketAPI.class);
    private final ObjectMapper objectMapper;
    private ConsumerSessionHandler consumerSessionHandler;
    private MongoEventsStreamConnector mongoEventsStreamConnector;

    @Autowired
    public EventConsumerWebSocketAPI(ObjectMapper objectMapper, ConsumerSessionHandler consumerSessionHandler,
                                     MongoEventsStreamConnector mongoEventsStreamConnector) {
        this.objectMapper = objectMapper;
        this.consumerSessionHandler = consumerSessionHandler;
        this.mongoEventsStreamConnector = mongoEventsStreamConnector;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Disposable disposable = this.mongoEventsStreamConnector.connect().subscribe(event -> {
            try {
                this.sendMessage(session, event);
            } catch (IOException e) {
                throw new CancellationException();
            }
        });

        this.consumerSessionHandler.subscribeTopic(session.getId(), "events", disposable);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        LOGGER.info("client: {} disconnected", session.getId());

        this.consumerSessionHandler.removeSubscriber(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        LOGGER.info("message: {} received from client: {}", textMessage.getPayload(), session.getId());
    }

    private void sendMessage(WebSocketSession session, Event event) throws IOException {
        String eventString = this.objectMapper.writeValueAsString(event);
        session.sendMessage(new TextMessage(eventString));
        LOGGER.info("message: {} sent to client: {}", eventString, session.getId());
    }
}
