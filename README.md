## Overview

This library implements log4j and log4j2 appenders that send logs to [Rollbar](https://rollbar.com/) (an error tracking service). This library is designed to be a light weight and dependency free jar that you add to a pre-existing java service at runtime.

## Usage

0. Register for a Rollbar.com account, create your project, and generate an access token.
1. Download the jar locally and add it to your target application's classpath.
2. Add `rollbar` to your log4j properties or xml file.

Example log4j config:
```
log4j.rootLogger=CONSOLE, ROLLBAR
log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender
log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout
log4j.appender.CONSOLE.layout.ConversionPattern=[%d{MMM dd HH:mm:ss}] %-5p (%F:%L) - %m%n
log4j.appender.ROLLBAR=com.nextdoor.rollbar.RollbarLog4jAppender
log4j.appender.ROLLBAR.AccessToken=example_token
log4j.appender.ROLLBAR.Environment=production
log4j.appender.ROLLBAR.layout=org.apache.log4j.PatternLayout
log4j.appender.ROLLBAR.Threshold=ERROR
```

Example log4j2 config:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration packages="com.nextdoor.rollbar">
  <Appenders>
    <Console name="Console">
      <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
    </Console>
    <Rollbar name="Rollbar">
      <accessToken>example_token</accessToken>
      <environment>production</environment>
      <ThresholdFilter level="ERROR" onMatch="ACCEPT" onMismatch="DENY"/>
    </Rollbar>
  </Appenders>
  <Loggers>
    <Root level="trace">
      <AppenderRef ref="Console"/>
      <AppenderRef ref="Rollbar"/>
    </Root>
  </Loggers>
</Configuration>
```