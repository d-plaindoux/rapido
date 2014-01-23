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

package smallibs.rapido.utils

object Options {

  def parse(usage: String, args: Array[String]): (Map[Symbol, String], Map[String, String]) = {
    if (args.length == 0) {
      println(usage)
      sys.exit(1)
    }

    def nextArguments(arguments: Map[String, String], list: List[String]): Map[String, String] = {
      list match {
        case Nil =>
          arguments
        case head :: tail =>
          val equalSep = head indexOf "="
          if (equalSep > 0) {
            val name = head.substring(0, equalSep)
            val value = head.substring(equalSep + 1, head.length)
            nextArguments(arguments ++ Map(name -> value), tail)
          } else
            nextArguments(arguments, tail)
      }
    }

    def nextOption(map: Map[Symbol, String], list: List[String]): (Map[Symbol, String], Map[String, String]) = {
      list match {
        case Nil => (map, Map())
        case "--lang" :: value :: tail =>
          nextOption(map ++ Map('lang -> value), tail)
        case "--api" :: value :: tail =>
          nextOption(map ++ Map('api -> value), tail)
        case "--out" :: value :: tail =>
          nextOption(map ++ Map('out -> value), tail)
        case "--" :: tail =>
          (map, nextArguments(Map(), tail))
        case option :: tail => println("Unknown option " + option)
          sys.exit(1)
      }
    }

    nextOption(Map(), args.toList)
  }

}
