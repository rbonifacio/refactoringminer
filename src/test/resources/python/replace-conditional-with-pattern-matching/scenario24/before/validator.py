class Validator:
    def process(self, problem):
        if problem.identifier is not None:
            return self._validate(problem.identifier)
