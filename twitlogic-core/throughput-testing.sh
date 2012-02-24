#!/bin/bash

# Find Java
if [ "$JAVA_HOME" = "" ] ; then
        JAVA="java"
else
        JAVA="$JAVA_HOME/bin/java"
fi

# Set Java options
if [ "$JAVA_OPTIONS" = "" ] ; then
        JAVA_OPTIONS="-Xms32M -Xmx512M -XX:+HeapDumpOnOutOfMemoryError"
fi

# Launch the application
$JAVA $JAVA_OPTIONS -cp target/classes:"target/dependency/*" net.fortytwo.twitlogic.util.misc.ThroughputTesting $*

# Return the program's exit code
exit $?

