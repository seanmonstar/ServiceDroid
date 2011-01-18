package com.monstarlab.servicedroid.util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class ServiceDroidDocument {

	private static final String TAG = "ServiceDroidDocument";
	
	protected Document mDoc;
	
	public ServiceDroidDocument() {
		this("<ServiceDroid></ServiceDroid>");
	}
	
	public ServiceDroidDocument(String xml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			mDoc = builder.parse(new InputSource(new StringReader(xml)));
			
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "Failed building a blank document");
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "Failed building a blank document");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "Failed building a blank document");
		}
	}
	

	public void addNode(String tagName, String[] attributes, String[] values) {
		Element el = mDoc.createElement(tagName);
		for (int i = 0; i < attributes.length; i++) {
			el.setAttribute(attributes[i], values[i]);
		}
		mDoc.getDocumentElement().appendChild(el);
	}
	
	public int getNumberOfTag(String tagName) {
		return mDoc.getElementsByTagName(tagName).getLength();
	}
	
	public String[][] getDataFromNode(String tagName, int index) {
		Node node = mDoc.getElementsByTagName(tagName).item(index);
		
		NamedNodeMap attrs = node.getAttributes();
		int numOfAttrs = attrs.getLength();
		
		String[][] data = new String[2][numOfAttrs];
		for (int i = 0; i < numOfAttrs; i++) {
			data[0][i] = attrs.item(i).getNodeName();
			data[1][i] = attrs.item(i).getNodeValue();
		}
		
		
		return data;
	}
	
	protected String getStringFromNode(Node root) {
		StringBuilder result = new StringBuilder();

        if (root.getNodeType() == Node.TEXT_NODE)
            result.append(root.getNodeValue());
        else {
            if (root.getNodeType() != Node.DOCUMENT_NODE) {
                StringBuffer attrs = new StringBuffer();
                for (int k = 0; k < root.getAttributes().getLength(); ++k) {
                    attrs.append(" ").append(
                            root.getAttributes().item(k).getNodeName()).append(
                            "=\"").append(
                            root.getAttributes().item(k).getNodeValue())
                            .append("\" ");
                }
                result.append("<").append(root.getNodeName()).append(" ")
                        .append(attrs);
            } else {
                result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            }

            NodeList nodes = root.getChildNodes();
            if (nodes.getLength() > 0) {
            	if (root.getNodeType() != Node.DOCUMENT_NODE) {
            		result.append(">");
            	}
            	for (int i = 0, j = nodes.getLength(); i < j; i++) {
	                Node node = nodes.item(i);
	                result.append(getStringFromNode(node));
	            }
				if (root.getNodeType() != Node.DOCUMENT_NODE) {
				    result.append("</").append(root.getNodeName()).append(">");
				}
            } else {
            	 if (root.getNodeType() != Node.DOCUMENT_NODE) {
            		 result.append("/>");
                 }
            }

           
        }
        return result.toString();
	}
	
	public String toString() {
		
		return getStringFromNode(mDoc);
        //return null;
	}
	
}
