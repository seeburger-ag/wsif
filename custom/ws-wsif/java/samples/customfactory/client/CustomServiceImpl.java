package customfactory.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.base.WSIFClientProxy;
import org.apache.wsif.base.WSIFDefaultMessage;
import org.apache.wsif.compiler.schema.tools.Schema2Java;
import org.apache.wsif.compiler.util.TypeMapping;
import org.apache.wsif.compiler.util.Utils;
import org.apache.wsif.logging.MessageLogger;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.spi.WSIFProvider;
import org.apache.wsif.util.WSIFPluggableProviders;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.wsdl.extensions.java.JavaBinding;
import org.w3c.dom.Element;

import com.ibm.wsdl.util.xml.QNameUtils;

/**
 * An entry point to dynamic WSDL invocations.
 *
 * @author Alekander Slominski
 * @author Sanjiva Weerawarana
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class CustomServiceImpl implements WSIFService {
    private static MyPrivateCompositeExtensionRegistry providersExtRegs =
        new MyPrivateCompositeExtensionRegistry();
    private Definition def = null;
    private Service service;
    private PortType portType;
    private Port[] myPortsArr;
    private Map myPortsMap;
    private WSIFDynamicTypeMap typeMap = new WSIFDynamicTypeMap();
    private boolean typeMapInitialised = false;
    private String preferredPort = null;
    private Map typeReg = null;
    private Port chosenPort = null;
    private WSIFMessage context;

    /**
     * Create a WSIF service instance from WSDL document URL.
     * <br> If serviceName or serviceNS is null,
     *   then WSDL document must have exactly one service in it.
     * <br> If portTypeName or portTypeNS is null,
     *   then WSDL document must have exactly one portType in it
     *   and all ports of the selected service must
     *    implement the same portType.
     * <br>NOTE: 
     * The equivalent {@link org.apache.wsif.WSIFServiceFactory}.getService method
     * should be used to create a WSIFService.
     */
    CustomServiceImpl(
        String wsdlLoc,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName)
        throws WSIFException {
        Trc.entry(this, wsdlLoc, serviceNS, serviceName, portTypeNS, portTypeName);

        // load WSDL defintion
        Definition def = null;
        try {
            def = WSIFUtils.readWSDL(null, wsdlLoc);
            checkWSDL(def);
        } catch (WSDLException ex) {
        	Trc.exception(ex);
            throw new WSIFException("could not load " + wsdlLoc, ex);
        }

        // select WSDL service if given name
        Service service = WSIFUtils.selectService(def, serviceNS, serviceName);

        // select WSDL portType if given name
        PortType portType = WSIFUtils.selectPortType(def, portTypeNS, portTypeName);

        init(def, service, portType);
        if (Trc.ON)
            Trc.exit(deep());
    }

    /**
      * Create a WSIF service instance from WSDL document URL
      * using a ClassLoader to find local resources.
      * <br> If serviceName or serviceNS is null,
      *   then WSDL document must have exactly one service in it.
      * <br> If portTypeName or portTypeNS is null,
      *   then WSDL document must have exactly one portType in it
      *   and all ports of the selected service must
      *    implement the same portType.
      * <br>NOTE: 
      * The equivalent {@link org.apache.wsif.WSIFServiceFactory}.getService method
      * should be used to create a WSIFService.
      */
    CustomServiceImpl(
        String wsdlLoc,
        ClassLoader cl,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName)
        throws WSIFException {
        Trc.entry(this, wsdlLoc, cl, serviceNS, serviceName, portTypeNS, portTypeName);

        // load WSDL defintion
        Definition def = null;
        try {
            def = WSIFUtils.readWSDL(null, wsdlLoc, cl);
            checkWSDL(def);
        } catch (WSDLException ex) {
        	Trc.exception(ex);
            throw new WSIFException("could not load " + wsdlLoc, ex);
        }

        // select WSDL service if given name
        Service service = WSIFUtils.selectService(def, serviceNS, serviceName);

        // select WSDL portType if given name
        PortType portType = WSIFUtils.selectPortType(def, portTypeNS, portTypeName);

        init(def, service, portType);
        if (Trc.ON)
            Trc.exit(deep());
    }

    /**
      * Create a WSIF service instance
      * <br>NOTE:
      * The equivalent {@link org.apache.wsif.WSIFServiceFactory}.getService method
      * should be used to create a WSIFService.
      */
    CustomServiceImpl(Definition def) throws WSIFException {
        this(def, null);
    }

    /**
      * Create a WSIF service instance
      * <br>NOTE: 
      * The equivalent {@link org.apache.wsif.WSIFServiceFactory}.getService method
      * should be used to create a WSIFService.
      */
    CustomServiceImpl(Definition def, Service service) throws WSIFException {
        this(def, service, null);
    }

    /**
      * Create a WSIF service instance
      * <br>NOTE: 
      * The equivalent {@link org.apache.wsif.WSIFServiceFactory}.getService method
      * should be used to create a WSIFService.
      */
    CustomServiceImpl(Definition def, Service service, PortType portType)
        throws WSIFException {
        Trc.entry(this, def, service, portType);

        init(def, service, portType);
        if (Trc.ON)
            Trc.exit(deep());
    }

    /**
      * Create a WSIF service instance
      * <br>NOTE: 
      * The equivalent {@link org.apache.wsif.WSIFServiceFactory}.getService method
      * should be used to create a WSIFService.
      */
    CustomServiceImpl(Definition def, String serviceNS, String serviceName)
        throws WSIFException {
        Trc.entry(this, def, serviceNS, serviceName);

        // select WSDL service if given by name or only one
        Service service = WSIFUtils.selectService(def, serviceNS, serviceName);
        init(def, service, null);
        if (Trc.ON)
            Trc.exit(deep());
    }

    /**
      * Create a WSIF service instance
      * <br>NOTE: 
      * The equivalent {@link org.apache.wsif.WSIFServiceFactory}.getService method
      * should be used to create a WSIFService.
      */
    CustomServiceImpl(
        Definition def,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName)
        throws WSIFException {
        Trc.entry(this, def, serviceNS, serviceName, portTypeNS, portTypeName);

        checkWSDLForWSIF(def);

        // select WSDL service if given by name or only one
        Service service = WSIFUtils.selectService(def, serviceNS, serviceName);

        // select WSDL portType if given by name or only one portType
        PortType portType = WSIFUtils.selectPortType(def, portTypeNS, portTypeName);

        init(def, service, portType);
        if (Trc.ON)
            Trc.exit(deep());
    }

    /**
      * Create a WSIF service instance from another instance. 
      */
    CustomServiceImpl(CustomServiceImpl wsi)
        throws WSIFException {
        Trc.entry(this, wsi);
        copyInitializedService(wsi);
        if (Trc.ON)
            Trc.exit(deep());
    }

	/**
	 * Copy the "read-only" parts of an initialized CustomServiceImpl
	 */
	private void copyInitializedService(CustomServiceImpl svc) {
		this.def = svc.def;
        this.service = svc.service;
        this.portType = svc.portType;
		this.myPortsArr = new Port[svc.myPortsArr.length];
		System.arraycopy(svc.myPortsArr, 0, this.myPortsArr, 0, svc.myPortsArr.length);
        this.myPortsMap = (Map) ((Hashtable) svc.myPortsMap).clone();
		this.typeMap = svc.typeMap.copy();
	}

    /**
     * Set the preferred port
     * @param portName The name of the port to use
     */
    public void setPreferredPort(String portName) throws WSIFException {
        Trc.entry(this, portName);

        if (portName == null) {
            throw new WSIFException("Preferred port name cannot be null");
        }
        PortType pt = getPortTypeFromPortName(portName);
        if (pt.getQName().equals(this.portType.getQName())) {
            this.preferredPort = portName;
        } else {
            throw new WSIFException(
                "Preferred port "
                    + portName
                    + "is not available for the port type "
                    + this.portType.getQName());
        }
        Trc.exit();
    }

    /**
     * Create a PortType object from the name of a port
     * @param portName The name of the port
     * @return A PortType corresponding to the port type used by the
     * specified port
     */
    private PortType getPortTypeFromPortName(String portName)
        throws WSIFException {
        if (portName == null) {
            throw new WSIFException("Unable to find port type from a null port name");
        }
        Port port = (Port) service.getPort(portName);
        if (port == null) {
            throw new WSIFException(
                "Port '" + portName + "' cannot be found in the service");
        }
        Binding binding = port.getBinding();
        if (binding == null) {
            throw new WSIFException("No binding found for port '" + portName + "'");
        }
        PortType pt = binding.getPortType();
        if (pt == null) {
            throw new WSIFException(
                "No port type found for binding '" + binding.getQName() + "'");
        }
        checkPortTypeInformation(def, pt);
        return pt;
    }

    /**
     * Get the names of the available ports
     * @return Iterator for list of available port names.
     */
    public Iterator getAvailablePortNames() throws WSIFException {
        Trc.entry(this);
        Iterator it = null;
        try {
            it = this.myPortsMap.keySet().iterator();
        } catch (NullPointerException ne) {
        	Trc.exception(ne);
            it = null;
        }
        Trc.exit();
        return it;
    }

    /**
     * Create dynamic port instance from WSDL model defnition and port.
     */
    private WSIFPort createDynamicWSIFPort(
        Definition def,
        Service service,
        Port port)
        throws WSIFException {
        checkWSDLForWSIF(def);
        List bindingExList = port.getBinding().getExtensibilityElements();
        ExtensibilityElement bindingFirstEx =
            (ExtensibilityElement) bindingExList.get(0);
        String bindingNS = bindingFirstEx.getElementType().getNamespaceURI();
        WSIFProvider provider = WSIFPluggableProviders.getProvider(bindingNS);
        if (provider != null) {
            return provider.createDynamicWSIFPort(def, service, port, typeMap);
        } else {
            throw new WSIFException(
                "could not find suitable provider for binding namespace '" + bindingNS + "'");
        }
    }

    public WSIFPort getPort() throws WSIFException {
	// ignore preferred port preference
	// check if the service has a java binding; if so use that port
	// since it will be faster than accessing a SOAP, EJB etc. implemenations of 
	// the same thing
	Definition definition = getDefinition();
	// get the first service (we assume there is exactly one)
	// if we have no services, no bindings or other screwy stuff
	// the code below will choke
	Map services = definition.getServices();
	Service service = (Service) services.values().iterator().next();
	Iterator ports = service.getPorts().values().iterator();
	while (ports.hasNext()) {
	    Port port = (Port) ports.next();
	    // check the binding
	    Binding binding = port.getBinding();
	    if (binding instanceof JavaBinding)
		return getPort(port.getName());
	}
	// no java binding available, just return the first port
	Port firstPort = (Port) service.getPorts().values().iterator().next();
	return getPort(firstPort.getName());
    }

    /**
     * Return dynamic port instance selected by port name.
     */
    public WSIFPort getPort(String portName) throws WSIFException {
        Trc.entry(this, portName);
        Port port = null;

        if (portName == null) {
            // Get first available port
            if (myPortsArr.length > 0) {
                port = myPortsArr[0];
            }
        } else {
            port = (Port) myPortsMap.get(portName);
        }
        if (port == null) {
            if (portName == null) {
                throw new WSIFException("Unable to find an available port");
            } else {
                throw new WSIFException(
                    "Port '"
                        + portName
                        + "' is not available and "
                        + " no alternative can be found");
            }
        }

        portName = port.getName();
        WSIFPort portInstance = createDynamicWSIFPort(def, service, port);
        if (portInstance == null) {
            throw new WSIFException(
                "Provider was unable to create WSIFPort for port " + portName);
        }
        // Store the chosen port so that we can query which was is being used
        chosenPort = port;

        Trc.exit(portInstance);
        return portInstance;
    }

    /**
     * Add association between XML and Java type.
     * @param xmlType The qualified xml name
     * @param javaType The Java class 
     */
    public void mapType(QName xmlType, Class javaType) throws WSIFException {
        Trc.entry(this, xmlType, javaType);
        typeMap.mapType(xmlType, javaType);
        Trc.exit();
    }

    /**
     * Add an association between XML and Java type.
     * @param xmlType The qualified xml name
     * @param javaType The Java class
     * @param force flag to indicate if mapping should override an existing one
     * for the same xmlType 
     */
    private void mapType(QName xmlType, Class javaType, boolean force)
        throws WSIFException {
        Trc.entry(this, xmlType, javaType, new Boolean(force));
        typeMap.mapType(xmlType, javaType, force);
        Trc.exit();
    }

    /**
     * Add an association between a namespace URI and and a Java package.
     * @param namespace The namespace URI
     * @param packageName The full package name
     */
    public void mapPackage(String namespace, String packageName)
        throws WSIFException {
        Trc.entry(namespace, packageName);
        typeMap.mapPackage(namespace, packageName);
        Trc.exit();
    }

    /**
     * @deprecated this method is replaced by the getProvider
     * method in the org.apache.util.WSIFPluggableProviders class
     */
    public static WSIFProvider getDynamicWSIFProvider(String namespaceURI) {
        Trc.entry(null, namespaceURI);
        WSIFProvider p = 
           WSIFPluggableProviders.getProvider( namespaceURI );
        Trc.exit( p );
        return p;
    }

    /**
     * @deprecated this method is replaced by the overrideDefaultProvider
     * method in the org.apache.util.WSIFPluggableProviders class
     */
    public static void setDynamicWSIFProvider(
        String providerNamespaceURI,
        WSIFProvider provider) {
        Trc.entry(null, providerNamespaceURI, provider);

        WSIFPluggableProviders.overrideDefaultProvider(
           providerNamespaceURI, provider );

        Trc.exit();
    }

    /**
     * @deprecated this method is replaced by the setAutoLoadProviders
     * method in the org.apache.util.WSIFPluggableProviders class
     */
    public static void setAutoLoadProviders(boolean b) {
        Trc.entry(null, b);
        WSIFPluggableProviders.setAutoLoadProviders( b );
        Trc.exit();
    }

    /**
     * Get the dynamic proxy that will implement the interface iface
     * for the port portName.
     */
    public Object getStub(String portName, Class iface) throws WSIFException {
        Trc.entry(this, portName, iface);

        // Initialise the type mappings here (not in the constructor) so that
        // other products which use non-standard WSDL in their complexTypes
        // that WSIF wouldn't understand, can use the DynamicInvoker 
        // successfully. Using the DynamicInvoker means we would never come
        // through this code and so never try to parse the complexTypes.
        // Obviously if the user wants to use dynamic proxies then we have to 
        // parse the complex types.
        if (!typeMapInitialised) {
            initialiseTypeMappings();
            typeMapInitialised = true;
        }

        // if the port is not available, force the expection now rather
        // rather than go through the rest of this method		
        WSIFPort wsifPort = getPort(portName);

        // If we've got to this line then the port must be available
        PortType pt = getPortTypeFromPortName(portName);

        // If the user has already created a proxy for this interface before
        // but is now asking for a proxy for the same interface but a different
        // portName, we should cache the proxy here and just call 
        // clientProxy.setPort() instead.
        WSIFClientProxy clientProxy =
            WSIFClientProxy.newInstance(
                iface,
                def,
                service.getQName().getNamespaceURI(),
                service.getQName().getLocalPart(),
                portType.getQName().getNamespaceURI(),
                portType.getQName().getLocalPart(),
                typeMap);

        clientProxy.setPort(wsifPort);
        Object proxy = clientProxy.getProxy();

        // Tracing the proxy causes a hang!
        Trc.exit();
        return proxy;
    }

    /**
     * Get the dynamic proxy that will implement the interface iface
     */
    public Object getStub(Class iface) throws WSIFException {
        Trc.entry(this, iface);

        // Initialise the type mappings here (not in the constructor) so that
        // other products which use non-standard WSDL in their complexTypes
        // that WSIF wouldn't understand, can use the DynamicInvoker 
        // successfully. Using the DynamicInvoker means we would never come
        // through this code and so never try to parse the complexTypes.
        // Obviously if the user wants to use dynamic proxies then we have to 
        // parse the complex types.
        if (!typeMapInitialised) {
            initialiseTypeMappings();
            typeMapInitialised = true;
        }

        // if the port is not available, force the expection now rather
        // rather than go through the rest of this method		
        WSIFPort wsifPort = getPort();

        // Chosen port has been stored so use it to find portType
        String portName = chosenPort.getName();
        PortType pt = getPortTypeFromPortName(portName);

        // If the user has already created a proxy for this interface before
        // but is now asking for a proxy for the same interface but a different
        // portName, we should cache the proxy here and just call 
        // clientProxy.setPort() instead.
        WSIFClientProxy clientProxy =
            WSIFClientProxy.newInstance(
                iface,
                def,
                service.getQName().getNamespaceURI(),
                service.getQName().getLocalPart(),
                pt.getQName().getNamespaceURI(),
                pt.getQName().getLocalPart(),
                typeMap);

        clientProxy.setPort(wsifPort);
        Object proxy = clientProxy.getProxy();

        // Tracing the proxy causes a hang!
        Trc.exit();
        return proxy;
    }

    /**
     * Add new WSDL model extension registry that is shared by all
     * dynamic WSIF providers.
     */
    public static void addExtensionRegistry(ExtensionRegistry reg) {
        Trc.entry(null, reg);
        providersExtRegs.addExtensionRegistry(reg);
        Trc.exit();
    }

    /**
     * Return extension registry that contains ALL declared extensions.
     * This is special registry that does not allow to register serializers
     * but only to add new extension registreis through
     * addExtensionRegistry method.
     *
     * @see #addExtensionRegistry
     */
    public static ExtensionRegistry getCompositeExtensionRegistry() {
        Trc.entry(null);
        Trc.exit(providersExtRegs);
        return providersExtRegs;
    }

    private void init(Definition def, Service service, PortType portType)
        throws WSIFException {
        if (def == null)
            throw new IllegalArgumentException("WSDL definition can not be null");
        checkWSDLForWSIF(def);

        if (service == null) {
            Map services = WSIFUtils.getAllItems(def, "Service");

            service = (Service) WSIFUtils.getNamedItem(services, null, "Service");
        }

        if (portType == null) {
            // if all ports have the same portType --> use it
            Map ports = service.getPorts();
            if (ports.size() == 0) {
                throw new WSIFException(
                    "WSDL must contain at least one port in " + service.getQName());
            }

            for (Iterator i = ports.values().iterator(); i.hasNext();) {
                Port port = (Port) i.next();
                if (portType == null) {
                    portType = port.getBinding().getPortType();
                } else {
                    PortType pt = port.getBinding().getPortType();
                    if (!pt.getQName().equals(portType.getQName())) {
                        throw new WSIFException(
                            "when no port type was specified all ports "
                                + "must have the same port type in WSDL service "
                                + service.getQName());
                    }
                }
            }
            if (portType == null) {
                throw new IllegalArgumentException(
                    "WSDL more than one portType in service " + service);

            }
        }
        this.def = def;
        this.service = service;
        this.portType = portType;

        // checkPortTypeIsRPC(Definition def, PortType portType) has been replaced by 
        // checkPortTypeInformation(Definition def, PortType portType) since "Input Only"
        // operations are supported.
        checkPortTypeInformation(def, portType);

        // get all ports from service that has given portType

        Map ports = service.getPorts();
        // check that service has at least one port ...
        if (ports.size() == 0) {
            throw new WSIFException(
                "WSDL must contain at least one port in " + service.getQName());
        }

        myPortsMap = new Hashtable();
        for (Iterator i = ports.values().iterator(); i.hasNext();) {
            Port port = (Port) i.next();

            Binding binding = port.getBinding();
            if (binding == null)
                continue; // Ignore this error for the moment

            try {
                // Ignore port if provider is not available for supporting it
                List bindingExList = port.getBinding().getExtensibilityElements();
                ExtensibilityElement bindingFirstEx =
                    (ExtensibilityElement) bindingExList.get(0);
                String bindingNS = bindingFirstEx.getElementType().getNamespaceURI();
                String addressNS = bindingNS;
                try {
                    List addressExList = port.getExtensibilityElements();
                    ExtensibilityElement addressFirstEx =
                        (ExtensibilityElement) addressExList.get(0);
                    addressNS = addressFirstEx.getElementType().getNamespaceURI();
                } catch (NullPointerException npe) {
		        	Trc.ignoredException(npe);
					// ignore
                } catch (ArrayIndexOutOfBoundsException aie) {
		        	Trc.ignoredException(aie);
					// Extensibility element 0 does not exist
					// Allow address namespace to be the same as binding
                }
                // Check for a provider that supports the 
                if (WSIFPluggableProviders.isProviderAvailable(bindingNS, addressNS) ) {
                   // check if port has the same port type
                   if (binding.getPortType().getQName().equals(portType.getQName())) {
                      String portName = port.getName();
                      myPortsMap.put(portName, port);
                   }
                }
            } catch (NullPointerException e) {
	        	Trc.ignoredException(e);
                // Binding or extensibility element or QName was null
                // any of which means something's not right with
                // the port so don't include it.
            } catch (ArrayIndexOutOfBoundsException aie) {
	        	Trc.ignoredException(aie);
				// Extensibility element 0 does not exist
            }
        }
        int size = myPortsMap.size();
        myPortsArr = new Port[size];
        int count = 0;
        for (Iterator i = myPortsMap.values().iterator(); i.hasNext();) {
            // NOTE: there is no order in ports (it is hash function dependent...)
            Port port = (Port) i.next();
            myPortsArr[count++] = port;
        }

        // Provide the WSIDDynamicTypeMap with a list of all of the custom
        // types in the wsdl
        typeMap.setAllTypes(getAllCustomTypes());
    }

    /**
     * Get a list of all the custom complexTypes and simpleTypes in the wsdl
     */
    private ArrayList getAllCustomTypes() {
        ArrayList types = new ArrayList();
        Iterator typeMappingIterator = getDefaultTypeMappings();
        if (typeMappingIterator != null) {
            while (typeMappingIterator.hasNext()) {
                TypeMapping tm = (TypeMapping) typeMappingIterator.next();
                if (tm != null) {
                    String namespaceURI = tm.elementType.getNamespaceURI();
                    if (!namespaceURI.equals(WSIFConstants.NS_URI_1999_SCHEMA_XSD)
                        && !namespaceURI.equals(WSIFConstants.NS_URI_2000_SCHEMA_XSD)
                        && !namespaceURI.equals(WSIFConstants.NS_URI_2001_SCHEMA_XSD)) {
                        QName element = tm.elementType;
                        if (element != null) {
                            types.add(element);
                        }
                    }
                }
            }
        }
        return types;
    }

    /**
     * Get a list of all the default mappings for complexTypes and simpleTypes 
     * in the wsdl
     */
    private Iterator getDefaultTypeMappings() {
        if (typeReg != null) {
    		return typeReg.values().iterator();
    	}
        typeReg = new HashMap();
        List typesElList = Utils.getAllTypesElements(def);
        if (typesElList.size() > 0) {
            String schemaURI1999 = WSIFConstants.NS_URI_1999_SCHEMA_XSD;
            Schema2Java s2j1999 = new Schema2Java(schemaURI1999);
            QName qElemSchema1999 = new QName(schemaURI1999, "schema");
            String schemaURI2000 = WSIFConstants.NS_URI_2000_SCHEMA_XSD;
            Schema2Java s2j2000 = new Schema2Java(schemaURI2000);
            QName qElemSchema2000 = new QName(schemaURI2000, "schema");
            String schemaURI2001 = WSIFConstants.NS_URI_2001_SCHEMA_XSD;
            Schema2Java s2j2001 = new Schema2Java(schemaURI2001);
            QName qElemSchema2001 = new QName(schemaURI2001, "schema");

            Iterator typesElIterator = typesElList.iterator();
            while (typesElIterator.hasNext()) {
                UnknownExtensibilityElement unknExEl =
                    (UnknownExtensibilityElement) typesElIterator.next();
                Element schemaEl = unknExEl.getElement();
                try {
                    if (QNameUtils.matches(qElemSchema1999, schemaEl)
                        || QNameUtils.matches(qElemSchema2000, schemaEl)
                        || QNameUtils.matches(qElemSchema2001, schemaEl)) {
                        //Hashtable typeReg = new Hashtable();
                        if (QNameUtils.matches(qElemSchema1999, schemaEl))
                            s2j1999.createJavaMapping(schemaEl, typeReg);
                        else if (QNameUtils.matches(qElemSchema2000, schemaEl))
                            s2j2000.createJavaMapping(schemaEl, typeReg);
                        else
                            s2j2001.createJavaMapping(schemaEl, typeReg);
                    }
                } catch (Exception e) {
		        	Trc.ignoredException(e);
                    //ignored
                }
            }
        }
        return typeReg.values().iterator();
    }

    /**
     * Initialize default mappings between custom complex types and simple types and
     * Java classes.
     */
    private void initialiseTypeMappings() throws WSIFException {
        Iterator typeMappingIterator = getDefaultTypeMappings();
        if (typeMappingIterator != null) {
            while (typeMappingIterator.hasNext()) {
                TypeMapping tm = (TypeMapping) typeMappingIterator.next();

                if (tm.elementType != null && tm.elementType.getNamespaceURI() != null) {
                    String namespaceURI = tm.elementType.getNamespaceURI();
                    if (namespaceURI != null
                        && !namespaceURI.equals(WSIFConstants.NS_URI_1999_SCHEMA_XSD)
                        && !namespaceURI.equals(WSIFConstants.NS_URI_2000_SCHEMA_XSD)
                        && !namespaceURI.equals(WSIFConstants.NS_URI_2001_SCHEMA_XSD)
                        && !namespaceURI.equals(WSIFConstants.NS_URI_SOAP_ENC)
                        && tm.javaType != null) {
                        String packageName = Utils.getPackageName(tm.javaType);
                        if (packageName != null && !packageName.equals("")) {
                        	packageName += ".";
                        }
                        String className = packageName + Utils.getClassName(tm.javaType);
                        Class clazz = null;

                        try {
                            clazz =
                                Class.forName(className, true, Thread.currentThread().getContextClassLoader());
                        } catch (ClassNotFoundException e) {
                            // Ignore error - mapping will not be added
                            Trc.ignoredException(e);
                        }
                        // Create a new mapping but don't override one that already exists for this element type
                        if (clazz != null) {
                        	mapType(tm.elementType, clazz, false);
                        }
                    }
                }
            } // end while
        }
    }

    /**
     * Check PortType information is consistent. This method can be updated when
     * new operation types are supported.
     */
    private void checkPortTypeInformation(Definition def, PortType portType)
        throws WSIFException {
        List operationList = portType.getOperations();

        // process each operation to create dynamic operation instance
        for (Iterator i = operationList.iterator(); i.hasNext();) {
            Operation op = (Operation) i.next();
            String name = op.getName();
            if (op.isUndefined()) {
                throw new WSIFException("operation " + name + " is undefined!");
            }
            OperationType opType = op.getStyle();
            if (opType == null) {
                throw new WSIFException("operation " + name + " has no type!");
            }
            if (opType.equals(OperationType.REQUEST_RESPONSE)) {
                Input input = op.getInput();
                Output output = op.getOutput();
                if (input == null) {
                    throw new WSIFException("missing input message for operation " + name);
                }
                if (output == null) {
                    throw new WSIFException("missing output message for operation " + name);
                }
            } else if (opType.equals(OperationType.ONE_WAY)) {
                Input input = op.getInput();
                if (input == null) {
                    throw new WSIFException("missing input message for operation " + name);
                }
            } else {
                // Log message
                MessageLogger.log(
                    "WSIF.0004E",
                    opType,
                    portType.getQName().getLocalPart());

                // End message
                throw new WSIFException(
                    "operation type "
                        + opType
                        + " is not supported in port instance for "
                        + portType.getQName());
            }
        }
    }

    private void checkWSDLForWSIF(Definition def) throws WSIFException {
        try {
            checkWSDL(def);
        } catch (WSDLException ex) {
        	Trc.exception(ex);
            throw new WSIFException("invalid WSDL defintion " + def.getQName(), ex);
        }
    }

    /**
     * Check WSDL defintion to make sure it does not contain undefined
     * elements (typical case is referncing not defined portType).
     * <p><b>NOTE:</b> check is done only for curent document and not
     *  recursively for imported ones (they may be invalid but this
     *  port factory may not need them...).
     */
    private void checkWSDL(Definition def) throws WSDLException {
        for (Iterator i = def.getMessages().values().iterator(); i.hasNext();) {
            Message v = (Message) i.next();
            if (v.isUndefined()) {
                throw new WSDLException(
                    WSDLException.INVALID_WSDL,
                    "referencing undefined message " + v);
            }
        }
        for (Iterator i = def.getPortTypes().values().iterator(); i.hasNext();) {
            PortType v = (PortType) i.next();
            if (v.isUndefined()) {
                throw new WSDLException(
                    WSDLException.INVALID_WSDL,
                    "referencing undefined portType " + v);
            }
        }
        for (Iterator i = def.getBindings().values().iterator(); i.hasNext();) {
            Binding v = (Binding) i.next();
            if (v.isUndefined()) {
                throw new WSDLException(
                    WSDLException.INVALID_WSDL,
                    "referencing undefined binding " + v);
            }
        }
    }

    /**
    * Get the Definition object representing the wsdl document
    * @return The Definition object
    */
    public Definition getDefinition() {
        Trc.entry(this);
        Trc.exit(def);
        return def;
    }

    /**
     * Gets the context information for this WSIFService.
     * @return context
     */
    public WSIFMessage getContext() throws WSIFException {
        Trc.entry(this);
    	WSIFMessage contextCopy;
    	if (this.context == null) {
    		contextCopy = new WSIFDefaultMessage();
    	} else {
		    try {
			    contextCopy = (WSIFMessage) this.context.clone();
		    } catch (CloneNotSupportedException e) {
			    throw new WSIFException(
			        "CloneNotSupportedException cloning context", e);
		    }
    	}
        Trc.exit(contextCopy);
    	return contextCopy;
    }

    /**
     * Sets the context information for this WSIFService.
     * @param WSIFMessage the new context information
     */
    public void setContext(WSIFMessage context) {
        Trc.entry(this, context);
        if (context == null) {
        	throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
        Trc.exit();
    }

    public String deep() {
        String buff = "";
        try {
            buff = new String(this.toString());
            buff += "\nprovidersExtRegs:"
                + (providersExtRegs == null ? "null" : providersExtRegs.toString());
            buff += "\ndef:" + Trc.brief(def);
            buff += "\nservice:" + Trc.brief(service);
            buff += "\nportType:" + Trc.brief(portType);
            buff += "\nmyPortsArr:" + (myPortsArr == null ? "null" : myPortsArr.toString());
            buff += "\nmyPortsMap:" + Trc.brief(myPortsMap);
            buff += "\ntypeMap:" + (typeMap == null ? "null" : typeMap.toString());
            buff += "\ntypeMapInitialised:" + typeMapInitialised;
            buff += "\npreferredPort:" + (preferredPort == null ? "null" : preferredPort);
            buff += "\nchosenPort:" + Trc.brief(chosenPort);
            buff += "\ncontext:" + context;
        } catch (Exception e) {
            Trc.exceptionInTrace(e);
        }
        return buff;
    }
}

