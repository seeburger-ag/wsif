rem ------------------------------------------------------------
rem Batch file that unconfigures JNDI for the JMS definitions for 
rem the WSIF samples.
rem -------------------------------------------------------------

@echo off

echo + Creating script for object deletion within JMSAdmin
echo del qcf(WSIFSampleQCF) > wsifjmstidy.scp
echo del q(SoapJmsAddressBookQueue) >> wsifjmstidy.scp
echo del q(NativeJmsAddressBookQueue) >> wsifjmstidy.scp
echo del q(NativeJmsAddressBookQueueResponse) >> wsifjmstidy.scp

echo del q(SoapJmsStockquoteQueue) >> wsifjmstidy.scp
echo del q(JmsStockquoteQueue) >> wsifjmstidy.scp
echo del q(JmsStockquoteQueueResponse) >> wsifjmstidy.scp

echo del q(NativeJmsQueue) >> wsifjmstidy.scp
echo del q(NativeJmsQueueResponse) >> wsifjmstidy.scp
echo end >> wsifjmstidy.scp

echo + Calling JMSAdmin in batch mode to create objects
java -DMQJMS_LOG_DIR="%MQ_JAVA_INSTALL_PATH%"\log -DMQJMS_TRACE_DIR="%MQ_JAVA_INSTALL_PATH%"\trace -DMQJMS_INSTALL_PATH="%MQ_JAVA_INSTALL_PATH%" com.ibm.mq.jms.admin.JMSAdmin < wsifjmstidy.scp

echo + Administration done; tidying up files
del wsifjmstidy.scp

echo + Done!