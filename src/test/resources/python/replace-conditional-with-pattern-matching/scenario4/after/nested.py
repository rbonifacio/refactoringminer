class Renderer:
    def render(self, shape):
        match shape:
            case "circle":
                if self.radius > 0:
                    self.draw_circle()
            case "square":
                self.draw_square()
            case _:
                self.draw_default()
