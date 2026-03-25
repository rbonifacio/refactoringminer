class Classifier:
    def classify(self, value):
        if value < 0:
            return "negative"
        elif value == 0:
            return "zero"
        elif value < 100:
            return "small positive"
        else:
            return "large positive"
