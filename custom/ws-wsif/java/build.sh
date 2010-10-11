#!/bin/sh
# $Id: build.sh,v 1.1 2010/04/29 08:36:30 paskalev Exp $

# You can set JAVA_HOME to point ot JDK 1.3 
# or shell will try to deterine java location using which
# IMPPORTANT:  and make sure that JAVA_HOME\lib contains tools.jar !!!!
#
# written by Aleksander Slominski 


# 
# No need to modify anything after this line.
# --------------------------------------------------------------------

if [ -z "$JAVA_HOME" ] ; then
  JAVA=`/usr/bin/which java`
  if [ -z "$JAVA" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BIN=`dirname $JAVA`
  JAVA_HOME=$JAVA_BIN/..
else
  JAVA=$JAVA_HOME/bin/java
fi

echo "JAVA=$JAVA"

LOCALCLASSPATH=`/bin/sh $PWD/classpath.sh build`

CMD="$JAVA $OPTS -classpath $LOCALCLASSPATH org.apache.tools.ant.Main $@ -buildfile build.xml"
echo $CMD
$CMD

