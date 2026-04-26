class TypeConverter:
    def convert(self, value, value_type):
        if value_type in ('bool', 'boolean'):
            return bool(value)
        elif value_type in ('int', 'integer'):
            return int(value)
        elif value_type in ('float', 'double'):
            return float(value)
        elif value_type in ('str', 'string'):
            return str(value)
