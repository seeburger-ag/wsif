rem ------------------------------------------------------------
rem Batch file that configures JNDI for the JMS definitions for 
rem the WSIF samples.
rem -------------------------------------------------------------

@echo off

echo + Creating script for object creation within JMSAdmin
echo def qcf(WSIFSampleQCF) tempmodel(WSIFSampleModel) > wsifjmssetup.scp
echo def q(SoapJmsAddressBookQueue) qu(SoapJmsAddressBookQueue) >> wsifjmssetup.scp
echo def q(NativeJmsAddressBookQueue) qu(NativeJmsAddressBookQueue) >> wsifjmssetup.scp
echo def q(NativeJmsAddressBookQueueResponse) qu(NativeJmsAddressBookQueueResponse) >> wsifjmssetup.scp


echo def q(SoapJmsStockquoteQueue) qu(SoapJmsStockquoteQueue) >> wsifjmssetup.scp
echo def q(JmsStockQuoteQueue) queue(JmsStockQuoteQ) >> wsifjmssetup.scp
echo def q(JmsStockQuoteQueueResponse) queue(JmsStockQuoteQResponse) >> wsifjmssetup.scp

echo def q(NativeJmsQueue) queue(WSIFQ) >> wsifjmssetup.scp
echo def q(NativeJmsQueueResponse) queue(WSIFQResponse) >> wsifjmssetup.scp

echo end >> wsifjmssetup.scp

echo + Calling JMSAdmin in batch mode to create objects
java -DMQJMS_LOG_DIR="%MQ_JAVA_INSTALL_PATH%"\log -DMQJMS_TRACE_DIR="%MQ_JAVA_INSTALL_PATH%"\trace -DMQJMS_INSTALL_PATH="%MQ_JAVA_INSTALL_PATH%" com.ibm.mq.jms.admin.JMSAdmin < wsifjmssetup.scp

echo + Administration done; tidying up files
del wsifjmssetup.scp

echo + Done!