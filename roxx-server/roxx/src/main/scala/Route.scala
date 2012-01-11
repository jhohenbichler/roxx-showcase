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

import akka.event.EventHandler
import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import dispatch.json.{ JsObject, JsString, JsValue }
import org.bson.types.ObjectId
import sjson.json.{Reads, DefaultProtocol, Format, JsonSerialization}

object Route extends DefaultProtocol {

  private object Keys {
    val Name = "name"
    val Country = "country"
    val Region = "region"
    val Sector = "sector"
    val Grade = "grade"
    val GradeQualifier = "gradeQualifier"
    val Id = "id"
    val HasImage = "hasImage"
  }

  implicit object routeFormat extends Format[Route] {
//    import Keys._
//    asProduct7(Name, Country, Region, Sector, Grade, Id, HasImage)(apply)(unapply(_).get)

    override def reads(json: JsValue): Route = {
      val JsObject(jsObject) = json
      def value[A](key: String)(implicit reads: Reads[A]) =
        JsonSerialization.fromjson[A](jsObject(JsString(key)))
      def optionalValue[A](key: String)(implicit reads: Reads[A]) =
        jsObject get JsString(key) map JsonSerialization.fromjson[A]
      Route(
          name = value[String](Keys.Name),
          country = value[String](Keys.Country),
          region = value[String](Keys.Region),
          sector = value[String](Keys.Sector),
          grade = value[Grade](Keys.Grade),
          id = optionalValue[String](Keys.Id))
    }

    override def writes(route: Route): JsValue = {
      import JsonSerialization._
      var values = List(
          JsString(Keys.Name) -> tojson(route.name),
          JsString(Keys.Country) -> tojson(route.country),
          JsString(Keys.Region) -> tojson(route.region),
          JsString(Keys.Sector) -> tojson(route.sector),
          JsString(Keys.Grade) -> tojson(route.grade),
          JsString(Keys.HasImage) -> tojson(route.hasImage))
      for (id <- route.id) { values ::= JsString(Keys.Id) -> tojson(id) }
      JsObject(values)
    }
  }

  def fromMongoDbObject(mongoDbObject: MongoDBObject): Option[Route] =
    for {
      name <- mongoDbObject.getAs[String](Keys.Name)
      country <- mongoDbObject.getAs[String](Keys.Country)
      region <- mongoDbObject.getAs[String](Keys.Region)
      sector <- mongoDbObject.getAs[String](Keys.Sector)
      gradeValue <- mongoDbObject.getAs[Int](Keys.Grade)
      gradeQualifier = mongoDbObject.getAs[Int](Keys.GradeQualifier)
      id = mongoDbObject.getAs[ObjectId]("_id") map { _.toString }
      hasImage <- mongoDbObject.getAs[Boolean](Keys.HasImage)
    } yield Route(
        name,
        country,
        region,
        sector,
        Grade(gradeValue, gradeQualifier map { s => Grade.GradeQualifier(s.toInt) }),
        id,
        hasImage)

  implicit val mongoFormat: Route => DBObject =
    route => {
      // TODO Find a good immutable approach!
      var attributes =
        List(
            Keys.Name -> route.name,
            Keys.Country -> route.country,
            Keys.Region -> route.region,
            Keys.Sector -> route.sector,
            Keys.Grade -> route.grade.value,
            Keys.HasImage -> route.hasImage)
      for (qualifier <- route.grade.qualifier) { attributes ::= Keys.GradeQualifier -> qualifier.id }
      for (id <- route.id) { attributes ::= "_id" -> new ObjectId(id) }
      MongoDBObject(attributes)
    }
}

/**
 * A climbing route.
 *
 * Thanks to implicit conversions defined on $grade a grade can be given as `Int` only, e.g. {{{
 * Route("name", 8)
 * }}}
 *
 * @param name The name; must not be `null`!
 * @param country The country; must not be `null`!
 * @param region The region; must not be `null`!
 * @param sector The sector; must not be `null`!
 * @param grade The $grade; must not be `null`!
 * @param id The $id; defaults to `None`; must not be `null`!
 * @param hasImage Flag indicating whether there is an image; defaults to `false`
 *
 * @define grade [[com.weiglewilczek.roxx.Grade]]
 */
case class Route(
    name: String,
    country: String,
    region: String,
    sector: String,
    grade: Grade,
    id: Option[String] = None,
    hasImage: Boolean = false) {
  require(name != null, "name must not be null!")
  require(country != null, "country must not be null!")
  require(region != null, "region must not be null!")
  require(sector != null, "sector must not be null!")
  require(grade != null, "grade must not be null!")
  require(id != null, "id must not be null!")

  override def toString = "%s %s %s %s (%s)".format(name, country, region, sector, grade)
}
