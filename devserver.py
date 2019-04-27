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

ext_map = {
    '.manifest': 'text/cache-manifest',
    '.html': 'text/html',
    '.png': 'image/png',
    '.jpg': 'image/jpg',
    '.svg':	'image/svg+xml',
    '.css':	'text/css',
    '.js':	'application/x-javascript',
    '': 'application/octet-stream', # Default
}

class RewritingHTTPRequestHandler(hs.SimpleHTTPRequestHandler):
    def translate_path(self, path):
        if allow_dirs(path, ['/cssout', '/target', '/assets']):
            return hs.SimpleHTTPRequestHandler.translate_path(self, path)
        return "index.html"

if __name__ == "__main__":
    with ss.TCPServer(("127.0.0.1", 8080),
                      RewritingHTTPRequestHandler) as server:
        server.extensions_map = ext_map
        print("HTTP server started, listenning on 127.0.0.1:8080...")
        try:
            server.serve_forever()
        except:
            server.server_close()
