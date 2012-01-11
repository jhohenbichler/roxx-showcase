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

import org.specs2.Specification
import sjson.json.JsonSerialization

class GradeSpec extends Specification { def is =

  "This is a specification to unit-test the Grade object:"                     ^
    "An Int should be implicitly converted into a Grade instance"              ! implicitlyIntToGrade ^
    "A Grade should be conversible to JSON and back"                           ! jsonGrade ^
    "A GradeQualifier should be conversible to JSON and back"                  ! jsonGradeQualifier ^
                                                                               p ^
  "This is a specification to unit-test the Grade class:"                      ^
                                                                               p ^
    "Creating a Grade instance ..."                                            ^
      "with a value smaller than 1 should throw an IAE"                        ! precon01 ^
      "with a value greater than 13 should throw an IAE"                       ! precon02 ^
      "with a null qualifier should throw an IAE"                              ! precon03 ^
      "without qualifier should return to a Grade with a qualifier of None"    ! precon04 ^
                                                                               p ^
    "Calling toString ..."                                                     ^
      """on a Grade(8, None) should return "1" """                             ! precon05 ^
      """on a Grade(8, Some(Plus)) should return "1+" """                      ! precon06 ^
      """on a Grade(8, Some(Minus)) should return "1-" """                     ! precon07

  def implicitlyIntToGrade = {
    val grade: Grade = 1
    grade.value must_== 1
  }

  def jsonGrade = {
    val grade = Grade(8, Grade.GradeQualifier.Plus)
    val json = JsonSerialization tojson grade
    JsonSerialization.fromjson[Grade](json) must_== grade
  }

  def jsonGradeQualifier = {
    val gradeQualifier = Grade.GradeQualifier.Plus
    val json = JsonSerialization tojson gradeQualifier
    JsonSerialization.fromjson[Grade.GradeQualifier.Value](json) must_== gradeQualifier
  }

  def precon01 =
    Grade(0) must throwA[IllegalArgumentException]

  def precon02 =
    Grade(84) must throwA[IllegalArgumentException]

  def precon03 =
    Grade(8, null) must throwA[IllegalArgumentException]

  def precon04 =
    Grade(8).qualifier must be(None)

  def precon05 =
    Grade(8).toString must_== "8"

  def precon06 =
    Grade(8, Grade.GradeQualifier.Plus).toString must_== "8+"

  def precon07 =
    Grade(8, Grade.GradeQualifier.Minus).toString must_== "8-"
}
