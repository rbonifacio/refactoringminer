class StreamHandler:
    def get_descriptor(self, message):
        match message.type:
            case "RECORD":
                return StreamDescriptor(name=message.record.stream)
            case "STATE":
                return StreamDescriptor(name=message.state.stream)
            case _:
                raise NotImplementedError(f"Unsupported type: {message.type}")
