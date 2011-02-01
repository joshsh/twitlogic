# Build the plugin
mvn clean install

# Create TwitLogic configuration properties (including Twitter OAuth credentials)
cp example.properties /tmp/testing.properties
vim /tmp/testing.properties

# Run the stand-alone demo
java -cp target/twitlogic-larkc-plugin-0.1-full.jar net.fortytwo.twitlogic.larkc.TwitLogicPluginDemo
