class SwarmHandler:
    def handle(self, key, value, opts, parsed_options_dict):
        if key == "user_count":
            opts["users"] = int(value)
        elif key == "spawn_rate":
            opts[key] = float(value)
        elif key == "host":
            opts[key] = str(value)
        elif key == "run_time":
            opts[key] = value
        elif key == "headless":
            opts[key] = bool(value)
        elif key in parsed_options_dict:
            opts[key] = value
