This is a simple test client that accesses a delayed stockquote service.

Sample Usage:
------------

java clients.stockquote.Main <PortName> <WSDLLocation> [soap|axis]

where <PortName> is one of either SOAPPort or JavaPort
      <WSDLLocation> is the full path including filename of the Stockquote.wsdl file
      [soap|axis] an optional value of soap or axis to specify which provider to use
      
For example:

java clients.stockquote.Main SOAPPort java\samples\stockquote\wsifservice\Stockquote.wsdl soap


For more documentation see README.html in top level directory.
