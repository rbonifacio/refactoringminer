class Cleaner:
    def clean(self, dirty):
        if isinstance(dirty, MutableSequence):
            dirty.clear()
