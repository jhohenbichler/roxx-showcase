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

import _root_.util.Properties
import util.rest.{ Delete, Get, Message, Post, Put }
import akka.actor.Actor
import akka.event.EventHandler
import akka.http.RequestMethod
import com.mongodb.casbah.Imports._
import dispatch.json.{ Js, JsObject, JsString }
import java.io.ByteArrayOutputStream
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.io.IOUtils
import org.bson.types.ObjectId
import sjson.json.JsonSerialization
import java.io.{InputStream, ByteArrayOutputStream}
import java.net.{UnknownServiceException, URL}

object RoxxActor {
  private val mongoConfigFile = "/mongoconfig.properties"
  private val properties = Properties(mongoConfigFile)
}

class RoxxActor extends Actor {
  import RoxxActor._

  private val roxxDb = {

    val url = properties("mongo.db.url") getOrElse sys.error("missing property mongo.db.url in file " + mongoConfigFile)
    val port = properties("mongo.db.port") getOrElse sys.error("missing property mongo.db.port in file " + mongoConfigFile)
    val user = properties("mongo.db.user") getOrElse sys.error("missing property mongo.db.user in file " + mongoConfigFile)
    val pw = properties("mongo.db.pw") getOrElse sys.error("missing property mongo.db.pw in file " + mongoConfigFile)

    val db = MongoConnection(url, port.toInt).getDB("roxx")

    if(user != ""){
      val auth = db.authenticate(user, pw)
      if(auth == false) EventHandler.error(this, "MongoDB authenticaiton was not successful. Check your credntials!")
    }

    db
  }


  private val routesCollection = roxxDb("routes")

  private val upload = new ServletFileUpload(new DiskFileItemFactory)

  override protected def receive = {
    case post @ Post("routes") =>
      postOrPutRoute(post)
    case put @ Put("routes") =>
      postOrPutRoute(put)
    case get @ Get("routes") =>
      getRoutes(get)
    case get @ Get("route", id) =>
      getRoute(get, id)
    case get @ Get("route", routeId, "image") =>
      getRouteImage(get, routeId)
    case delete @ Delete("route", id) =>
      deleteRoute(delete, id)
    case other: RequestMethod =>
      other.NotAllowed(Message("Unsupported request: %s" format other.request.getRequestURI))
  }


  private def postOrPutRoute(requestMethod: RequestMethod) {
    EventHandler.debug(this, "postRoute called")

    try {
      def default(any: Any): String = "false";
      val doNotChangeImage = requestMethod.getHeaderOrElse("doNotChangeImage", default) match { case "true" => true
                                                                                                case _ => false };
      EventHandler.info(this, "doNotChangeImage: " + doNotChangeImage)

      val (json, image): (Option[InputStream], Option[InputStream]) = {
        import scala.collection.JavaConversions._
        val items = {
          if(ServletFileUpload isMultipartContent requestMethod.request){
            EventHandler.info(this, "Found a multipart request")
            (upload parseRequest requestMethod.request map { item =>
              val fileItem = item.asInstanceOf[FileItem]
              fileItem.getFieldName -> fileItem.getInputStream
            }).toMap
          } else {
            EventHandler.info(this, "Found no multipart request")
            Map("json" -> requestMethod.request.getInputStream)
          }
        }
        (items get "json") -> (items get "image")
      }

      if (json.isEmpty) {
        EventHandler.error(this, "Required field json missing!")
        requestMethod.BadRequest(Message("Required field json missing!"))
      } else {
        val route: Route = {
          val routeFromJson = JsonSerialization.fromjson[Route](Js(json.get))
          if ((routeFromJson.id.isDefined && doNotChangeImage == true) || image.isDefined) routeFromJson.copy(hasImage = true)
          else routeFromJson
        }

        val mongoRoute = Route.mongoFormat(route)

        if((route.id.isDefined && doNotChangeImage == true)){
          // load the exisiting image for the route and save the route object with that exisiting image

          EventHandler.info(this, "Loading image for route because doNotChangeImage is true")
          
          (routesCollection findOne MongoDBObject("_id" -> new ObjectId(route.id.get))) flatMap {
            imageMongoDbObject => imageMongoDbObject.getAs[Array[Byte]]("image")
          } map { byteArray =>
            mongoRoute += ("image" -> byteArray)
            EventHandler.info(this, "Successfully loaded route image for route with id to keep it unchanged: " + route.id.get)
          } getOrElse {
            EventHandler.error(this, "Could not find route image for route with id: " + route.id.get)
            requestMethod.NotFound(Message("Could not find route image for route with id: " + route.id.get))
          }

        } else  {
          if (image.isDefined) {
            val bytes = new ByteArrayOutputStream
            IOUtils.copy(image.get, bytes)
            mongoRoute += ("image" -> bytes.toByteArray)
          }
        }

        // do the add/update
        routesCollection += mongoRoute

        val id = mongoRoute.getAs[ObjectId]("_id").get // We want an exception here, because that would be a Mongo error!
        EventHandler.info(this, "Successfully added or updated route with id: " + id)
        requestMethod.OK(JsObject(Map(JsString("id") -> JsString(id))).toString)
      }
    } catch {
      case e: Exception =>
        EventHandler.error(e, this, "Error adding route!")
        requestMethod.BadRequest(Message("Error adding route!"))
    }
  }

