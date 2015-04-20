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
    
How To
------

Following are the steps to integrate this plugin with Graylog server and index the files

### Copy the plugin to Graylog plugins directory
* Download the plugin [jar] (https://github.com/sivasamyk/graylog2-input-lumberjack/raw/master/graylog2-input-lumberjack-1.0.0-rc1.jar) and copy the jar to plugin directory in Graylog server installation

### Generate SSL certificates
* Generate SSL certificates to be used for transport using following command (for further information refer logstash-forwarder [documentation] (https://github.com/elastic/logstash-forwarder/))

     `openssl req -x509  -batch -nodes -newkey rsa:2048 -keyout lumberjack.key -out lumberjack.crt -subj /CN=<graylog-server-name>`
* Export these certificates 

     `openssl pkcs12 -export -in lumberjack.crt -inkey lumberjack.key -out lumberjack.p12 -name localhost -passin pass:<password> -passout pass:<store-pass>`
     
* Import the certificates using keytool. 

     `keytool -importkeystore -srckeystore lumberjack.p12 -srcstoretype PKCS12 -srcstorepass <store-pass> -alias <graylog-server-name> -deststorepass <keystore-pass> -destkeypass <key-pass> -destkeystore lumberjack.jks`
     
* The above commands will generate following files lumberjack.crt, lumberjack.key, lumberjack.jks
   
### Configure plugin in Graylog 
* Create new input of type "Logstash-forwarder Input" 
     
     ![Logstash-forwarder input configuration] (https://raw.githubusercontent.com/sivasamyk/graylog2-input-lumberjack/master/input-config.png)

### Create logstash-forwarder configuration file

### Start indexing the files
* Launch logstash-frowarder with -config option
* Create extractors to extract timestamp from message.




