Please see the readme file in the addressbook and stockquote subdirectories for
examples of stub usage.

In this directory we included a stubless invoker (clients.DynamicInvoker) that allows
the user to execute an operation on any service that is described in WSDL. This is
only an example and is limited to operations that only operate on primitive types.

Sample Usage:
------------

java clients.DynamicInvoker http://www.xmethods.net/sd/2001/TemperatureService.wsdl getTemp 10570


For more documentation see README.html in top level directory.
