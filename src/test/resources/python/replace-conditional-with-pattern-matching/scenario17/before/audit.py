class ArtifactProcessor:
    def process(self, artifact):
        if artifact.artifact_type == ArtifactType.XCARCHIVE:
            return self._process_xcarchive(artifact)
        elif artifact.artifact_type == ArtifactType.APK:
            return self._process_apk(artifact)
