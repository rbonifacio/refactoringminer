class ArtifactProcessor:
    def get_url_params(self, artifact):
        if artifact.artifact_type == ArtifactType.XCARCHIVE:
            return "?format=plist"
