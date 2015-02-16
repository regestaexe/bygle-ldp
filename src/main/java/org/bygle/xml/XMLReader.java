package org.bygle.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
/**
 * @author Sandro De Leo  sdeleo@regesta.com
 */
public class XMLReader {
    private Element root = null;
    private String defaultEncoding;
	
	public XMLReader(FileInputStream fileInputStream) throws DocumentException {
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(fileInputStream);
		defaultEncoding = document.getXMLEncoding();
		root = document.getRootElement();		
	}
	public XMLReader(InputStreamReader inputStreamReader) throws DocumentException {
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(inputStreamReader);
		defaultEncoding = document.getXMLEncoding();
		root = document.getRootElement();		
	}
	public XMLReader(File file) throws DocumentException {
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(file);
		defaultEncoding = document.getXMLEncoding();
		root = document.getRootElement();
	}
	public XMLReader(String strDocXml) throws DocumentException {
		Document document = DocumentHelper.parseText(strDocXml);
		defaultEncoding = document.getXMLEncoding();
		root = document.getRootElement();
	}
	public XMLReader(byte[] bytes) throws DocumentException {
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(new ByteArrayInputStream(bytes));
		defaultEncoding = document.getXMLEncoding();
		root = document.getRootElement();
	}
	public void analyzeNodes(HashMap<String, ArrayList<String>> xpaths){
		analyzeNodes(root,xpaths);
	}
	
	@SuppressWarnings("unchecked")
	public List<Namespace> getNamespaces(){
		return (List<Namespace>)root.declaredNamespaces();
	}
	
