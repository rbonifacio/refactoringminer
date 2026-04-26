class Notifier:
    def get_url(self, key, group, event):
        if self.use_new_engine:
            url = self._new_url(group, event)
        else:
            url = self._legacy_url(group, event)
        return url
