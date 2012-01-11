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

import dispatch._
import dispatch.json._
import Http._
import sjson.json.JsonSerialization
import org.apache.http.HttpStatus


class RestSpec extends Specification { def is =

  sequential  ^
  "This is a specification to unit-test the basic CRUD operations via the rest api"    ^
  p ^
  "CRUD a route"                                                               ^
  "create"                                                                   ! create ^
  "create with an invalid mongo id should fail"                              ! createWithIdIvalidMongoShouldFail ^
  "create with an valid mongo id should fail"                                ! createWithIdValidFormedMongoShouldFail ^
  "read"                                                                     ! read ^
  "update"                                                                   ! update ^
  "read again"                                                               ! readAgain ^
  "find by mogoID"                                                           ! findByMongoID   ^
  "delete"                                                                   ! delete ^
  "create and read a route without qualifier"                                ! createReadWithoutQualifier ^
  "update a route without id should throw an exception"                      ! updateWithoutIdThrowException ^
  "update a route without id should fail"                                    ! updateWithoutId

  val testRouteWithoutId = Route("name", "DE", "region1", "sector1", Grade(8, Some(Grade.GradeQualifier.Plus)))

  //TODO 07.04.2011 hohenbichler: implement
//  val testRouteWithId = Route("name", "DE", "region1", "sector1", Grade(8, Some(Grade.GradeQualifier.Plus)))

  def basicStatusCodeTests = {
    Http("http://localhost:8080/api/NOT_EXISTING_API_PATH" as_str) must throwA[StatusCode].like {
        case e:StatusCode => e.code must_== HttpStatus.SC_METHOD_NOT_ALLOWED
    }
  }

  def create = {
    val route = Route("rest create test 2", "DE", "region1", "sector1", Grade(8, Some(Grade.GradeQualifier.Plus)))
    val json = JsonSerialization tojson route
    val responseStr = Http("http://localhost:8080/api/routes" << json.toString as_str).toString
    (responseStr must contain ("id")) and
      (responseStr.length must_==  35) // a mongo id has a fixed lenghth. Example: {"id" : "4dbeac9c16887ae07105817d"}
  }

  def createWithIdIvalidMongoShouldFail = {
    val routeWithId = Route("name", "DE", "region1", "sector1", Grade(8, Some(Grade.GradeQualifier.Plus)), Option("invalid id .nwfownoihwe"))

    val json = JsonSerialization tojson routeWithId
    Http("http://localhost:8080/api/routes" << json.toString as_str) must throwA[StatusCode].like {
        case e:StatusCode => (e.code must_== HttpStatus.SC_BAD_REQUEST) and (e.getMessage must contain("Exceptional response code: 400"))
    }
  }

  def createWithIdValidFormedMongoShouldFail = {
    //TODO 08.04.2011 hohenbichler: FIXME: currently that is working - assert on serverside that an create-statement has an no ID in the request
    val routeWithId = Route("name", "DE", "region1", "sector1", Grade(8, Some(Grade.GradeQualifier.Plus)), Option("4d9f10fb43667288966222ce"))

    val json = JsonSerialization tojson routeWithId
    Http("http://localhost:8080/api/routes" << json.toString as_str) must throwA[StatusCode].like {
        case e:StatusCode => (e.code must_== HttpStatus.SC_BAD_REQUEST) and (e.getMessage must contain ("illegal argument: create does not allow routes with set id"))
    }
  }.pendingUntilFixed

  def read = {
    val http = new Http
    //    println("using >>>: ")
    //    http("http://localhost:8080/api/routes" >>> System.out )
    //
    //    println("using >~: ")
    //    http("http://localhost:8080/api/routes" >~ { _.getLines.foreach(println) }  )

    //TODO 08.04.2011 hohenbichler: read the route by ID that has been created in the create-test

    // note: if you dont prefix with "http://" you get following runtime errror: Host name may not be null
    val strResult = http("http://localhost:8080/api/routes" as_str).toString
    val routes = JsonSerialization.fromjson[List[Route]](Js(strResult))

    // note: result is sth linke that: [{"sector" : "s1", "name" : "22323zzz", "region" : "r1", "id" : "4d9dc3f7381b17fd6347fefa", "country" : "c1", "grade" : {"value" : 1, "qualifier" : {"id" : 1}}}
    // note: if you dont prefix with "http://" you get following runtime errror: Host name may not be null
//    val strResult = http("http://localhost:8080/api/routes" as_str)
//
//    val json = JsonSerialization tojson strResult
//    JsonSerialization.fromjson[List[Route]](json).filter(_.toStrin)
//
//
//

    //TODO 08.04.2011 hohenbichler: check that the read route exactly matches the route created in the create test
    routes.size must_!= 0
  }

  def update = {
    0 must_== 1
  }.pendingUntilFixed

  def readAgain = {
    0 must_== 1
  }.pendingUntilFixed

  def findByMongoID = {
    0 must_== 1
  }.pendingUntilFixed

  def delete = {
    0 must_== 1
  }.pendingUntilFixed

  def createReadWithoutQualifier = {
    0 must_== 1
  }.pendingUntilFixed

  def updateWithoutIdThrowException = {
    0 must_== 1
  }.pendingUntilFixed

  def updateWithoutId = {
    0 must_== 1
  }.pendingUntilFixed


}
