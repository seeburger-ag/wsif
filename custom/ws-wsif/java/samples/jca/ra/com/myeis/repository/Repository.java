/*
 * Copyright 2002-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, 2002, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package com.myeis.repository;

import java.io.InputStream;
import java.io.StringReader;
import java.util.*;

import javax.wsdl.*;
import javax.xml.namespace.QName;

import org.apache.wsif.base.WSIFServiceImpl;
import org.apache.wsif.providers.jca.toolplugin.*;
import org.apache.wsif.util.WSIFUtils;

import com.ibm.wsdl.PortTypeImpl;
import com.myeis.wsdl.extensions.j2c.myeis.MyEISExtensionRegistry;
import com.ibm.wsdl.xml.*;

public class Repository {

	public PortTypeArray getPortTypes(String queryString) {
		/*
		 * If the back end exposes business functions that allow the browsing of the metadata then getting the port types can
		 * be delegated directly. In this sample, the metadata is represented by the two WSDL files (CustomerInfo and PurchaseOrderInfo).
		 * Hence, we simply retrieve those files and return their corresponding port types.
		 */
		ArrayList portTypes = new ArrayList();
		try {
			WSIFServiceImpl.addExtensionRegistry(new MyEISExtensionRegistry());
			WSIFServiceImpl.addExtensionRegistry(new org.apache.wsif.wsdl.extensions.format.FormatExtensionRegistry());
			WSIFServiceImpl.addExtensionRegistry(new org.apache.wsif.wsdl.extensions.java.JavaExtensionRegistry());
			// load all definitions
			ArrayList definitions = new ArrayList();
			Definition definition = WSIFUtils.readWSDL(null, "com/myeis/services/CustomerInfo.wsdl", this.getClass().getClassLoader());
			definitions.add(definition);
			definition = WSIFUtils.readWSDL(null, "com/myeis/services/PurchaseOrderInfo.wsdl", this.getClass().getClassLoader());
			definitions.add(definition);

			Iterator definitionIterator = definitions.iterator();
			while (definitionIterator.hasNext()) {
				definition = (Definition) definitionIterator.next();
				Iterator portTypeIterator = definition.getPortTypes().values().iterator();
				while (portTypeIterator.hasNext()) {
					PortType portType = (PortType) portTypeIterator.next();
					if ((queryString.equals("")) || (queryString.equals("*")) || (portType.getQName().getLocalPart().indexOf(queryString) != -1)) {
						portTypes.add(portType);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		PortType[] portTypeA = new PortType[portTypes.size()];
		portTypes.toArray(portTypeA);
		PortTypeArray portTypeArray = new PortTypeArray();
		portTypeArray.setPortTypes(portTypeA);

		return portTypeArray;
	}

	public ImportDefinition getDefinition(PortTypeSelection portTypeSelection) {
		/*
		 * If the back end exposes business functions that allow the browsing of the metadata then getting the definitions can
		 * be delegated directly. In this sample, the metadata is represented by the two WSDL files (CustomerInfo and PurchaseOrderInfo).
		 * Hence, we simply retrieve those files and filter their corresponding port types and operations based on the selection passed in.
		 */
		ArrayList importDefinitions = new ArrayList();
		ArrayList importDefinitionsResult = new ArrayList();
		try {
			WSIFServiceImpl.addExtensionRegistry(new MyEISExtensionRegistry());
			WSIFServiceImpl.addExtensionRegistry(new org.apache.wsif.wsdl.extensions.format.FormatExtensionRegistry());
			WSIFServiceImpl.addExtensionRegistry(new org.apache.wsif.wsdl.extensions.java.JavaExtensionRegistry());
			// load all definitions
			ImportDefinition importDefinition = new ImportDefinition();
			importDefinition.setDefinition(WSIFUtils.readWSDL(null, "com/myeis/services/CustomerInfo.wsdl", this.getClass().getClassLoader()));

			ImportResource[] importResources = new ImportResource[1];
			ImportResource importResource = new ImportResource();
			importResource.setLocation("com/myeis/services/MyEISFormatHandlerGenMetaData.eis");
			String metaData = "MyEIS Resource Adapter version 1.0 meta data";
			importResource.setContents(metaData.getBytes());
			importResources[0] = importResource;

			ImportXSD[] importXSDs = new ImportXSD[1];
			ImportXSD importXSD = new ImportXSD();
			importXSD.setNamespace("http://services.myeis.ibm.com/");
			importXSD.setLocation("com/myeis/services/CustomerInfo.xsd");

			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("com/myeis/services/CustomerInfo.xsd");
			byte[] inputBytes = new byte[inputStream.available()];
			inputStream.read(inputBytes);
			importXSD.setSource(new String(inputBytes));

			importXSDs[0] = importXSD;
			importDefinition.setImportXSDs(importXSDs);
			importDefinitions.add(importDefinition);

			importDefinition = new ImportDefinition();
			importDefinition.setDefinition(WSIFUtils.readWSDL(null, "com/myeis/services/PurchaseOrderInfo.wsdl", this.getClass().getClassLoader()));
			importXSDs = new ImportXSD[1];
			importXSD = new ImportXSD();
			importXSD.setNamespace("http://services.myeis.ibm.com/");
			importXSD.setLocation("com/myeis/services/PurchaseOrderInfo.xsd");

			inputStream = this.getClass().getClassLoader().getResourceAsStream("com/myeis/services/PurchaseOrderInfo.xsd");
			inputBytes = new byte[inputStream.available()];
			inputStream.read(inputBytes);
			importXSD.setSource(new String(inputBytes));

			importXSDs[0] = importXSD;
			importDefinition.setImportXSDs(importXSDs);
			importDefinition.setImportResources(importResources);
			importDefinitions.add(importDefinition);

			for (int j = 0; j < importDefinitions.size(); j++) {
				if (((ImportDefinition) importDefinitions.get(j)).getDefinition().getPortTypes().containsKey(portTypeSelection.getPortTypeQName())) {

					ImportDefinition returnDefinition = (ImportDefinition) importDefinitions.get(j);
					Definition def = returnDefinition.getDefinition();

					//Adjust the import statements to be relative paths
					Iterator importIter = def.getImports().values().iterator();
					while (importIter.hasNext()) {
						List imports = (List) importIter.next();
						Iterator listIterator = imports.iterator();
						while (listIterator.hasNext()) {
							javax.wsdl.Import imp = (javax.wsdl.Import) listIterator.next();
							if (imp.getLocationURI().startsWith("/com/myeis/services/")) {
								imp.setLocationURI("." + imp.getLocationURI());
							}
						}

					}
					//Remove all the portTypes that were not part of the selection

					Iterator iter = def.getPortTypes().keySet().iterator();
					List unusedPortTypes = new ArrayList();
					PortType selectedPortType = null;
					while (iter.hasNext()) {
						QName portTypeQName = (QName) iter.next();
						if (!portTypeQName.equals(portTypeSelection.getPortTypeQName())) {
							unusedPortTypes.add(portTypeQName);
						} else {
							selectedPortType = def.getPortType(portTypeQName);
						}
					}
					iter = unusedPortTypes.iterator();
					while (iter.hasNext()) {
						def.removePortType((QName) iter.next());
					}

					//Remove all the operations that were not part of the selection
					List operationList = selectedPortType.getOperations();
					List selectedOperationsList = new ArrayList();
					for (int index = 0; index < portTypeSelection.getOperationSelection().length; index++) {
						OperationSelection selection = portTypeSelection.getOperationSelection()[index];
						Operation op = selectedPortType.getOperation(selection.getOperationName(), selection.getInputName(), selection.getOutputName());
						if (op != null) {
							selectedOperationsList.add(op);
						}
					}

					iter = operationList.iterator();
					List unusedOperationsList = new ArrayList();

					while (iter.hasNext()) {
						Operation op = (Operation) iter.next();
						if (!selectedOperationsList.contains(op)) {
							unusedOperationsList.add(op);
						}
					}

					iter = unusedOperationsList.iterator();
					while (iter.hasNext()) {
						Operation op = (Operation) iter.next();
						operationList.remove(op);
						Iterator bindingIterator = def.getBindings().values().iterator();
						while (bindingIterator.hasNext()) {
							Binding binding = (Binding) bindingIterator.next();
							String inputName = null;
							if (op.getInput() != null)
								inputName = op.getInput().getName();

							String outputName = null;
							if (op.getOutput() != null)
								outputName = op.getOutput().getName();
							BindingOperation bindingOp = binding.getBindingOperation(op.getName(), inputName, outputName);
							if (bindingOp != null)
								binding.getBindingOperations().remove(bindingOp);
						}
					}

					return returnDefinition;
				}
			}
			//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] getRawEISMetaData(String queryString) {
		/*
		 * This method is just for illustrative purposes, to demonstrate how an EIS can return a proprietary metadata format.
		 * This metadata can be cached in tool environments for performance improvements if several services are to be imported from 
		 * the same EIS system.
		 */
		String customerInfoData = "PortType: CustomerInfo\n Operation: getAddress\n Operation: getCustomer\n";
		String purchaseOrderData = "PortType: PurchaseOrderInfo\n Operation: getPurchaseOrder\n";
		String result = new String();
		if ((queryString.equals("")) || (queryString.equals("*"))) {
			result = customerInfoData + purchaseOrderData;

		} else {
			if ("CustomerInfo".indexOf(queryString) != -1) {
				result = result + customerInfoData;
			}
			if ("PurchaseOrderInfo".indexOf(queryString) != -1) {
				result = result + purchaseOrderData;
			}

		}
		return result.getBytes();
	}
}