class LLMFactory:
    def get_llm(self, provider, config, api_key):
        api_key_secret = api_key if api_key else None
        match provider:
            case 'openai':
                kwargs = {'model': config['model_name'], 'temperature': 0.0}
                if api_key_secret:
                    kwargs['api_key'] = api_key_secret
                return kwargs
            case 'anthropic':
                kwargs = {'model_name': config['model_name'], 'temperature': 0.0}
                if api_key_secret:
                    kwargs['api_key'] = api_key_secret
                return kwargs
            case 'google':
                kwargs = {'model': config['model_name'], 'temperature': 0.0}
                if api_key_secret:
                    kwargs['api_key'] = api_key_secret
                return kwargs
            case 'openai_compatible':
                kwargs = {'model': config['model_name'], 'base_url': config.get('base_url'), 'temperature': 0.0}
                if api_key_secret:
                    kwargs['api_key'] = api_key_secret
                return kwargs
            case _:
                raise ValueError(f'Unknown provider: {provider}')
