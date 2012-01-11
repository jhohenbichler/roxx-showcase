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

import akka.actor.Actor
import akka.config.Supervision.OneForOneStrategy
import akka.event.EventHandler
import akka.http.{ Endpoint, RootEndpoint }

class RoxxEndpoint extends Actor with Endpoint {

  self.dispatcher = Endpoint.Dispatcher

  self.faultHandler = OneForOneStrategy(List(classOf[Exception]), 3, 100)

  private lazy val roxxActor = self.spawnLink[RoxxActor]

  override def preStart {
    val root = Actor.registry.actorsFor(classOf[RootEndpoint]).headOption
    if (root.isEmpty)
      EventHandler.error(this, "No RootEndpoint actor registerd!")
    else
      root.get ! Endpoint.Attach(hook, provide)
  }

  override protected def receive = handleHttpRequest

  private def hook(uri: String) = uri startsWith "/api"

  private def provide(uri: String) = roxxActor
}
