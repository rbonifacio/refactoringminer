class Renderer:
    def render(self, shape):
        if shape == "circle":
            if self.radius > 0:
                self.draw_circle()
        elif shape == "square":
            self.draw_square()
        else:
            self.draw_default()
