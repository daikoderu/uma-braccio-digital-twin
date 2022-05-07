def decode_dict(dct: dict):
    result = {}
    for key in dct.keys():
        result[key.decode()] = dct[key].decode()
    return result