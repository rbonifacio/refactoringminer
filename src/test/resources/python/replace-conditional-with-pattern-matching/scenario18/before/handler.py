class StreamHandler:
    def get_descriptor(self, message):
        if message.type == "RECORD":
            return StreamDescriptor(name=message.record.stream)
        elif message.type == "STATE":
            return StreamDescriptor(name=message.state.stream)
        else:
            raise NotImplementedError(f"Unsupported type: {message.type}")
