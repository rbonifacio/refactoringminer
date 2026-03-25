class ShapeProcessor:
    def describe(self, shape):
        match shape:
            case "circle":
                return "A round shape"
            case "square":
                return "A shape with four equal sides"
            case "triangle":
                return "A shape with three sides"
            case _:
                return "Unknown shape"
