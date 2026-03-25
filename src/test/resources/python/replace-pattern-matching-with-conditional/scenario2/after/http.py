class HttpHandler:
    def handle_status(self, status_code):
        if status_code == 200:
            return "OK"
        elif status_code == 404:
            return "Not Found"
        elif status_code == 500:
            return "Internal Server Error"
        elif status_code == 403:
            return "Forbidden"
        else:
            return "Unknown status"
