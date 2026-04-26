class ReverseLayer:
    def handle(self, spec, context):
        child_layer = "NextLayer"
        match spec.scheme:
            case "http3" | "quic" | "https" | "tls" | "dtls":
                if not context.keep_host_header:
                    context.server_sni = spec.address[0]
            case "tcp" | "http" | "udp" | "dns":
                pass
            case _:
                raise AssertionError(spec.scheme)
        return child_layer
