class SwarmHandler:
    def handle(self, key, value, opts, parsed_options_dict):
        match key:
            case "user_count":
                opts["users"] = int(value)
            case "spawn_rate":
                opts[key] = float(value)
            case "host":
                opts[key] = str(value)
            case "run_time":
                opts[key] = value
            case "headless":
                opts[key] = bool(value)
            case _ if key in parsed_options_dict:
                opts[key] = value
