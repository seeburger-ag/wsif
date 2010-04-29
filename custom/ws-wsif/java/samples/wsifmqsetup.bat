rem ------------------------------------------------------------
rem Batch file that configures MQ Series for the WSIF samples.
rem -------------------------------------------------------------

@echo off

echo + Creating script for object creation within MQ

echo DEFINE QMODEL('WSIFSampleModel') DEFTYPE(PERMDYN) REPLACE > wsifmqsetup.mqs
echo DEFINE QLOCAL('SoapJmsAddressBookQueue') REPLACE >> wsifmqsetup.mqs
echo DEFINE QLOCAL('SoapJmsStockquoteQueue') REPLACE >> wsifmqsetup.mqs
echo DEFINE QLOCAL('NativeJmsAddressBookQueue') REPLACE >> wsifmqsetup.mqs
echo DEFINE QLOCAL('NativeJmsAddressBookQueueResponse') REPLACE >> wsifmqsetup.mqs

echo DEFINE QLOCAL('JmsStockQuoteQ') REPLACE >> wsifmqsetup.mqs
echo DEFINE QLOCAL('JmsStockQuoteQResponse') REPLACE >> wsifmqsetup.mqs

echo DEFINE QLOCAL('WSIFQ') REPLACE >> wsifmqsetup.mqs
echo DEFINE QLOCAL('WSIFQResponse') REPLACE >> wsifmqsetup.mqs

echo + Calling runmqsc in batch mode to create objects
@REM w/o queue manger - use system default queuemanager
runmqsc < wsifmqsetup.mqs 

echo + Administration done; tidying up files
del wsifmqsetup.mqs