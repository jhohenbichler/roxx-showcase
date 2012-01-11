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
package util

import akka.http.{ Delete => AkkaDelete, Get => AkkaGet, Post => AkkaPost, Put => AkkaPut, RequestMethod}
import scalaz.Scalaz._

package object rest {

  /**
   * Base for extractors for HTTP requests.
   */
  sealed trait HttpVerb[A <: RequestMethod] {
    def unapplySeq(requestMethod: A): Option[Seq[String]] = {
      require(requestMethod != null, "requestMethod must not be null!")
      Some(parsePath(requestMethod))
    }
  }

  /**
   * Extractor for DELETE requests.
   */
  object Delete extends HttpVerb[AkkaDelete]

  /**
   * Extractor for GET requests.
   */
  object Get extends HttpVerb[AkkaGet]

  /**
   * Extractor for POST requests.
   */
  object Post extends HttpVerb[AkkaPost]

  /**
   * Extractor for PUT requests.
   */
  object Put extends HttpVerb[AkkaPut]

  private def parsePath(requestMethod: RequestMethod): Seq[String] = {
    val pathInfo = Option(requestMethod.request.getPathInfo) getOrElse "/"
    pathInfo dropWhile { _ === '/' } split "/" match {
      case Array() => Nil
      case path => path.tail // Because pathInfo contains (!!) the servletPath
    }
  }
}
