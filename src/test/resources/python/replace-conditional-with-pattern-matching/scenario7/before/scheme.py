class ReverseLayer:
    def handle(self, spec, context):
        if spec.scheme in ("http3", "quic"):
            if not context.keep_host_header:
                context.server_sni = spec.address[0]
            child_layer = "QuicLayer"
        elif spec.scheme in ("https", "tls", "dtls"):
            if not context.keep_host_header:
                context.server_sni = spec.address[0]
            child_layer = "TLSLayer"
        elif spec.scheme in ("tcp", "http", "udp", "dns"):
            child_layer = "NextLayer"
        else:
            raise AssertionError(spec.scheme)
        return child_layer
