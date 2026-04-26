class Cleaner:
    def clean(self, dirty):
        match dirty:
            case MutableSequence():
                dirty.clear()
