class FewShotConfig:
    def get_samples(self, samples):
        if isinstance(samples, list):
            return samples
        elif callable(samples):
            return samples()
        else:
            raise ValueError("samples must be a list or callable")
