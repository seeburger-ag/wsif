#!/bin/sh
#
# This script sets required LOCALCLASSPATH and by default CLASSPATH 
# if no arguments.Otherwise use "set" option to set CLASSPATH
# and use "quiet" option to suppress prinitng of messages
# It must be run by source its content to modify current environment
#    . classpath.sh [build|run] [set] [quiet]
#
# written by Aleksander Slominski [http://www.extreme.indiana.edu/~aslom]
LOCALCLASSPATH=.

if [ "$1" = "build" ] ; then 
    LOCALCLASSPATH=`echo lib/*.jar | tr ' ' ':'`:$LOCALCLASSPATH
    LOCALCLASSPATH=$JAVA_HOME/lib/tools.jar:$LOCALCLASSPATH
    if [ "$2" = "set" ] ; then
        CLASSPATH=$LOCALCLASSPATH
        if [ ! "$3" = "quiet" ] ; then
            echo $LOCALCLASSPATH
        fi
    elif [ ! "$2" = "quiet" ] ; then
        echo $LOCALCLASSPATH
    fi
else 
    # put all jars except ant.jar on classpath
    RUNTIME_JARS=`ls -1 lib/*.jar | egrep -v 'ant.*jar'`
    LOCALCLASSPATH=`echo $RUNTIME_JARS | tr ' ' ':'`:$LOCALCLASSPATH
    LOCALCLASSPATH=build/classes:build/samples:build/tests:$LOCALCLASSPATH
    if [ ! "`echo build/lib/*.jar`" = "build/lib/*.jar" ] ; then
       LOCALCLASSPATH=`echo build/lib/*.jar | tr ' ' ':'`:$LOCALCLASSPATH
    fi
    if [ "$1" = "run" ] ; then
        if [ "$2" = "set" ] ; then
            CLASSPATH=$LOCALCLASSPATH
            if [ ! "$3" = "quiet" ] ; then
                echo $LOCALCLASSPATH
            fi
        elif [ ! "$2" = "quiet" ] ; then
            echo $LOCALCLASSPATH
        fi
    else 
        CLASSPATH=$LOCALCLASSPATH
        if [ ! "$1" = "quiet" ] ; then
            echo $CLASSPATH
        fi
    fi
fi
export CLASSPATH

