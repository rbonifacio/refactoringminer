class MessageConverter:
    def convert(self, message):
        match message:
            case HumanMessage():
                return {"role": "user", "content": message.content}
            case AIMessage():
                return {"role": "assistant", "content": message.content}
            case SystemMessage():
                return {"role": "system", "content": message.content}
