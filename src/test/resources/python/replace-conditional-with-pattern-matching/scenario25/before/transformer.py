class Transformer:
    def transform(self, value, value_type):
        if self.apply_transforms and value_type in self.type_mapping:
            return self.type_mapping[value_type](value)
