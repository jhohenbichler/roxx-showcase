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

import com.mongodb.casbah.Imports._
import org.apache.commons.io.IOUtils
import java.io.{FileInputStream, File, ByteArrayOutputStream}
import scala.util.Random

object InitTestData {

  private val roxxDb = MongoConnection()("roxx")
  private val routesCollection = roxxDb("routes")
  val routes = MongoConnection()("roxx")("routes")

  def createRandomDummies(country: String):List[(Route, Option[String])]={
    val rand = new Random();
    val dummies = for { count <- 1 to 10
      grade = 4 + rand.nextInt(8) // generate test data from 4 to 12
      val tuple = route("route" + count, country, "region" + count, "sector" + count, Grade(grade))
    } yield(tuple)
    dummies.toList
  }

  def route(name: String, country: String, region: String, sector: String, grade: Grade, imageFileName: Option[String] = None):(Route, Option[String])={
    val fileOption = imageFileName match {  case Some(fn) => val file = new File(fn); if(file.exists == true) { Some(fn) } else { println("Could not find image: " + file.getAbsoluteFile); None }
                                            case _ => None }
    val r = Route(name, country, region, sector, grade, None, fileOption.isDefined )
    (r, fileOption)
  }

  def main(args: Array[String]) {

    routesCollection.drop

    val realRoutes =
      route("Gamba Leggare", "CH", "Tessin", "sector1", Grade(7, Some(Grade.GradeQualifier.Plus)), Some("""1.jpg""")) ::
      route("Corona", "DE", "Frankenjura", "sector1", Grade(11, Some(Grade.GradeQualifier.Plus)), Some("""2.jpg""")) ::
      route("Action direct", "DE", "Frankenjura", "Krottenseer Forst", Grade(11), Some("""3.jpg""")) ::
      route("Wallstreet", "DE", "Frankenjura", "?", Grade(11, Some(Grade.GradeQualifier.Minus))) ::
      route("Chasin' the Trane", "DE", "Frankenjura", "Krottenseer Forst", Grade(9, Some(Grade.GradeQualifier.Plus))) ::
      route("Action direct", "IT", "Massone", "?", Grade(8)) ::
      route("The Man That Follows Hell", "DE", "Frankenjura", "sector1", Grade(8, Some(Grade.GradeQualifier.Plus))) ::
      Nil

    val dummyRoutes = createRandomDummies("DE") :::
      createRandomDummies("CH") :::
      createRandomDummies("AT") :::
      createRandomDummies("CZ") :::
      createRandomDummies("FR") :::
      createRandomDummies("IT")

    val testRoutes = realRoutes ::: dummyRoutes
//    val testRoutes = realRoutes

    for(tuple  <- testRoutes){
      val route = tuple._1
      val mongoRoute = Route.mongoFormat(route)

      if(route.hasImage){
        val file = new File(tuple._2.get)
        val fileSize = file.length
        val stream = new FileInputStream(file)
        val barr = new Array[Byte](fileSize.toInt)
        stream.read(barr)

        mongoRoute += ("image" -> barr)
      }

      routesCollection += mongoRoute

    }

  }
}
