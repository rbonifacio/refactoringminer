class ArtifactProcessor:
    def get_url_params(self, artifact):
        match artifact.artifact_type:
            case ArtifactType.XCARCHIVE:
                return "?format=plist"
            case ArtifactType.AAB:
                return "?format=apk"
