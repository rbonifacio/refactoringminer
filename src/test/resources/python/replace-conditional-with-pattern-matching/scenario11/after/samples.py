class FewShotConfig:
    def get_samples(self, samples):
        match samples:
            case list():
                return samples
            case _ if callable(samples):
                return samples()
            case _:
                raise ValueError("samples must be a list or callable")
