class HttpHandler:
    def handle_status(self, status_code):
        match status_code:
            case 200:
                return "OK"
            case 404:
                return "Not Found"
            case 500:
                return "Internal Server Error"
            case 403:
                return "Forbidden"
            case _:
                return "Unknown status"
