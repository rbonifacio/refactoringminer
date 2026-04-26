def resolve_name(config):
    if "dataset_name" in config:
        return "{dataset_path}_{dataset_name}".format(**config)
    else:
        return "{dataset_path}".format(**config)
