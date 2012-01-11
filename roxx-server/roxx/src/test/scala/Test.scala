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

// TODO Delete as soon as SBT's REPL for 2.9 is fixed to show input again!
object Test {

  def main(args: Array[String]) {
    import com.mongodb.casbah.Imports._
    val routes = MongoConnection()("roxx")("routes")
//    routes.dropCollection()
//    routes += Route(System.currentTimeMillis.toString, Grade(8, GradeQualifier.Plus))
    val mongoObject = routes.head
    println(mongoObject)
    println(Route fromMongoDbObject mongoObject)
  }
}
