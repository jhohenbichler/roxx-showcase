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
package rest

import dispatch.json.{ JsObject, JsString }

object Message {

  def apply(message: String): Message =
    new Message(message)

  implicit def messageToString(message: Message): String =
    message.toString
}

class Message(message: String) {

  override val toString: String =
    JsObject(Map(JsString("message") -> JsString(message))).toString
}
