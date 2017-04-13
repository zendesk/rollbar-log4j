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

import java.io.Serializable;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.rollbar.Rollbar;

@Plugin(name = "Rollbar", category = "Core", elementType = "appender", printObject = true)
public class RollbarLog4j2Appender extends AbstractAppender {
  private final Rollbar client;

  protected RollbarLog4j2Appender(String name, Filter filter, Layout<? extends Serializable> layout,
      boolean ignoreExceptions, Rollbar client) {
    super(name, filter, layout, ignoreExceptions);
    this.client = client;
  }

  @PluginFactory
  public static RollbarLog4j2Appender createAppender(@PluginAttribute("name") String name,
      @PluginElement("Layout") Layout<? extends Serializable> layout,
      @PluginElement("Filter") final Filter filter,
      @PluginAttribute("accessToken") String accessToken,
      @PluginAttribute("environment") String environment) {

    if (name == null) {
      LOGGER.error("No name provided for RollbarLog4j2Appender");
      return null;
    }

    if (accessToken == null || accessToken.isEmpty()) {
      LOGGER.error("'accessToken' must be set for RollbarLog4j2Appender");
      return null;
    }

    if (environment == null || environment.isEmpty()) {
      LOGGER.error("'environment' must be set for RollbarLog4j2Appender");
      return null;
    }

    if (layout == null) {
      layout = PatternLayout.createDefaultLayout();
    }

    Rollbar rollbar = new Rollbar(accessToken, environment);
    return new RollbarLog4j2Appender(name, filter, layout, true, rollbar);
  }

  public void append(LogEvent event) {
    com.rollbar.payload.data.Level rollbarLevel = null;

    if (event.getLevel() == org.apache.logging.log4j.Level.INFO) {
      rollbarLevel = com.rollbar.payload.data.Level.INFO;
    } else if (event.getLevel() == org.apache.logging.log4j.Level.TRACE
        || event.getLevel() == org.apache.logging.log4j.Level.DEBUG) {
      rollbarLevel = com.rollbar.payload.data.Level.DEBUG;
    } else if (event.getLevel() == org.apache.logging.log4j.Level.WARN) {
      rollbarLevel = com.rollbar.payload.data.Level.WARNING;
    } else if (event.getLevel() == org.apache.logging.log4j.Level.ERROR) {
      rollbarLevel = com.rollbar.payload.data.Level.ERROR;
    } else if (event.getLevel() == org.apache.logging.log4j.Level.FATAL) {
      rollbarLevel = com.rollbar.payload.data.Level.CRITICAL;
    } else {
      return;
    }

    if (event.getThrown() != null) {
      if (event.getMessage().toString() != null) {
        this.client.log(event.getThrown(), event.getMessage().getFormattedMessage(), rollbarLevel);
      } else {
        this.client.log(event.getThrown(), rollbarLevel);
      }

    } else {
      this.client.log(event.getMessage().getFormattedMessage(), rollbarLevel);
    }
  }
}
