rem ------------------------------------------------------------
rem Batch file that unconfigures MQ Series for the WSIF samples.
rem -------------------------------------------------------------

@echo off

echo + Creating script for object deletion within MQ

echo DELETE QMODEL('WSIFSampleModel') > wsifmqtidy.mqs
echo DELETE QLOCAL('SoapJmsAddressBookQueue') > wsifmqtidy.mqs
echo DELETE QLOCAL('SoapJmsStockquoteQueue') >> wsifmqtidy.mqs
echo DELETE QLOCAL('NativeJmsAddressBookQueue') >> wsifmqtidy.mqs
echo DELETE QLOCAL('NativeJmsAddressBookQueueResponse') >> wsifmqtidy.mqs

echo DELETE QLOCAL('JmsStockQuoteQ') >> wsifmqtidy.mqs
echo DELETE QLOCAL('JmsStockQuoteQResponse') >> wsifmqtidy.mqs

echo DELETE QLOCAL('WSIFQ') >> wsifmqtidy.mqs
echo DELETE QLOCAL('WSIFQResponse') >> wsifmqtidy.mqs

echo + Calling runmqsc in batch mode to create objects
@REM w/o queue manger - use system default queuemanager
runmqsc < wsifmqtidy.mqs 

echo + Administration done; tidying up files
del wsifmqtidy.mqs