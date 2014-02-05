#
#  Copyright (C)2014 D. Plaindoux.
#
#  This program is free software; you can redistribute it and/or modify it
#  under the terms of the GNU Lesser General Public License as published
#  by the Free Software Foundation; either version 2, or (at your option) any
#  later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU Lesser General Public License for more details.
#
#  You should have received a copy of the GNU Lesser General Public License
#  along with this program; see the file COPYING.  If not, write to
#  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
#


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
