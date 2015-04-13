# Graylog Logstash forwarder Lumberjack Input Plugin

This plugin provides support for [logstash-forwarder](https://github.com/elastic/logstash-forwarder) lumberjack protocol.

* Support for reading from files
* Compressed frames support
* TLS/SSL for secure transport

Getting started
---------------

This project is using Maven 3 and requires Java 7 or higher. The plugin will require Graylog 1.0.0 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated JAR file in target directory to your Graylog plugin directory.
* Restart the Graylog.
* Input configuration requires following parameters
    * Keystore path - Absolute path of keystore in JKS format. To convert openssl keystore to JKS format refer to
     this [link] (http://www.cloudera.com/content/cloudera/en/documentation/core/v5-2-x/topics/cm_sg_openssl_jks.html)
    * Keystore password
    * Key Password


