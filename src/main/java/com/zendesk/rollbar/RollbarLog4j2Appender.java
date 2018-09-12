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
 * Copyright 2018 Zendesk.com, Inc
 * Original Copyright 2017 Nextdoor.com, Inc
 *
 */

package com.zendesk.rollbar;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.rollbar.api.payload.data.Server;
import com.rollbar.notifier.config.Config;
import com.rollbar.notifier.config.ConfigBuilder;
import com.rollbar.notifier.provider.Provider;
import com.rollbar.notifier.provider.server.ServerProvider;
import org.apache.log4j.helpers.LogLog;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.*;
import java.net.*;

import com.rollbar.notifier.Rollbar;

import static org.apache.logging.log4j.Level.*;


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
      @PluginAttribute("url") String url,
      @PluginAttribute("environment") String environment
      @PluginAttribute("hostName") String hostName ) {

    try {
      InetAddress ip = InetAddress.getByName(hostName);
    }catch(Exception e){
      LogLog.error("Invalid hostName");
    }
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


    Config config;
    if (url != null && !url.isEmpty()) {
      config = ConfigBuilder.withAccessToken(accessToken)
              .environment(environment)
              .endpoint(url)
              .build();
    }
    else {
      config = ConfigBuilder.withAccessToken(accessToken)
              .environment(environment)
              .build();
    }

    Rollbar rollbar = com.rollbar.notifier.Rollbar.init(config);
    return new RollbarLog4j2Appender(name, filter, layout, true, rollbar);
  }

  public void append(LogEvent event) {
    com.rollbar.api.payload.data.Level rollbarLevel;

    try {
      InetAddress ip = InetAddress.getByName(hostName);
    }catch(Exception e){
      LogLog.error("Invalid hostName");
    }

    Map<String, Object> custom = new HashMap<String,Object>();
    custom.put("hostName",hostName);

    if (this.client == null) {
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

    if (event.getThrown() != null) {
      if (event.getMessage().toString() != null) {
        this.client.log(event.getThrown(), custom, event.getMessage().getFormattedMessage(), rollbarLevel);
      } else {
        this.client.log(event.getThrown(), custom, rollbarLevel);
      }

    } else {
      this.client.log(custom, event.getMessage().getFormattedMessage(), rollbarLevel);
    }
  }
}
