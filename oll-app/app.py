import json
import threading

import websocket
from bson.objectid import ObjectId
from pymongo import MongoClient
from websocket_server import WebsocketServer

api_server_port = 9000
cs_client_url = 'ws://localhost:9090/ws'

mongo_host = 'localhost'
mongo_port = 27017
mongo_db_name = "demo"


class EventsDBConnector:
    client = MongoClient(mongo_host, mongo_port)
    db = client[mongo_db_name]
    events = db['events']

    @staticmethod
    def insert_event(document):
        EventsDBConnector.events.insert_one(document)

    @staticmethod
    def update_event(id, document):
        EventsDBConnector.events.update_one({'_id': ObjectId(id)}, {'$set': document}, True)


class EventsProcessor:
    @staticmethod
    def process(message):
        parts = message.split("\n")
        print('message received: ' + parts[2])

        command = parts[0]
        content = json.loads(parts[2])

        if command == "MESSAGE":
            content['processed'] = True
            id = content['id']
            del content['id']

            EventsDBConnector.update_event(id, content)
            message_out = "COMMIT_OFFSET\n{}\n" + parts[1]
            WebSocketCSClientHandler.send_message(message_out)

            print('Event processed and updated')
        elif command == "INSERT":
            EventsDBConnector.insert_event(content)

            print('Event inserted')
        else:
            print('Unknown command')


class WebSoketAPIServerHandler:
    api_server = None

    @staticmethod
    def on_connected(client, server):
        print('client connected')

    @staticmethod
    def on_disconnected(client, server):
        print('client disconnected')

    @staticmethod
    def on_message(client, server, message):
        EventsProcessor.process(message)

    @staticmethod
    def send_to_all(msg):
        WebSoketAPIServerHandler.api_server.send_message_to_all(msg)


class WebSocketCSClientHandler:
    cs_client = None

    @staticmethod
    def on_connected(ws):
        WebSocketCSClientHandler.cs_client = ws
        print("connected with cs connector")

    @staticmethod
    def on_disconnected(ws):
        print("disconnected with cs connector")

    @staticmethod
    def on_error(ws, error):
        print(error)

    @staticmethod
    def on_message(ws, message):
        EventsProcessor.process(message)

    @staticmethod
    def send_message(msg):
        WebSocketCSClientHandler.cs_client.send(msg)


def start_server_task():
    ws = WebsocketServer(api_server_port)
    ws.set_fn_new_client(WebSoketAPIServerHandler.on_connected)
    ws.set_fn_client_left(WebSoketAPIServerHandler.on_disconnected)
    ws.set_fn_message_received(WebSoketAPIServerHandler.on_message)

    WebSoketAPIServerHandler.api_server = ws
    ws.run_forever()


def start_client_task():
    ws = websocket.WebSocketApp(cs_client_url,
                                on_message=WebSocketCSClientHandler.on_message,
                                on_error=WebSocketCSClientHandler.on_error,
                                on_close=WebSocketCSClientHandler.on_disconnected)
    ws.on_open = WebSocketCSClientHandler.on_connected
    ws.run_forever()


if __name__ == "__main__":
    t1 = threading.Thread(target=start_server_task)
    t2 = threading.Thread(target=start_client_task)

    t1.start()
    t2.start()
