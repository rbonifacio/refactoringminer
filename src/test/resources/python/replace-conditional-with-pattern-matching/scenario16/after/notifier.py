class Notifier:
    def get_url(self, key, group, event):
        match key:
            case "workflow_id":
                url = self._new_url(group, event)
            case "legacy_id":
                url = self._legacy_url(group, event)
        return url
