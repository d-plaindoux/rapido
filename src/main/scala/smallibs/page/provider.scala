/*
 * Copyright (C)2014 D. Plaindoux.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package smallibs.page

trait DataProvider {

  def values: List[DataProvider]

  def get(name: String): Option[DataProvider]

  def set(name: String, data: DataProvider): DataProvider

}

// ------------------------------------------------------------------

class ConstantProvider(value: String) extends DataProvider {
  def values: List[DataProvider] = Nil

  def get(name: String): Option[DataProvider] = None

  def set(name: String, data: DataProvider): DataProvider =
    throw new IllegalAccessException

  override def toString: String = value
}

// ------------------------------------------------------------------

class RecordProvider(map: Map[String, DataProvider]) extends DataProvider {
  def values: List[DataProvider] = map.values.toList

  def get(name: String): Option[DataProvider] = map get name

  def set(name: String, data: DataProvider): DataProvider =
    new RecordProvider(map + (name -> data))
}

// ------------------------------------------------------------------

class SetProvider(set: List[DataProvider]) extends DataProvider {
  def values: List[DataProvider] = set

  def get(name: String): Option[DataProvider] =
    throw new IllegalAccessException

  def set(name: String, data: DataProvider): DataProvider =
    throw new IllegalAccessException
}

// ------------------------------------------------------------------

object Provider {
  def constant(value: String): DataProvider =
    new ConstantProvider(value)

  def record(values: (String, DataProvider)*): DataProvider =
    record(values.toMap)

  def record(values: Map[String, DataProvider]): DataProvider =
    new RecordProvider(values)

  def empty: DataProvider =
    set()

  def set(provides: DataProvider*): DataProvider =
    set(provides.toList)

  def set(providers: List[DataProvider]): DataProvider =
    new SetProvider(providers)
}
