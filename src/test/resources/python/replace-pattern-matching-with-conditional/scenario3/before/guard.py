class Classifier:
    def classify(self, value):
        match value:
            case x if x < 0:
                return "negative"
            case 0:
                return "zero"
            case x if x < 100:
                return "small positive"
            case _:
                return "large positive"
