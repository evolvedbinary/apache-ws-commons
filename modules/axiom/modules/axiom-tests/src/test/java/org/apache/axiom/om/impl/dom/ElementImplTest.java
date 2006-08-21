/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
 */
package org.apache.axiom.om.impl.dom;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.apache.axiom.om.impl.dom.jaxp.DocumentBuilderFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class ElementImplTest extends TestCase {

	public ElementImplTest() {
		super();
	}

	public ElementImplTest(String name) {
		super(name);
	}
	
	public void testSetText() {
		OMDOMFactory factory = new OMDOMFactory();
		String localName = "TestLocalName";
		String namespace = "http://ws.apache.org/axis2/ns";
		String prefix = "axis2";
		OMElement elem = factory.createOMElement(localName,namespace,prefix);
		
		String text = "The quick brown fox jumps over the lazy dog";
		
		elem.setText(text);
		
		assertEquals("Text value mismatch", text, elem.getText());
		
	}
	
	public void testSerialize() {
		OMDOMFactory factory = new OMDOMFactory();
		String localName = "TestLocalName";
		String namespace = "http://ws.apache.org/axis2/ns";
		String prefix = "axis2";
		String tempText = "The quick brown fox jumps over the lazy dog";
		String textToAppend = " followed by another";
		
		OMElement elem = factory.createOMElement(localName,namespace,prefix);
		OMText textNode = factory.createOMText(elem,tempText);
		
		((Text)textNode).appendData(textToAppend);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			elem.serialize(baos);
//			System.out.println(new String(baos.toByteArray()));
			//TODO TEMPORARY: remove this
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testAddChild() {
		OMDOMFactory factory = new OMDOMFactory();
		String localName = "TestLocalName";
		String childLocalName = "TestChildLocalName";
		String namespace = "http://ws.apache.org/axis2/ns";
		String prefix = "axis2";
		
		OMElement elem = factory.createOMElement(localName,namespace,prefix);
		OMElement childElem = factory.createOMElement(childLocalName,namespace, prefix);
		
		elem.addChild(childElem);
		
		Iterator it = elem.getChildrenWithName(new QName(namespace, childLocalName));
		
		int count = 0;
		while (it.hasNext()) {
			OMElement child = (OMElement) it.next();
			assertEquals("Child local name mismatch", childLocalName, child.getLocalName());
			assertEquals("Child namespace mismatch", namespace, child.getNamespace().getNamespaceURI());
			count ++;
		}
		assertEquals("In correct number of children", 1, count );
	}
	
	public void testAppendChild() {
		try {
			String elementName = "TestElem";
			String childElemName = "TestChildElem";
			String childTextValue = "text value of the child text node";
			
			//Apending am Element node
            DocumentBuilderFactoryImpl.setDOOMRequired(true);
			Document doc = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder().newDocument();
			Element elem = doc.createElement(elementName);
			Element childElem = doc.createElement(childElemName);
			
			elem.appendChild(childElem);
			
			Element addedChild = (Element)elem.getFirstChild();
			assertNotNull("Child Element node missing",addedChild);
			assertEquals("Incorre node object", childElem, addedChild);
			
			elem = doc.createElement(elementName);
			Text text = doc.createTextNode(childTextValue);
			elem.appendChild(text);
			
			Text addedTextnode = (Text)elem.getFirstChild();
			assertNotNull("Child Text node missing", addedTextnode);
			assertEquals("Incorrect node object", text, addedTextnode);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
            DocumentBuilderFactoryImpl.setDOOMRequired(false);      
        }
	}
	
	/**
	 * Testing the NodeList returned with the elements's children
	 */
	public void testGetElementsbyTagName() {
		try {
			String childElementLN = "Child";
			
            DocumentBuilderFactoryImpl.setDOOMRequired(true);
            
			Document doc = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder().newDocument();
			Element docElem = doc.getDocumentElement();
			assertNull("The document element shoudl be null", docElem);
			
			docElem = doc.createElement("Test");
			docElem.appendChild(doc.createElement(childElementLN));
			docElem.appendChild(doc.createElement(childElementLN));
			docElem.appendChild(doc.createElement(childElementLN));
			docElem.appendChild(doc.createElement(childElementLN));
			docElem.appendChild(doc.createElement(childElementLN));
			docElem.appendChild(doc.createElement(childElementLN));
			docElem.appendChild(doc.createElement(childElementLN));
			
			NodeList list = docElem.getElementsByTagName(childElementLN);
			
			assertEquals("Incorrect number of child elements", 7 ,list.getLength());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
            DocumentBuilderFactoryImpl.setDOOMRequired(false);      
        }
	}
	
	public void testGetElementsbyTagNameNS() {
		try {
			String childElementLN = "test:Child";
			String childElementNS = "http://ws.apache.org/ns/axis2/dom";
			
            DocumentBuilderFactoryImpl.setDOOMRequired(true);
			Document doc = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder().newDocument();
			Element docElem = doc.getDocumentElement();
			assertNull("The document element shoudl be null", docElem);
			
			docElem = doc.createElementNS("http://test.org", "test:Test");
			
			docElem.appendChild(doc.createElementNS(childElementNS, childElementLN));
			docElem.appendChild(doc.createElementNS(childElementNS, childElementLN));
			docElem.appendChild(doc.createElementNS(childElementNS, childElementLN));
			docElem.appendChild(doc.createElementNS(childElementNS, childElementLN));
			docElem.appendChild(doc.createElementNS(childElementNS, childElementLN));
			docElem.appendChild(doc.createElementNS(childElementNS, childElementLN));
			docElem.appendChild(doc.createElementNS(childElementNS, childElementLN));
			
			NodeList list = docElem.getElementsByTagNameNS(childElementNS, childElementLN);
			
			assertEquals("Incorrect number of child elements", 7 ,list.getLength());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
            DocumentBuilderFactoryImpl.setDOOMRequired(false);      
        }
	}	
}