	@SuppressWarnings("unchecked")
	public void analyzeNodes(Element node,HashMap<String, ArrayList<String>> xpaths){
		if(node!=null){
			if(node.getText()!=null && !node.getText().trim().equals("")){
				if(xpaths.get(node.getPath())==null){
					ArrayList<String> arrayList = new ArrayList<String>();
					arrayList.add(node.getText().trim());
					xpaths.put(node.getPath(),arrayList);
				}else{
					ArrayList<String> arrayList = xpaths.get(node.getPath());
					arrayList.add(node.getText().trim());
					xpaths.put(node.getPath(),arrayList);
				}
			}
			if(node.attributes()!=null){
				List<?> list =node.attributes();
				for (Iterator<Attribute> iterator = (Iterator<Attribute>)list.iterator(); iterator.hasNext();) {
					Attribute attribute = iterator.next();
					if(attribute.getText()!=null && !attribute.getText().trim().equals("")){
						if(xpaths.get(attribute.getPath())==null){
							ArrayList<String> arrayList = new ArrayList<String>();
							arrayList.add(attribute.getText().trim());
							xpaths.put(attribute.getPath(),arrayList);
						}else{
							ArrayList<String> arrayList = xpaths.get(attribute.getPath());
							arrayList.add(attribute.getText().trim());
							xpaths.put(attribute.getPath(),arrayList);
						}	
					}
				}
			}
			if(node.elements()!=null){
				List<?> list =node.elements();
				for (Iterator<Element> iterator = (Iterator<Element>)list.iterator(); iterator.hasNext();) {
					Element element = iterator.next(); 
					analyzeNodes(element,xpaths);	
				}
			}
		}
	}
	private String getValue(String xPath,String separator){
		String value = "";
		if (xPath.endsWith("/text()")) {
			List<?> nodeList = root.selectNodes(xPath);
			for (int i = 0; i < nodeList.size(); i++) {
				try {
					if(((Node)nodeList.get(i)).getText().indexOf("\\n")!=-1)
						value += ((Node)nodeList.get(i)).getText() + "\n";
					else		
						value += ((Node)nodeList.get(i)).getText();	
				} catch (NullPointerException exc) {
				}
				if(separator!=null && i<nodeList.size()-1)
					value+=separator;
			}
		} else {
			Node node = root.selectSingleNode(xPath);
			if (node != null) {
				value = node.getText();
			} else {
				value = "";
			}
		}
		if (value.equals(null)) {
			value = "";
		}
		//value = StringEscapeUtils.unescapeXml(value);
		OutputFormat outputFormat = new OutputFormat();
		outputFormat.setEncoding(defaultEncoding);
		StringWriter stringWriter = new StringWriter();
		XMLWriter xmlWriter = null;
		outputFormat.setNewlines(true);
		xmlWriter = new XMLWriter(stringWriter, outputFormat);
		xmlWriter.setMaximumAllowedCharacter(255);
		try {
			xmlWriter.write(value);
			xmlWriter.flush();
			xmlWriter.close();
			stringWriter.close();
		} catch (IOException e) {
			return "";
		}
		return StringEscapeUtils.unescapeXml(stringWriter.toString());
	}
	public String getNodeAsXML(String xPath){
		try {
			Node node = root.selectSingleNode(xPath);
			OutputFormat outputFormat = new OutputFormat();
			outputFormat.setEncoding(defaultEncoding);
			StringWriter stringWriter = new StringWriter();
			XMLWriter xmlWriter = null;
			outputFormat.setNewlines(true);
			xmlWriter = new XMLWriter(stringWriter, outputFormat);
			xmlWriter.setMaximumAllowedCharacter(255);
			try {
				xmlWriter.write(node.asXML());
				xmlWriter.flush();
				xmlWriter.close();
				stringWriter.close();
			} catch (IOException e) {
				return null;
			}
			return stringWriter.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;			
		}
	}
	public String getNodeListAsXML(String xPath){
		try {
			List<?> nodeList = root.selectNodes(xPath);
			OutputFormat outputFormat = new OutputFormat();
			outputFormat.setEncoding(defaultEncoding);
			StringWriter stringWriter = new StringWriter();
			XMLWriter xmlWriter = null;
			outputFormat.setNewlines(true);
			xmlWriter = new XMLWriter(stringWriter, outputFormat);
			xmlWriter.setMaximumAllowedCharacter(255);			
			try {
				for (int i = 0; i < nodeList.size(); i++) {
					xmlWriter.write(((Node)nodeList.get(i)).asXML());
					if(i< nodeList.size()-1)
						xmlWriter.write("\r\n");
				}
				xmlWriter.flush();
				xmlWriter.close();
				stringWriter.close();
			} catch (IOException e) {
				return null;
			}
			return stringWriter.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;			
		}
	}
	public int getNodeCount(String xPath){		
		try {			
			return root.selectNodes(xPath).size();
		} catch (Exception e) {
			return 0;
		}
	}
	private ArrayList<String> getValues(String xPath){
		ArrayList<String> result = new ArrayList<String>();
		List<?> nodeList = root.selectNodes(xPath);
		for (int i = 0; i < nodeList.size(); i++) {
			if(((Node)nodeList.get(i)).getNodeType()== Node.ATTRIBUTE_NODE){
				result.add(((Attribute)nodeList.get(i)).getStringValue());
			}else{
				try {
					if(((Node)nodeList.get(i)).getText().indexOf("\\n")!=-1)
						result.add(((Node)nodeList.get(i)).getText());
					else		
						result.add(((Node)nodeList.get(i)).getText());	
				} catch (NullPointerException exc) {
				}
			}
		}
		return result;
	}
	public List<?> getNodeList(String xPath){
		return root.selectNodes(xPath);
	}
	public String getNodeValue(String xPath,String separator){
		return getValue(xPath,separator);
	} 
	public String getNodeValue(String xPath){
		return getValue(xPath,null);
	}
	public String getTrimmedNodeValue(String xPath,String separator){
		return getValue(xPath,separator).trim();
	} 
	public String getTrimmedNodeValue(String xPath){
		return getValue(xPath,null).trim();
	}
	public String getValueOf(String xPath){
		return root.valueOf(xPath);
	}
	public ArrayList<String> getNodesValues(String xPath){
		return getValues(xPath);
	}
	public String getDefaultEncoding() {
		return defaultEncoding;
	}
	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}
	public String getRootNode(){
		return root.getName();
	}
}
