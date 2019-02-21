import websocket
from pymongo import MongoClient
from tornado import websocket as websocket_t
import tornado.ioloop
from threading import Thread
import json

client = MongoClient('localhost', 27017)
db = client['demo']
events = db['events']

try:
    import server_thread
except ImportError:
    import _thread as server_thread


def on_message(ws, message):
    parts = message.split("\n")
    print(parts[len(parts) - 1])


def on_error(ws, error):
    print(error)


def on_close(ws):
    print("close")


def on_open(ws):
    print("open")


def start_server():
    application = tornado.web.Application([(r"/", WebSocketListener), ])
    application.listen(8080)
    tornado.ioloop.IOLoop.instance().start()


def start_client():
    ws = websocket.WebSocketApp("ws://localhost:8081/ws",
                                on_message=on_message,
                                on_error=on_error,
                                on_close=on_close)
    ws.on_open = on_open
    ws.run_forever()


class WebSocketListener(websocket_t.WebSocketHandler):
    def check_origin(self, origin):
        return True

    def open(self):
        print('client connected')

    def on_message(self, message):
        parts = message.split("\n")
        print(parts[len(parts) - 1])

        if parts[0] == "INSERT":
            content = json.loads(parts[len(parts) - 1])
            events.insert_one(content)

    def on_close(self):
        print('client disconnected')


if __name__ == "__main__":
    client_thread = Thread(target=start_client)
    client_thread.start()

    application = tornado.web.Application([(r"/", WebSocketListener), ])
    application.listen(8080)
    tornado.ioloop.IOLoop.instance().start()

    client_thread.join()