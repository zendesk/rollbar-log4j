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
 * Copyright 2017 Zendesk.com, Inc
 *
 */

package com.zendesk.rollbar;

import com.rollbar.notifier.config.Config;
import com.rollbar.notifier.config.ConfigBuilder;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import com.rollbar.notifier.Rollbar;

import static com.rollbar.api.payload.data.Level.CRITICAL;
import static org.apache.log4j.Level.*;


public class RollbarLog4jAppender extends AppenderSkeleton {

  private String accessToken;
  private String environment;
  private String url;
  private com.rollbar.notifier.Rollbar client;

  @Override
  public synchronized void activateOptions() {
    super.activateOptions();
    if (this.accessToken != null && !this.accessToken.isEmpty() && this.environment != null
        && !this.environment.isEmpty()) {

      Config config;
      if(url != null && !url.isEmpty()) {
        config = ConfigBuilder.withAccessToken(this.accessToken)
                  .environment(this.environment)
                  .endpoint(this.url)
                  .build();
      }
      else {
        config = ConfigBuilder.withAccessToken(this.accessToken)
                .environment(this.environment)
                .build();
      }
      this.client = Rollbar.init(config);
    }
  }

  public void close() {

  }

  public boolean requiresLayout() {
    return false;
  }

  @Override
  protected void append(LoggingEvent event) {

    com.rollbar.api.payload.data.Level rollbarLevel;

    if (this.client == null || this.accessToken == null || this.environment == null) {
      LogLog.error("Rollbar client is not configured");
    }

    if (event.getLevel() == INFO) {
      rollbarLevel = com.rollbar.api.payload.data.Level.INFO;
    } else if (event.getLevel() == TRACE
        || event.getLevel() == DEBUG) {
      rollbarLevel = com.rollbar.api.payload.data.Level.DEBUG;
    } else if (event.getLevel() == WARN) {
      rollbarLevel = com.rollbar.api.payload.data.Level.WARNING;
    } else if (event.getLevel() == ERROR) {
      rollbarLevel = com.rollbar.api.payload.data.Level.ERROR;
    } else if (event.getLevel() == FATAL) {
      rollbarLevel = com.rollbar.api.payload.data.Level.CRITICAL;
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
    this.environment = environment; }

  public String getUrl() {
    return this.url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAccessToken() {
    return this.accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}
