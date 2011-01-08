package com.monstarlab.servicedroid.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
	
	public String toString() {
		try {
            Source source = new DOMSource(mDoc);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed converting to string");
        } catch (TransformerException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed converting to string");
        }
        return null;
	}
	
}
