class TypeConverter:
    def convert(self, value, value_type):
        match value_type:
            case 'bool' | 'boolean':
                return bool(value)
            case 'int' | 'integer':
                return int(value)
            case 'float' | 'double':
                return float(value)
            case 'str' | 'string':
                return str(value)
