class DataProcessor:

    def process(self, data):
        count: int = 0
        for item in data:
            count = count + 1
        return count
