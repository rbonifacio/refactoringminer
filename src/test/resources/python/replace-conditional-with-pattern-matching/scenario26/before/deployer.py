class Deployer:
    def get_status(self, preprod_artifact):
        if (
            preprod_artifact.state == ArtifactState.UPLOADING
            or preprod_artifact.state == ArtifactState.UPLOADED
        ):
            return StatusCheck.IN_PROGRESS
        else:
            return StatusCheck.FAILED
