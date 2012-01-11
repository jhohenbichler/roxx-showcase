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

import akka.actor.Actor
import akka.event.EventHandler
import com.weiglewilczek.slf4s.Logger

class Slf4sEventHandler extends Actor {
  import EventHandler._

  self.dispatcher = EventHandlerDispatcher

  override protected def receive = {
    case Error(cause, instance, message) =>
      Logger(instance.getClass).error(message.toString, cause)
    case Warning(instance, message) =>
      Logger(instance.getClass).warn(message.toString)
    case Info(instance, message) =>
      Logger(instance.getClass).info(message.toString)
    case Debug(instance, message) =>
      Logger(instance.getClass).debug(message.toString)
  }
}
