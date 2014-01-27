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

    def virtual_value(self, data, path, virtual, attributes):
        subData = self.__get_value(data, path)
        subData[virtual] = self.__get_value(data, attributes)
