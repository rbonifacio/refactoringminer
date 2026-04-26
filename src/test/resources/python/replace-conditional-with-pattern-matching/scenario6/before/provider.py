class LLMFactory:
    def get_llm(self, provider, config, api_key):
        api_key_secret = api_key if api_key else None
        if provider == 'openai':
            kwargs = {'model': config['model_name'], 'temperature': 0.0}
            if api_key_secret:
                kwargs['api_key'] = api_key_secret
            return kwargs
        elif provider == 'anthropic':
            kwargs = {'model_name': config['model_name'], 'temperature': 0.0}
            if api_key_secret:
                kwargs['api_key'] = api_key_secret
            return kwargs
        elif provider == 'google':
            kwargs = {'model': config['model_name'], 'temperature': 0.0}
            if api_key_secret:
                kwargs['api_key'] = api_key_secret
            return kwargs
        elif provider == 'openai_compatible':
            kwargs = {'model': config['model_name'], 'base_url': config.get('base_url'), 'temperature': 0.0}
            if api_key_secret:
                kwargs['api_key'] = api_key_secret
            return kwargs
