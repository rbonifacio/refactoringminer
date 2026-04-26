class ASTParser:
    def parse_value(self, item):
        if isinstance(item.value, ast.Constant):
            return item.value.value
        elif isinstance(item.value, ast.Name):
            return item.value.id
