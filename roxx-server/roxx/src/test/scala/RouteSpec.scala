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

import dispatch.json._
import org.specs2.Specification
import sjson.json.JsonSerialization

class RouteSpec extends Specification { def is =

  "This is a specification to unit-test the Route object:"                     ^
    "A Route should be conversible to JSON and back"                           ! jsonRoute ^
    "A Route should express its hasImage property in its JSON data"            ! jsonRouteHasImage ^
    "A Route should be conversible to Mongo format and back"                   ! mongoRoute ^
                                                                               p ^
  "This is a specification to unit-test the Route class:"                      ^
                                                                               p ^
  "Creating a Route instance ..."                                              ^
    "with a null name should throw an IAE"                                     ! precon01 ^
    "with a null country should throw an IAE"                                  ! precon02 ^
    "with a null region should throw an IAE"                                   ! precon03 ^
    "with a null sector should throw an IAE"                                   ! precon04 ^
    "with a null grade should throw an IAE"                                    ! precon05 ^
    "with a null id should throw an IAE"                                       ! precon06 ^
                                                                               p ^
  "Calling toString ..."                                                       ^
    """should return "name country region sector (grade)" """                  ! precon07

  def jsonRoute = {
    {
      val route = Route("name", "country", "region", "sector", Grade(8, Some(Grade.GradeQualifier.Plus)))
      val json = JsonSerialization tojson route
      JsonSerialization.fromjson[Route](json) must_== route
    } and {
      val route = Route("name", "country", "region", "sector", Grade(8), Some("id"))
      val json = JsonSerialization tojson route
      JsonSerialization.fromjson[Route](json) must_== route
    }
  }

  def jsonRouteHasImage = {
    {
      val route = Route("name", "country", "region", "sector", Grade(8))
      val JsObject(json) = JsonSerialization tojson route
      json must havePair(JsString("hasImage") -> JsValue(false))
    } and  {
      val route = Route("name", "country", "region", "sector", Grade(8), None, true)
      val JsObject(json) = JsonSerialization tojson route
      json must havePair(JsString("hasImage") -> JsValue(true))
    }
  }

  def mongoRoute = {
    val route = Route("name", "DE", "region", "sector", Grade(8, Some(Grade.GradeQualifier.Plus)))
    val mongoDBObject = Route mongoFormat route
    import com.mongodb.casbah.Implicits._
    Route fromMongoDbObject mongoDBObject must_== Some(route)
  }

  def precon01 = {
    Route(null, "DE", "region", "sector", Grade(8)) must throwA [IllegalArgumentException]
  }

  def precon02 = {
    Route("name", null, "region", "sector", Grade(8)) must throwA [IllegalArgumentException]
  }

  def precon03 = {
    Route("name", "DE", null, "sector", Grade(8)) must throwA [IllegalArgumentException]
  }

  def precon04 = {
    Route("name", "DE", "region", null, Grade(8)) must throwA [IllegalArgumentException]
  }

  def precon05 = {
    Route("name", "DE", "region", "sector", null) must throwA [IllegalArgumentException]
  }

  def precon06 = {
    Route("name", "DE", "region", "sector", Grade(8), null) must throwA [IllegalArgumentException]
  }

  def precon07 = {
    Route("name", "DE", "region", "sector", Grade(8)).toString must_== "name DE region sector (8)"
  }
}
