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

package @OPT[|@USE::package.|]core

object Type {

  type ToType[E] = JSon => E

  def list[E](f: ToType[E]): ToType[List[E]] = _.asInstanceOf[ArrayData].data.map(f)

  def primitive[E]: ToType[E] = _.toRaw.asInstanceOf[E]

  def map: ToType[Map[String, Any]] = primitive[Map[String, Any]]

  def integer: ToType[Int] = primitive[Int]

  def string: ToType[String] = primitive[String]

  def boolean: ToType[Boolean] = primitive[Boolean]

  def data[E](f: ToType[E]): ToType[E] = f

}
