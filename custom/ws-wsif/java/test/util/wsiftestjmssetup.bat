rem ------------------------------------------------------------
rem Batch file that configures JNDI for the JMS definitions for 
rem the WSIF tests.
rem
rem YOU ALSO NEED TO RUN THE WSIF SAMPLE JMS SETUP BATCH FILE!!!
rem -------------------------------------------------------------

@echo off

echo + Creating script for object creation within JMSAdmin

echo def qcf(TempQCF) > wsiftestjmssetup.scp
echo def q(AddressBookReplyTo) qu(AddressBookReplyTo) >> wsiftestjmssetup.scp

rem these should match the nativejms properties in wsif.test.properties 
echo def q(NativeJmsRequestQueue) queue(NativeJmsRequestQueue) >> wsiftestjmssetup.scp
echo def q(NativeJmsResponseQueue) queue(NativeJmsResponseQueue) >> wsiftestjmssetup.scp

rem this should match the wsif.async.replytoq value in wsif.test.properties 
echo def q(AsyncReplyTo) qu(AsyncReplyTo) >> wsiftestjmssetup.scp
echo def q(AsyncReplyTo2) qu(AsyncReplyTo2) >> wsiftestjmssetup.scp

echo end >> wsiftestjmssetup.scp

echo + Calling JMSAdmin in batch mode to create objects
java -DMQJMS_LOG_DIR="%MQ_JAVA_INSTALL_PATH%"\log -DMQJMS_TRACE_DIR="%MQ_JAVA_INSTALL_PATH%"\trace -DMQJMS_INSTALL_PATH="%MQ_JAVA_INSTALL_PATH%" com.ibm.mq.jms.admin.JMSAdmin < wsiftestjmssetup.scp

echo + Administration done; tidying up files
del wsiftestjmssetup.scp

echo + Done!