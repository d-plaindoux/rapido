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

package smallibs.page.lang.ast

import scala.util.parsing.input.Positional

//
// Template definitions
//

trait Template extends Positional

case class Value(name: Option[String], content: Option[Template]) extends Template

case class Macro(name: String, content: Template) extends Template

case class Define(name: String, content: Template) extends Template

case class Use(name: String, content: Option[Template]) extends Template

case class Repetition(name: Option[String], separator: Option[String], content: Option[Template]) extends Template

case class Optional(name: Option[String], content: Option[Template]) extends Template

case class Alternate(name: Option[String], content: List[Template]) extends Template

case object NoTemplate extends Template

case class Text(text: String) extends Template

case class Sequence(sequence: List[Template]) extends Template
