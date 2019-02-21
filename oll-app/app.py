import json

import websocket
from pymongo import MongoClient

client = MongoClient('localhost', 27017)
db = client['demo']
events = db['events']

try:
    import thread
except ImportError:
    import _thread as thread


def on_message(ws, message):
    parts = message.split("\n")
    print(parts[len(parts) - 1])

    if parts[0] == "MESSAGE":
        content = json.loads(parts[len(parts) - 1])
        content['processed'] = True
        print('processed')
        id = content['_id']
        del content['_id']

        events.replace_one({'_id': id}, {'$set': content}, {'upsert': True})


def on_error(ws, error):
    print(error)


def on_close(ws):
    print("close")


def on_open(ws):
    print("open")


if __name__ == "__main__":
    ws = websocket.WebSocketApp("ws://localhost:8082/ws",
                                on_message=on_message,
                                on_error=on_error,
                                on_close=on_close)
    ws.on_open = on_open
    ws.run_forever()
