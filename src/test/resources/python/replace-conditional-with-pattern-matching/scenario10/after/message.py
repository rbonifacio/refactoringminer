class MessageHandler:
    def handle_record_counts(self, message, stream_message_count):
        match message.type:
            case "RECORD":
                stream_message_count[message.stream] = stream_message_count.get(message.stream, 0) + 1.0
            case "STATE":
                stream_descriptor = message.stream
                message.state_stats = message.state_stats or {}
                message.state_stats["record_count"] = stream_message_count.get(stream_descriptor, 0.0)
                stream_message_count[stream_descriptor] = 0.0
        return message
