import threading

import websocket
from websocket_server import WebsocketServer

api_server_port = 8000
cs_client_url = 'ws://localhost:8080/ws'
oll_client_url = 'ws://localhost:9000'


class EventsProcessor:
    @staticmethod
    def process(message):
        parts = message.split("\n")
        print('message received: ' + parts[2])

        command = parts[0]

        if command == "MESSAGE":
            WebSoketAPIServerHandler.send_to_all(message)

            print('Event transferred to client')
        elif command == "INSERT":
            WebSocketOLLClientHandler.send_message(message)

            print('Event transffered to oll')
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


class WebSocketOLLClientHandler:
    oll_client = None

    @staticmethod
    def on_connected(ws):
        WebSocketOLLClientHandler.oll_client = ws
        print("connected with oll")

    @staticmethod
    def on_disconnected(ws):
        print("disconnected with oll")

    @staticmethod
    def on_error(ws, error):
        print(error)

    @staticmethod
    def on_message(ws, message):
        EventsProcessor.process(message)

    @staticmethod
    def send_message(msg):
        WebSocketOLLClientHandler.oll_client.send(msg)


def start_server_task():
    ws = WebsocketServer(api_server_port)
    ws.set_fn_new_client(WebSoketAPIServerHandler.on_connected)
    ws.set_fn_client_left(WebSoketAPIServerHandler.on_disconnected)
    ws.set_fn_message_received(WebSoketAPIServerHandler.on_message)

    WebSoketAPIServerHandler.api_server = ws
    ws.run_forever()


def start_cs_client_task():
    ws = websocket.WebSocketApp(cs_client_url,
                                on_message=WebSocketCSClientHandler.on_message,
                                on_error=WebSocketCSClientHandler.on_error,
                                on_close=WebSocketCSClientHandler.on_disconnected)
    ws.on_open = WebSocketCSClientHandler.on_connected
    ws.run_forever()


def start_oll_client_task():
    ws = websocket.WebSocketApp(oll_client_url,
                                on_message=WebSocketOLLClientHandler.on_message,
                                on_error=WebSocketOLLClientHandler.on_error,
                                on_close=WebSocketOLLClientHandler.on_disconnected)
    ws.on_open = WebSocketOLLClientHandler.on_connected

    ws.run_forever()


if __name__ == "__main__":
    t1 = threading.Thread(target=start_server_task)
    t2 = threading.Thread(target=start_cs_client_task)
    t3 = threading.Thread(target=start_oll_client_task)

    t1.start()
    t2.start()
    t3.start()
