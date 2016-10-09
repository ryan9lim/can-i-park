from http.server import SimpleHTTPRequestHandler, HTTPServer
from fetch import query
from urllib.parse import urlparse, parse_qs

class HTTPHandler(SimpleHTTPRequestHandler):

    def do_GET(self):
        print(self.path)
        params = parse_qs(urlparse(self.path).query)
        print(params, self.path)
        try:
            res = query(float(params['lat'][0]), float(params['lng'][0]))
            self.send_response(200)
            self.send_header('Content-type','text/html')
            self.end_headers()
            self.wfile.write(bytes(str(res), "utf8"))
        except (ValueError, KeyError) as e:
            self.send_response(400)
            self.send_header('Content-type','text/html')
            self.end_headers()
            self.wfile.write(bytes("Bad request params", "utf8"))
        return

def run():
  print('starting server...')
  server_address = ('0.0.0.0', 8081)
  httpd = HTTPServer(server_address, HTTPHandler)
  httpd.serve_forever()

run()
