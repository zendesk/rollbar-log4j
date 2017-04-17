/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * Copyright 2017 Nextdoor.com, Inc
 *
 */

package com.nextdoor.rollbar;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import com.rollbar.Rollbar;
import com.rollbar.payload.data.Server;

public class RollbarLog4jAppender extends AppenderSkeleton {

  private String accessToken;
  private String environment;
  private Rollbar client;

  @Override
  public synchronized void activateOptions() {
    super.activateOptions();
    if (this.accessToken != null && !this.accessToken.isEmpty() && this.environment != null
        && !this.environment.isEmpty()) {

      Server thisNode = null;
      try {
        thisNode = new Server().host(InetAddress.getLocalHost().getHostName());
      } catch (UnknownHostException | IllegalArgumentException e) {
        LogLog.error("unable to get hostname", e);
      }

      if (thisNode != null) {
        this.client = new Rollbar(this.accessToken, this.environment).server(thisNode);
      } else {
        this.client = new Rollbar(this.accessToken, this.environment);
      }
    }
  }

  public void close() {

  }

  public boolean requiresLayout() {
    return false;
  }

  @Override
  protected void append(LoggingEvent event) {

    com.rollbar.payload.data.Level rollbarLevel = null;

    if (this.client == null || this.accessToken == null || this.environment == null) {
      LogLog.error("Rollbar client is not configured");
    }

    if (event.getLevel() == org.apache.log4j.Level.INFO) {
      rollbarLevel = com.rollbar.payload.data.Level.INFO;
    } else if (event.getLevel() == org.apache.log4j.Level.TRACE
        || event.getLevel() == org.apache.log4j.Level.DEBUG) {
      rollbarLevel = com.rollbar.payload.data.Level.DEBUG;
    } else if (event.getLevel() == org.apache.log4j.Level.WARN) {
      rollbarLevel = com.rollbar.payload.data.Level.WARNING;
    } else if (event.getLevel() == org.apache.log4j.Level.ERROR) {
      rollbarLevel = com.rollbar.payload.data.Level.ERROR;
    } else if (event.getLevel() == org.apache.log4j.Level.FATAL) {
      rollbarLevel = com.rollbar.payload.data.Level.CRITICAL;
    } else {
      return;
    }

    if (event.getThrowableInformation() != null
        && event.getThrowableInformation().getThrowable() != null) {
      if (event.getMessage().toString() != null) {
        this.client.log(event.getThrowableInformation().getThrowable(), event.getRenderedMessage(),
            rollbarLevel);
      } else {
        this.client.log(event.getThrowableInformation().getThrowable(), rollbarLevel);
      }

    } else {
      this.client.log(event.getRenderedMessage(), rollbarLevel);
    }
  }

  public String getEnvironment() {
    return this.environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public String getAccessToken() {
    return this.accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}