  private def getRoutes(requestMethod: RequestMethod) {
    EventHandler.debug(this, "getRoutes")
    val routes =
      routesCollection.find.toList flatMap {
        mongoDbObject => Route fromMongoDbObject mongoDbObject
      }
    requestMethod.OK(JsonSerialization tojson routes toString)
  }

  // Sample result: {"sector" : "sector1", "name" : "Corona", "region" : "Frankenjura", "id" : "4dbea8a416883539906f77aa", "country" : "DE", "grade" : {"value" : 11, "qualifier" : {"id" : 0}}, "hasImage" : true}
  private def getRoute(requestMethod: RequestMethod, id: String) {
    EventHandler.debug(this, "getRoute with id: "  + id)
    try {
      (routesCollection findOne MongoDBObject("_id" -> new ObjectId(id))) flatMap {
        mongoDbObject => Route fromMongoDbObject mongoDbObject
      } map {
        route => requestMethod.OK(JsonSerialization tojson route toString)
      } getOrElse {
        EventHandler.error(this, "Could not find route with id: " + id)
        requestMethod.NotFound(Message("Could not find route with id: " + id))
      }
    } catch {
      case e: IllegalArgumentException => {
        EventHandler.error(e, this, "Error getting route with id: " + id)
        requestMethod.BadRequest(Message("Error getting route with id: " + id))
      }
    }
  }

  private def getRouteImage(requestMethod: RequestMethod, routeId: String) {
    EventHandler.debug(this, "getRouteImage with routeId: "  + routeId)
    try {
      (routesCollection findOne MongoDBObject("_id" -> new ObjectId(routeId))) flatMap {
        mongoDbObject => mongoDbObject.getAs[Array[Byte]]("image")
      } map { imageStream =>
        requestMethod.response.setContentType("image")
        val os = requestMethod.response.getOutputStream
        os.write(imageStream)
        os.flush
        EventHandler.info(this, "Successfully loaded route image for route with id: " + routeId)
      } getOrElse {
        EventHandler.error(this, "Could not find route image for route with id: " + routeId)
        requestMethod.NotFound(Message("Could not find route image for route with id: " + routeId))
      }
    } catch {
      case e: IllegalArgumentException => {
        EventHandler.error(e, this, "Error getting route image for route with id: " + routeId)
        requestMethod.BadRequest(Message("Error getting route image for route with id: " + routeId))
      }
    }
  }

  private def deleteRoute(requestMethod: RequestMethod, id: String) {
    EventHandler.debug(this, "deleteRoute with id: "  + id)
    routesCollection findAndRemove MongoDBObject("_id" -> new ObjectId(id)) match {
      case Some(_) =>
        requestMethod.OK(JsObject(Map(JsString("id") -> JsString(id))).toString)
      case None =>
        EventHandler.error(this, "Could not find and delete route with id: " + id)
        requestMethod.NotFound(Message("Could not find and delete route with id: " + id))
    }
  }
}
