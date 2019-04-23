#!/usr/bin/env python3
# This is a hacky webserver to use when developing
# Its main advantage is that, being written in Python 3, most people won't
# need to install anything to get it running.
import http.server as hs
import socketserver as ss

def allow_dirs(path, dir_list):
    for d in dir_list:
        if path.startswith(d):
            return True
    return False

class RewritingHTTPRequestHandler(hs.SimpleHTTPRequestHandler):
    def translate_path(self, path):
        if allow_dirs(path, ['/cssout', '/target', '/img']):
            return hs.SimpleHTTPRequestHandler.translate_path(self, path)
        return "index.html"

if __name__ == "__main__":
    with ss.TCPServer(("127.0.0.1", 8080),
                      RewritingHTTPRequestHandler) as server:
        print("HTTP server started, listenning on 127.0.0.1:8080...")
        try:
            server.serve_forever()
        except:
            server.server_close()
