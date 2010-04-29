rem ------------------------------------------------------------
rem Batch file that configures MQ Series for the WSIF tests.
rem
rem YOU ALSO NEED TO RUN THE WSIF SAMPLE MQ SETUP BATCH FILE!!!
rem -------------------------------------------------------------

@echo off

echo + Creating script for object creation within MQ

echo DEFINE QLOCAL('AddressBookReplyTo') REPLACE > wsiftestmqsetup.mqs

rem these should match the nativejms properties in wsif.test.properties 
echo DEFINE QLOCAL('NativeJmsRequestQueue') REPLACE >> wsiftestmqsetup.mqs
echo DEFINE QLOCAL('NativeJmsResponseQueue') REPLACE >> wsiftestmqsetup.mqs

rem this should match the wsif.async.replytoq value in wsif.test.properties 
echo DEFINE QLOCAL('AsyncReplyTo') REPLACE >> wsiftestmqsetup.mqs
echo DEFINE QLOCAL('AsyncReplyTo2') REPLACE >> wsiftestmqsetup.mqs

echo + Calling runmqsc in batch mode to create objects
@REM w/o queue manger - use system default queuemanager
runmqsc < wsiftestmqsetup.mqs 

echo + Administration done; tidying up files
del wsiftestmqsetup.mqs