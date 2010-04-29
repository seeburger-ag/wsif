Sample service that retrieves a 20-min delayed quote for a ticker symbol
(taken from ApacheSOAP 2.2).


To install this service on an Apache-SOAP listener, you need to make
the services.stockquote package available on the Apache-SOAP listener's
classpath. Then deploy this service by filling in the deployment
template using the info in the deployment descriptor in this
directory or by using the service manager client:
  java org.apache.soap.server.ServiceManagerClient routerURL deploy DeploymentDescriptor.xml
where routerURL is the URL of the SOAP RPC router and dd.xml is the
name of the deployment descriptor file.  For example:
  java org.apache.soap.server.ServiceManagerClient  \
    http://localhost:8080/soap/servlet/rpcrouter deploy DeploymentDescriptor.xml

For more documentation see README.html in top level directory.
