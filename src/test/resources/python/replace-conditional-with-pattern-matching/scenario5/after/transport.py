class FileTransfer:
    def transfer(self, method, sftp_action, in_path, out_path):
        match method:
            case 'sftp':
                self.run_sftp(in_path, out_path)
            case 'scp':
                if sftp_action == 'get':
                    self.run_scp(in_path, out_path, direction='get')
                else:
                    self.run_scp(in_path, out_path, direction='put')
            case 'piped':
                if sftp_action == 'get':
                    self.run_piped_get(in_path, out_path)
                else:
                    self.run_piped_put(in_path, out_path)
