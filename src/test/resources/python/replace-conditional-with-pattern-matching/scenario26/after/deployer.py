class Deployer:
    def get_status(self, preprod_artifact):
        match preprod_artifact.state:
            case ArtifactState.UPLOADING | ArtifactState.UPLOADED:
                return StatusCheck.IN_PROGRESS
            case _:
                return StatusCheck.FAILED
