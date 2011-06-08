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

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

public class ServiceDroidDocument {

	private static final String TAG = "ServiceDroidDocument";
	
	protected static final int SCHEMA_ATTRS = 1;
	protected static final int SCHEMA_ELS = 2;
	
	protected Document mDoc;
	protected int mSchema = 0;
	
	public ServiceDroidDocument() {
		this("<ServiceDroid schema=\"" + SCHEMA_ELS + "\"></ServiceDroid>");
	}
	
	public ServiceDroidDocument(String xml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			mDoc = builder.parse(new InputSource(new StringReader(xml)));
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			Log.e(TAG, "Failed parsing backup file.");
			Log.e(TAG, e.toString());
		} catch (SAXException e) {
			e.printStackTrace();
			Log.e(TAG, "Failed parsing backup file.");
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Failed reading backup file from SD card.");
			Log.e(TAG, e.toString());
		} finally {
			if (mDoc == null) {
				
			} else {
				mSchema = getSchema();
			}
		}
	}
	
	public boolean isValid() {
		return mDoc != null;
	}
	
	public int getSchema() {
		String schema = mDoc.getDocumentElement().getAttribute("schema");
		
		if (TextUtils.isEmpty(schema)) {
			return SCHEMA_ATTRS;
		} else {
			return Integer.parseInt(schema);
		}
	}
	

	public void addNode(String tagName, String[] attributes, String[] values) {
		Element el = mDoc.createElement(tagName);
		for (int i = 0; i < attributes.length; i++) {
			//el.setAttribute(attributes[i], TextUtils.htmlEncode(values[i]));
			String content = values[i];
			if (content != null) {
				Element attr = mDoc.createElement(attributes[i]);
			
				attr.setTextContent(TextUtils.htmlEncode(content));
				el.appendChild(attr);
			}
			
		}
		mDoc.getDocumentElement().appendChild(el);
	}
	
	public int getNumberOfTag(String tagName) {
		return mDoc.getElementsByTagName(tagName).getLength();
	}
	
	public String[][] getDataFromNode(String tagName, int index) {
		Node node = mDoc.getElementsByTagName(tagName).item(index);
		
		switch (mSchema) {
		case SCHEMA_ATTRS:
			return getDataFromAttributes(node);
		case SCHEMA_ELS:
		default:
			return getDataFromChildren(node);
		}
	}
	
	private String[][] getDataFromAttributes(Node node) {
		NamedNodeMap attrs = node.getAttributes();
		int numOfAttrs = attrs.getLength();
		
		String[][] data = new String[2][numOfAttrs];
		for (int i = 0; i < numOfAttrs; i++) {
			data[0][i] = attrs.item(i).getNodeName();
			data[1][i] = Html.fromHtml(attrs.item(i).getNodeValue()).toString();
		}

		return data;
	}
	
	private String[][] getDataFromChildren(Node node) {
		NodeList children = node.getChildNodes();
		
		// since getChildNodes returns ALL childNodes, include whitespace nodes,
		// we need to do a first loop just to see how many ELEMENT nodes there are
		int numOfChildEls = 0;
		int length = children.getLength();
		for (int i = 0; i < length; i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
				numOfChildEls++;
			}
		}
		
		
		String[][] data = new String[2][numOfChildEls];
		for (int i = 0; i < length; i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				data[0][i] = child.getNodeName();
				data[1][i] = Html.fromHtml(child.getTextContent().trim()).toString();
			}
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
