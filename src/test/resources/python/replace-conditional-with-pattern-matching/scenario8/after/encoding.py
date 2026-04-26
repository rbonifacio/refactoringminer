class PdfWriter:
    def encode_string(self, s, fonttype):
        match fonttype:
            case 1:
                return s.encode('cp1252', 'replace')
            case 3:
                return s.encode('latin-1', 'replace')
            case _:
                return s.encode('utf-16be', 'replace')
