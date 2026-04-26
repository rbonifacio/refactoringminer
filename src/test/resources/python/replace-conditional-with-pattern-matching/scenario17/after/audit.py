class ArtifactProcessor:
    def process(self, artifact):
        match artifact.state:
            case State.PENDING:
                return self._start(artifact)
            case State.COMPLETE:
                return self._finalize(artifact)
