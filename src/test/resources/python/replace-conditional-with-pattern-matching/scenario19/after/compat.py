def get_class(name):
    match name:
        case 'LegacyFilterTypeError':
            return LegacyFilterTypeError
        case 'NewFilterTypeError':
            return NewFilterTypeError
