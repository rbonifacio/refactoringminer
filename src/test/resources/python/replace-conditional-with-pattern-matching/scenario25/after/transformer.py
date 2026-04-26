class Transformer:
    def transform(self, value, value_type):
        match self.encrypted_string_behavior:
            case EncryptedBehavior.DECRYPT:
                return str(value)
            case EncryptedBehavior.REDACT:
                return "[REDACTED]"
