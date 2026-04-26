class KindMapper:
    def to_key(self, kind):
        match kind:
            case Kind.ENTAILMENT | Kind.PRECEDENCE | Kind.PRIORITY:
                return kind.name.lower()
            case _:
                return None
