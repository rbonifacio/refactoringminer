class ContentFilter:
    def get_message(self, m, flow):
        match m["type"]:
            case "request-body":
                return flow.request
            case "response-body":
                return flow.response
            case _:
                return None
