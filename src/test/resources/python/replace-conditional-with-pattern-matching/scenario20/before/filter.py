class ContentFilter:
    def get_message(self, m, flow):
        if m["part"] == "request":
            return flow.request
        else:
            return flow.response
