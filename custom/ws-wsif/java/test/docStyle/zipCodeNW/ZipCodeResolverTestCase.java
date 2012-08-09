/**
 * ZipCodeResolverTestCase.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package docStyle.zipCodeNW;

public class ZipCodeResolverTestCase extends junit.framework.TestCase {
    public ZipCodeResolverTestCase(java.lang.String name) {
        super(name);
    }
    public void test1ZipCodeResolverSoapShortZipCode() {
        docStyle.zipCodeNW.ZipCodeResolverSoap binding;
        try {
            binding = new docStyle.zipCodeNW.ZipCodeResolverLocator().getZipCodeResolverSoap();
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }
        assertTrue("binding is null", binding != null);

        try {
            docStyle.zipCodeNW.ShortZipCode zc = new docStyle.zipCodeNW.ShortZipCode();
            zc.setAccessCode("9999");
            zc.setAddress("607 Trinity");
            zc.setCity("Austin");
            zc.setState("TX");

            docStyle.zipCodeNW.ShortZipCodeResponse value = null;
            value = binding.shortZipCode(zc);

            System.out.println("zipcode=" + value.getShortZipCodeResult());

        }
        catch (java.rmi.RemoteException re) {
            throw new junit.framework.AssertionFailedError("Remote Exception caught: " + re);
        }
    }

    public static void main(String[] args) {
   
        ZipCodeResolverTestCase t = new ZipCodeResolverTestCase("KJH");
        t.test1ZipCodeResolverSoapShortZipCode();
    	
    }

}
