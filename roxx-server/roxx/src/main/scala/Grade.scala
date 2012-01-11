/*
 * Copyright 2011 Weigle Wilczek GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weiglewilczek.roxx

import sjson.json.{ DefaultProtocol, Format, JsonSerialization }
import dispatch.json.{ JsObject, JsString, JsValue }

object Grade extends DefaultProtocol {

  /**
   * The optional qualifier of a $grade, i.e. `Plus` ("+") or `Minus` ("-").
   *
   * @define grade [[com.weiglewilczek.roxx.Grade]]
   */
  object GradeQualifier extends Enumeration {

    type GradeQualifier = Value

    val Plus = Value("+")

    val Minus = Value("-")

    implicit object ValueFormat extends Format[Value] {

      private val Id = "id"

      def reads(json: JsValue): Value = {
        import DefaultProtocol._
        val JsObject(jsObject) = json
        GradeQualifier(JsonSerialization.fromjson[Int](jsObject(JsString(Id))))
      }

      def writes(value: Value): JsValue =
        JsObject(List(JsString(Id) -> JsValue(value.id)))
    }

    implicit def valueToOption(gradeQualifier: Value) = Option(gradeQualifier)
  }

  implicit val format: Format[Grade] =
    asProduct2("value", "qualifier")(apply)(unapply(_).get)

  implicit def intToGrade(value: Int) = Grade(value)
}

/**
 * The grade of a $route.
 *
 * Thanks to implicit conversions defined on $gradeQualifier a qualifier can be given without
 * wrapping it into an `Option`, e.g. {{{
 * Grade(8, Plus)
 * }}}
 * 
 * @param value The value; must be between `1` and `13`!
 * @param qualifier The optional qualifier; defaults to `None`; must not be `null`!
 *
 * @define route [[com.weiglewilczek.roxx.Route]]
 * @define gradeQualifier [[com.weiglewilczek.roxx.GradeQualifier]]
 */
case class Grade(value: Int, qualifier: Option[Grade.GradeQualifier.Value] = None) {
  require(value >= 1 && value <= 13, "value must be between 1 and 13!")
  require(qualifier != null, "qualifier must not be null!")

  override def toString = value + (qualifier map { _.toString } getOrElse "")
}
