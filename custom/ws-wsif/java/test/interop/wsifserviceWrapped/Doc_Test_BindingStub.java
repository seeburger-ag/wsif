/**
 * Doc_Test_BindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package interop.wsifserviceWrapped;

public class Doc_Test_BindingStub extends org.apache.axis.client.Stub implements interop.wsifserviceWrapped.Doc_TestPortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    public Doc_Test_BindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public Doc_Test_BindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public Doc_Test_BindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://soapinterop.org/", ">ComplexDocument");
            cachedSerQNames.add(qName);
            cls = interop.wsifserviceWrapped.ComplexDocument_ElemType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soapinterop.org/", ">SingleTagResponse");
            cachedSerQNames.add(qName);
            cls = interop.wsifserviceWrapped.SingleTagResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soapinterop.org/", "ArrayOfSimpleDocument");
            cachedSerQNames.add(qName);
            cls = interop.wsifserviceWrapped.ArrayOfSimpleDocument.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soapinterop.org/", "SingleTag");
            cachedSerQNames.add(qName);
            cls = interop.wsifserviceWrapped.SingleTag_Type.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soapinterop.org/", "ComplexDocument");
            cachedSerQNames.add(qName);
            cls = interop.wsifserviceWrapped.ComplexDocument_Type.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soapinterop.org/", "ChildDocument");
            cachedSerQNames.add(qName);
            cls = interop.wsifserviceWrapped.ChildDocument.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soapinterop.org/", ">SimpleDocumentResponse");
            cachedSerQNames.add(qName);
            cls = interop.wsifserviceWrapped.SimpleDocumentResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soapinterop.org/", ">SimpleDocument");
            cachedSerQNames.add(qName);
            cls = interop.wsifserviceWrapped.SimpleDocument_ElemType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soapinterop.org/", "SimpleDocument");
            cachedSerQNames.add(qName);
            cls = interop.wsifserviceWrapped.SimpleDocument_Type.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(simplesf);
            cachedDeserFactories.add(simpledf);

            qName = new javax.xml.namespace.QName("http://soapinterop.org/", ">SingleTag");
            cachedSerQNames.add(qName);
            cls = interop.wsifserviceWrapped.SingleTag_ElemType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://soapinterop.org/", ">ComplexDocumentResponse");
            cachedSerQNames.add(qName);
            cls = interop.wsifserviceWrapped.ComplexDocumentResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    private org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call =
                    (org.apache.axis.client.Call) super.service.createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                if(_call.isPropertySupported(key))
                    _call.setProperty(key, super.cachedProperties.get(key));
                else
                    _call.setScopedProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                        java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                        _call.registerTypeMapping(cls, qName, sf, df, false);
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", t);
        }
    }

    public interop.wsifserviceWrapped.SingleTagResponse singleTag(interop.wsifserviceWrapped.SingleTag_ElemType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.addParameter(new javax.xml.namespace.QName("http://soapinterop.org/", "SingleTag"), new javax.xml.namespace.QName("http://soapinterop.org/", ">SingleTag"), interop.wsifserviceWrapped.SingleTag_ElemType.class, javax.xml.rpc.ParameterMode.IN);
        _call.setReturnType(new javax.xml.namespace.QName("http://soapinterop.org/", ">SingleTagResponse"), interop.wsifserviceWrapped.SingleTagResponse.class);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://soapinterop.org/SingleTag");
        _call.setEncodingStyle(null);
        _call.setScopedProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setScopedProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setOperationStyle("document");
        _call.setOperationName(new javax.xml.namespace.QName("http://soapinterop.org/", "SingleTag"));

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            try {
                return (interop.wsifserviceWrapped.SingleTagResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (interop.wsifserviceWrapped.SingleTagResponse) org.apache.axis.utils.JavaUtils.convert(_resp, interop.wsifserviceWrapped.SingleTagResponse.class);
            }
        }
    }

    public interop.wsifserviceWrapped.SimpleDocumentResponse simpleDocument(interop.wsifserviceWrapped.SimpleDocument_ElemType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.addParameter(new javax.xml.namespace.QName("http://soapinterop.org/", "SimpleDocument"), new javax.xml.namespace.QName("http://soapinterop.org/", ">SimpleDocument"), interop.wsifserviceWrapped.SimpleDocument_ElemType.class, javax.xml.rpc.ParameterMode.IN);
        _call.setReturnType(new javax.xml.namespace.QName("http://soapinterop.org/", ">SimpleDocumentResponse"), interop.wsifserviceWrapped.SimpleDocumentResponse.class);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://soapinterop.org/SimpleDocument");
        _call.setEncodingStyle(null);
        _call.setScopedProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setScopedProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setOperationStyle("document");
        _call.setOperationName(new javax.xml.namespace.QName("http://soapinterop.org/", "SimpleDocument"));

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            try {
                return (interop.wsifserviceWrapped.SimpleDocumentResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (interop.wsifserviceWrapped.SimpleDocumentResponse) org.apache.axis.utils.JavaUtils.convert(_resp, interop.wsifserviceWrapped.SimpleDocumentResponse.class);
            }
        }
    }

    public interop.wsifserviceWrapped.ComplexDocumentResponse complexDocument(interop.wsifserviceWrapped.ComplexDocument_ElemType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.addParameter(new javax.xml.namespace.QName("http://soapinterop.org/", "ComplexDocument"), new javax.xml.namespace.QName("http://soapinterop.org/", ">ComplexDocument"), interop.wsifserviceWrapped.ComplexDocument_ElemType.class, javax.xml.rpc.ParameterMode.IN);
        _call.setReturnType(new javax.xml.namespace.QName("http://soapinterop.org/", ">ComplexDocumentResponse"), interop.wsifserviceWrapped.ComplexDocumentResponse.class);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://soapinterop.org/ComplexDocument");
        _call.setEncodingStyle(null);
        _call.setScopedProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setScopedProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setOperationStyle("document");
        _call.setOperationName(new javax.xml.namespace.QName("http://soapinterop.org/", "ComplexDocument"));

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            try {
                return (interop.wsifserviceWrapped.ComplexDocumentResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (interop.wsifserviceWrapped.ComplexDocumentResponse) org.apache.axis.utils.JavaUtils.convert(_resp, interop.wsifserviceWrapped.ComplexDocumentResponse.class);
            }
        }
    }

}
