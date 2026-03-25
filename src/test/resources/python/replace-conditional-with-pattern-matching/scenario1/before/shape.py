class ShapeProcessor:
    def describe(self, shape):
        if shape == "circle":
            return "A round shape"
        elif shape == "square":
            return "A shape with four equal sides"
        elif shape == "triangle":
            return "A shape with three sides"
        else:
            return "Unknown shape"
