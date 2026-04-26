class Validator:
    def process(self, problem):
        match problem.identifier.typ:
            case IdentifierType.FQDN:
                return self._validate_fqdn(problem.identifier)
            case IdentifierType.IP:
                return self._validate_ip(problem.identifier)
