class KindMapper:
    def to_key(self, kind):
        if kind in {Kind.ENTAILMENT, Kind.PRECEDENCE, Kind.PRIORITY}:
            return kind.name.lower()
        return None
