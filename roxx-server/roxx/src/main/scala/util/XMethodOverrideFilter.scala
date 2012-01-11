

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

import javax.servlet.{ Filter, FilterChain, FilterConfig, ServletRequest, ServletResponse }
import javax.servlet.http.{ HttpServletRequest, HttpServletRequestWrapper }

class XMethodOverrideFilter extends Filter {

  def init(config: FilterConfig) {
    //nothing
  }

  def doFilter(request: ServletRequest, response:ServletResponse , chain: FilterChain) {
    request match {
      case httpReq: HttpServletRequest =>
        chain.doFilter(new MethodOverrideHttpServletRequest(httpReq), response)
      case _ => 
        chain.doFilter(request, response)
    }
  }

  def destroy() {
    // nothing
  }
}

class MethodOverrideHttpServletRequest(request: HttpServletRequest)
    extends HttpServletRequestWrapper(request) {

  override def getMethod: String =
    Option(getHeader("X-HTTP-Method-Override")) getOrElse super.getMethod
}
