/*
 *
 *  Copyright (c) 2015 University of Massachusetts
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you
 *  may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 *  Initial developer(s): Westy
 *
 */
package edu.umass.cs.gnsserver.installer;

import edu.umass.cs.gnscommon.exceptions.server.ServerException;

/**
 * This class defines a HostConfigParseException
 *
 * @author <a href="mailto:westy@cs.umass.edu">Westy</a>
 */
@Deprecated
public class HostConfigParseException extends ServerException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates a new <code>HostConfigParseException</code> object
   */
  public HostConfigParseException() {
    super();
  }

  /**
   * Creates a new <code>HostConfigParseException</code> object
   *
   * @param message
   * @param cause
   */
  public HostConfigParseException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a new <code>HostConfigParseException</code> object
   *
   * @param message
   */
  public HostConfigParseException(String message) {
    super(message);
  }

  /**
   * Creates a new <code>HostConfigParseException</code> object
   *
   * @param throwable
   */
  public HostConfigParseException(Throwable throwable) {
    super(throwable);
  }

}
