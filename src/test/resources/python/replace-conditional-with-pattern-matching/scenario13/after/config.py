def resolve_name(config):
    match config:
        case {"dataset_path": dp, "dataset_name": dn}:
            return f"{dp}_{dn}"
        case {"dataset_path": dp}:
            return dp
        case _:
            raise ValueError("missing dataset_path")
