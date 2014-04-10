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


class BasicType:
    """
    Basic class dedicated to type management providing facilities for
    attribute get and set operations.
    """

    def __init__(self):
        self.data = None

    @staticmethod
    def get_value(data, attributes):
        current = data
        for key in attributes:
            current = current[key]
        return current

    @staticmethod
    def set_value(data, path, virtual, value):
        current = data
        for key in path:
            if key not in current:
                current[key] = dict()
            current = current[key]
        current[virtual] = value

    def set_virtual_value(self, data, path, virtual, attributes):
        self.set_value(data, path, virtual, self.get_value(data, attributes))


# Set of facilitators
class Type:
    """
    This class proposes a set of functions dedicated to type projection requires
    by getters.
    """

    def __init__(self):
        pass

    @staticmethod
    def list(f):
        return lambda l: [f(e) for e in l]

    @staticmethod
    def primitive():
        return lambda e: e

    @staticmethod
    def data(f):
        return f