class FileTransfer:
    def transfer(self, method, sftp_action, in_path, out_path):
        if method == 'sftp':
            self.run_sftp(in_path, out_path)
        elif method == 'scp':
            if sftp_action == 'get':
                self.run_scp(in_path, out_path, direction='get')
            else:
                self.run_scp(in_path, out_path, direction='put')
        elif method == 'piped':
            if sftp_action == 'get':
                self.run_piped_get(in_path, out_path)
            else:
                self.run_piped_put(in_path, out_path)
