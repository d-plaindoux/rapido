"""
Core type
"""

class Type:

    def __init__(self):
        self.data = None

    def __get_value(self, data, attributes):
        if attributes is None or not attributes:
            return data
        else:
            return self.__get_value(data[attributes[0]], attributes[1:])

    def set_value(self, data, path, virtual, attributes):
        if path is None or not path:
            data[virtual] = self.__get_value(data, attributes)
        else:
            self.set_value(data[path[0]], path[1:], virtual, attributes)
