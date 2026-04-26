class ASTParser:
    def parse_value(self, item):
        match item.value:
            case ast.Constant():
                return item.value.value
            case ast.Name():
                return item.value.id
