package org.bygle.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bygle.xml.exception.XMLException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.XPathException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jdom2.input.DOMBuilder;

/**
 * @author Sandro De Leo  sdeleo@regesta.com
 */
public class XMLBuilder {
	private org.jdom2.Document JDomDocument = null;
	private org.w3c.dom.Document DomDocument = null;
	private Document dom4JDocument = null;
	private Element root = null;
	private String htmlTagClass = "";
	
	public XMLBuilder()throws XMLException {}
	
	public XMLBuilder(String rootNode) throws XMLException {
		try {
			dom4JDocument = DocumentHelper.createDocument();
			root = dom4JDocument.addElement( rootNode );
		} catch (Exception e) {
			throw new XMLException(e.getMessage());
		}
	}
	
	public XMLBuilder(String rootNode,Map<String, String> uris) throws XMLException {
		try {
			DocumentFactory factory = new DocumentFactory();
			factory.setXPathNamespaceURIs(uris);
			dom4JDocument = factory.createDocument();
			root = dom4JDocument.addElement( rootNode );
			Iterator<?> iterator = uris.entrySet().iterator();
			 while (iterator.hasNext()) {
			        @SuppressWarnings("rawtypes")
					Map.Entry pairs = (Map.Entry)iterator.next();
			        root.add(Namespace.get((String)pairs.getKey(), (String) pairs.getValue()));
			 }
		} catch (Exception e) {
			throw new XMLException(e.getMessage());
		}
	}
	
	public XMLBuilder(String rootNode,Map<String, String> uris,String encoding) throws XMLException {
		try {
			DocumentFactory factory = new DocumentFactory();
			factory.setXPathNamespaceURIs(uris);
			dom4JDocument = factory.createDocument();
			if(encoding!=null)
				dom4JDocument.setXMLEncoding(encoding);
			root = dom4JDocument.addElement( rootNode );
			Iterator<?> iterator = uris.entrySet().iterator();
			 while (iterator.hasNext()) {
				 	@SuppressWarnings("rawtypes")
			        Map.Entry pairs = (Map.Entry)iterator.next();
			        root.add(Namespace.get((String)pairs.getKey(), (String) pairs.getValue()));
			 }
		} catch (Exception e) {
			throw new XMLException(e.getMessage());
		}
	}
	public XMLBuilder(ByteArrayInputStream docXml) throws XMLException {
		try {
			SAXReader reader = new SAXReader();
			dom4JDocument = reader.read(docXml);
			root = dom4JDocument.getRootElement();
		} catch (Exception e) {
			throw new XMLException(e.getMessage());
		}
	}
	
	public XMLBuilder(byte[] docXml) throws XMLException {
		try {
			SAXReader reader = new SAXReader();
			dom4JDocument = reader.read(new ByteArrayInputStream(docXml));
			root = dom4JDocument.getRootElement();
		} catch (Exception e) {
			throw new XMLException(e.getMessage());
		}
	}
	
	public XMLBuilder(File file) throws XMLException {
		try {
			SAXReader reader = new SAXReader();
			dom4JDocument = reader.read(file);
			root = dom4JDocument.getRootElement();
		} catch (Exception e) {
			throw new XMLException(e.getMessage());
		}
	}
	public XMLBuilder(String strDocXml, String strEncoding, String htmlTagClass) throws XMLException {
		strDocXml = strDocXml.replaceAll("<\\?xw-sr\\?>", "IniZioTagHtMlspan class=LeViRGoleTte" + htmlTagClass + "LeViRGoleTteFinETagHtMl");
		strDocXml = strDocXml.replaceAll("<\\?xw-er\\?>", "IniZioTagHtMl/spanFinETagHtMl");
        this.htmlTagClass = htmlTagClass;
		boolean isoPass = false;
		String strIso = "";
		if (strDocXml.indexOf("?>\n") != -1) {
			strIso = strDocXml.substring(0, strDocXml.indexOf("?>\n") + 3);
			strDocXml = strDocXml.substring(strDocXml.indexOf("?>\n") + 3);
			isoPass = true;
		}
		if (!isoPass && strEncoding != null && !(strEncoding.trim()).equals("")) {
			strDocXml = "<?xml version=\"1.0\" encoding=\"" + strEncoding + "\"?>\n" + strDocXml;
		}
		if (isoPass) {
			strDocXml = strIso + strDocXml;
		}
		try {
			strDocXml=parseAttribute(strDocXml);
			dom4JDocument = DocumentHelper.parseText(strDocXml);
			root = dom4JDocument.getRootElement();
		} catch (DocumentException e) {
			throw new XMLException(e.getMessage());
		}
					
	}
	public XMLBuilder(String strDocXml, String strEncoding,Map<String, String> uris) throws XMLException {
		strDocXml = "<?xml version=\"1.0\" encoding=\"" + strEncoding + "\"?>\n" + strDocXml;
		try {
			DocumentFactory factory = new DocumentFactory();
			factory.setXPathNamespaceURIs(uris);
			dom4JDocument = factory.createDocument();
			dom4JDocument = DocumentHelper.parseText(strDocXml);
			root = dom4JDocument.getRootElement();
		}catch (Exception e) {
			throw new XMLException(e.getMessage());
		}
					
	}
	public XMLBuilder(String strDocXml, String strEncoding) throws XMLException {
		try {		
			strDocXml=parseAttribute(strDocXml);
			dom4JDocument = DocumentHelper.parseText(strDocXml);
			root = dom4JDocument.getRootElement();
		}catch (DocumentException e) {
			e.printStackTrace();
			throw new XMLException(e.getMessage());
		}
					
	}
	
/*	public void insertNodeAt(String xPath,String nodeValue,int position) throws XMLException {
		try {
			
			NodeList listNode = XPathAPI.selectNodeList(DomDocument,newXPath);
			if(listNode!=null && listNode.getLength()>0){
				listNode.
			}else{
				insertNode(xPath,nodeValue);
			}
		} catch (TransformerException e) {
			
		}
	}*/
	public void insertNode(String xPath, String nodeValue) throws XMLException{
		String value = StringEscapeUtils.escapeHtml4(nodeValue);
		insertNodeEscaped(xPath,value,false);
	}
	public void insertNode(String xPath, String nodeValue,boolean isCData) throws XMLException{
		String value = StringEscapeUtils.escapeHtml4(nodeValue);
		insertNodeEscaped(xPath,value,isCData);
	}
	@SuppressWarnings("unchecked")
	private void insertNodeEscaped(String xPath, String nodeValue,boolean isCData) throws XMLException {
			xPath=testXPath(xPath);
			if (xPath != null && !xPath.trim().equals("")) {
				if(xPath.indexOf("//")!=-1)
					xPath = xPath.replaceAll("//", "~");
				StringTokenizer stringTokenizer = new StringTokenizer(xPath, "/");
				String currentXPath = "";
				int count = 0;
				int tot = stringTokenizer.countTokens();
				while (stringTokenizer.hasMoreTokens()) {
					count++;
					String nodeName = stringTokenizer.nextToken();
					String realName = nodeName.replace('~','/');
					nodeName = nodeName.replace('~','/');
					Attribute appoAttribute = null;
					Namespace apponNamespace = null;
					ArrayList<Attribute> appoAttributesList = null;
					try {
						if (realName.indexOf("[") != -1) {
							try {
								try {
									Integer.parseInt(realName.substring(realName.indexOf("[") + 1, realName.indexOf("]")));
								} catch (StringIndexOutOfBoundsException e1) {
									String endName=stringTokenizer.nextToken();
									realName+="/"+endName;
									nodeName+="/"+endName;
									tot=tot-1;
									Integer.parseInt(realName.substring(realName.indexOf("[") + 1, realName.indexOf("]")));
								}
							
							} catch (NumberFormatException exc) {
							
								String appo = realName.substring(realName.indexOf("[") + 1, realName.indexOf("]"));							
								 if(appo.indexOf("@")!=-1){
									    if(appo.indexOf("' and @")!=-1){
											appoAttributesList=new ArrayList<Attribute>();		
											String appoName2 = appo;		
											while(appoName2.indexOf("' and @")!=-1 ){		    
												appoName2=appoName2.substring(0,appoName2.toLowerCase().indexOf("' and @"))+"'~@"+appoName2.substring(appoName2.toLowerCase().indexOf("' and @")+"' and @".length(),appoName2.length());		    
											} 
											StringTokenizer stringTokenizer2 = new StringTokenizer(appoName2,"~");    
											 while(stringTokenizer2.hasMoreTokens()){    
												String currentName = stringTokenizer2.nextToken();			
												String appoName = currentName.substring(currentName.indexOf("@") + 1, currentName.indexOf("="));
												String appoValue = currentName.substring(currentName.indexOf("'") + 1, currentName.lastIndexOf("'"));
												if(currentName.indexOf("child::")!=-1){
													String appoName3 = currentName.substring(appo.indexOf("child::")+"child::".length(), currentName.indexOf("=")).trim();
													String appoValue3 = currentName.substring(appo.indexOf("'") + 1, currentName.lastIndexOf("'"));
													String newNode=currentXPath.trim()+"/"+realName.substring(0,realName.indexOf("[")).trim();									
													List<?> list=dom4JDocument.selectNodes(newNode);									
													if(list!=null){
														
														insertNodeEscaped(newNode+"/"+appoName3,appoValue3,isCData);
																							
													}
													else{
														insertNodeEscaped(newNode,"",isCData);
													}
												}
												appoAttribute = new Attribute(appoName.trim(),appoValue);
												appoAttributesList.add(appoAttribute);	
											 }
										}else{
											
											String appoName = appo.substring(appo.indexOf("@") + 1, appo.indexOf("="));
											String appoValue = appo.substring(appo.indexOf("'") + 1, appo.lastIndexOf("'"));
											if(appoName.toLowerCase().startsWith("xmlns")){
												apponNamespace = Namespace.get(appoName, appoValue);
											}else{
												appoAttribute = new Attribute(appoName.trim(),appoValue);
											}
											
										}
								 }else{
									 if(appo.indexOf("child::")!=-1){
										String appoName = appo.substring(appo.indexOf("child::")+"child::".length(), appo.indexOf("=")).trim();
										String appoValue = appo.substring(appo.indexOf("'") + 1, appo.lastIndexOf("'"));
										String newNode=currentXPath.trim()+"/"+realName.substring(0,realName.indexOf("[")).trim();									
										List<?> list=dom4JDocument.selectNodes(newNode);									
										if(list!=null){
											if(dom4JDocument.selectSingleNode(currentXPath+"/"+nodeName)==null){										   
												insertNodeEscaped(newNode+"["+list.size()+1+"]"+"/"+appoName,appoValue,isCData);
											}
											else{
												insertNodeEscaped(newNode+"/"+appoName,appoValue,isCData);
											}									
										}
										else{
											insertNodeEscaped(newNode,"",isCData);
										}
									}else if(appo.indexOf("=")!=-1){
										String appoName = appo.substring(0, appo.indexOf("=")).trim();
										String appoValue = appo.substring(appo.indexOf("'") + 1, appo.lastIndexOf("'"));
										String newNode=currentXPath.trim()+"/"+realName.substring(0,realName.indexOf("[")).trim();									
										List<?> list=dom4JDocument.selectNodes(newNode);									
										if(list!=null){
											if(dom4JDocument.selectSingleNode(currentXPath+"/"+nodeName)==null){										   
												insertNodeEscaped(newNode+"["+list.size()+1+"]"+"/"+appoName,appoValue,isCData);
											}
											else{
												insertNodeEscaped(newNode+"/"+appoName,appoValue,isCData);
											}									
										}else{
											insertNodeEscaped(newNode,"",isCData);
										}
									}	
								 }
							}
							realName = realName.substring(0, realName.indexOf("["));
					
						}
						Node testNode = null;
						try {
							testNode = dom4JDocument.selectSingleNode(currentXPath + "/" + nodeName);
						} catch (XPathException e2) {
						}
						if (testNode == null && count>1) {
							if (realName.indexOf("@") == -1) {							
								try {
									if(!currentXPath.trim().equals("")){
										List<Node> nodeList = dom4JDocument.selectNodes(currentXPath);
										//System.err.println(currentXPath);
										for (int j = 0; j < nodeList.size(); j++) {
											Node node = null;
											if (!realName.trim().equalsIgnoreCase("text()")) {
												node = DocumentHelper.createElement(realName);
												if (count == tot) {
													if(isCData){
														((Element)node).add(DocumentHelper.createCDATA(nodeValue));
													}else{
														node.setText(nodeValue);
													}
													
												}
											} else {
												if(isCData){
													node = DocumentHelper.createCDATA(nodeValue);
												}else{
													node = DocumentHelper.createText(nodeValue);
												}
												
											}							
											if (appoAttributesList != null) {
												for(int xx=0;xx<appoAttributesList.size();xx++)
												    ((Element)node).addAttribute( ((Attribute)appoAttributesList.get(xx)).getAttributeName() , ((Attribute)appoAttributesList.get(xx)).getAttributeValue());
												
											}else if (appoAttribute != null) {
												((Element)node).addAttribute(appoAttribute.getAttributeName(),appoAttribute.getAttributeValue());
											}else if (apponNamespace != null) {
												((Element)node).add(apponNamespace);
											}
											((Element)dom4JDocument.selectNodes(currentXPath).get(j)).add(node);
										}
										//((Element)dom4JDocument.selectSingleNode(currentXPath)).add(node);
									
									}
								} catch (NullPointerException e1) {								
									try{			
										Node node = null;
										if (!realName.trim().equalsIgnoreCase("text()")) {
											node = DocumentHelper.createElement(realName);
											if (count == tot) {
												if(isCData){
													((Element)node).add(DocumentHelper.createCDATA(nodeValue));
												}else{
													node.setText(nodeValue);
												}
												
											}
										} else {
											if(isCData){
												node = DocumentHelper.createCDATA(nodeValue);
											}else{
												node = DocumentHelper.createText(nodeValue);
											}
											
										}							
										if (appoAttributesList != null) {
											for(int xx=0;xx<appoAttributesList.size();xx++)
											    ((Element)node).addAttribute( ((Attribute)appoAttributesList.get(xx)).getAttributeName() , ((Attribute)appoAttributesList.get(xx)).getAttributeValue());
											
										}else if (appoAttribute != null) {
											((Element)node).addAttribute(appoAttribute.getAttributeName(),appoAttribute.getAttributeValue());
										}else if (apponNamespace != null) {
											((Element)node).add(apponNamespace);
										}
										int wrongNum=Integer.parseInt(currentXPath.substring(currentXPath.lastIndexOf("[")+1,currentXPath.lastIndexOf("]")));
										wrongNum=wrongNum-1;
										while(wrongNum!=0){
											currentXPath=currentXPath.substring(0,currentXPath.lastIndexOf("[")+1)+Integer.toString(wrongNum)+currentXPath.substring(currentXPath.lastIndexOf("]"),currentXPath.length());
											try{
												((Element)dom4JDocument.selectSingleNode(currentXPath)).add(node);
												break;
											}catch(NullPointerException exc){}
											wrongNum=wrongNum-1;
										}
									}
									catch(StringIndexOutOfBoundsException stex){								
									}
									catch(NumberFormatException stex){								
									}
								}										
							} else {
								if(realName.substring(realName.indexOf("@") + 1).toLowerCase().startsWith("xmlns")){
									String namespaceName = "";
									try{
										namespaceName = realName.substring(realName.indexOf("@xmlns:") + "@xmlns:".length());
									}catch (StringIndexOutOfBoundsException e) {
									}
									Namespace namespace = new Namespace(namespaceName, nodeValue);
									try{
										   ((Element) dom4JDocument.selectSingleNode(currentXPath)).add(namespace);
									}catch(NullPointerException exc){
										try{										   
											int wrongNum=Integer.parseInt(currentXPath.substring(currentXPath.lastIndexOf("[")+1,currentXPath.lastIndexOf("]")));
											wrongNum=wrongNum-1;
											while(wrongNum!=0){
												currentXPath=currentXPath.substring(0,currentXPath.lastIndexOf("[")+1)+Integer.toString(wrongNum)+currentXPath.substring(currentXPath.lastIndexOf("]"),currentXPath.length());
												try{
												((Element) dom4JDocument.selectSingleNode(currentXPath)).add(namespace);
													 break;
												}catch(NullPointerException exct){}
												wrongNum=wrongNum-1;
											}
										}
										catch(StringIndexOutOfBoundsException stex){										
										}
										catch(NumberFormatException stex){										
										}	
									}
								}else{
										Attribute attribute = new Attribute();
										attribute.setAttributeName(realName.substring(realName.indexOf("@") + 1));
										if (count == tot) {
											attribute.setAttributeValue(nodeValue);
										}
										try{
										   ((Element) dom4JDocument.selectSingleNode(currentXPath)).addAttribute(attribute.getAttributeName(),attribute.getAttributeValue());
										}
										catch(NullPointerException exc){
												try{										   
													int wrongNum=Integer.parseInt(currentXPath.substring(currentXPath.lastIndexOf("[")+1,currentXPath.lastIndexOf("]")));
													wrongNum=wrongNum-1;
													while(wrongNum!=0){
														currentXPath=currentXPath.substring(0,currentXPath.lastIndexOf("[")+1)+Integer.toString(wrongNum)+currentXPath.substring(currentXPath.lastIndexOf("]"),currentXPath.length());
														try{
														((Element) dom4JDocument.selectSingleNode(currentXPath)).addAttribute(attribute.getAttributeName(),attribute.getAttributeValue());
															 break;
														}catch(NullPointerException exct){}
														wrongNum=wrongNum-1;
													}
												}
												catch(StringIndexOutOfBoundsException stex){										
												}
												catch(NumberFormatException stex){										
												}
										}
								}	
							}
						}
						currentXPath += "/" + nodeName;
						dom4JDocument = DocumentHelper.parseText(dom4JDocument.asXML());
					} catch (Exception e) {
						throw new XMLException(e.getMessage());
					} 
				}
				
			} else {
				throw new XMLException("Invalid xPath value [" + xPath + "]");
			}
			
		}
	public void insertValueAt(String xPath, String val,boolean isCData) throws XMLException {
		insertValueAtInternal(xPath,val,isCData);
	}
	public void insertValueAt(String xPath, String val) throws XMLException {
		insertValueAtInternal(xPath,val,false);
	}
	private void insertValueAtInternal(String xPath, String val,boolean isCData) throws XMLException {
		String value = StringEscapeUtils.escapeHtml4(val);
		try {			
			if(xPath.trim().endsWith("text()")){
				List<?> textList= dom4JDocument.selectNodes(xPath);
				for(int i=0;i<textList.size();i++){
					((Node)textList.get(i)).setText("");
				}
				dom4JDocument.selectSingleNode(xPath).setText(value);
			}else{
				dom4JDocument.selectSingleNode(xPath).setText(value);
			}
		} catch (NullPointerException e) {
			insertNodeEscaped(xPath, value,isCData);
		} catch (Exception e) {
			throw new XMLException(e.getMessage());
		}
	}
	public Document deleteNode(Document aDoc, String xPath) throws XMLException {
		//String app = xPath;
		Document doc = aDoc;
		if (xPath != null && !xPath.trim().equals("")) {
			try {
				List<?> list = aDoc.selectNodes( xPath);
				if (list != null && list.size() != 0) {
					for (int i = 0; i < list.size(); i++) {
						Node node = (Node)list.get(i);
						node.getParent().remove(node);
					}
				}
				doc.normalize();
			} catch (Exception e) {
				e.printStackTrace();
				throw new XMLException(e.getMessage());
			}
		}
		return doc;
	}
	public void deleteNode(String xPath) throws XMLException {
		if (xPath != null && !xPath.trim().equals("")) {
			try {
				List<?> list = dom4JDocument.selectNodes(xPath);
				if (list != null && list.size() != 0) {
					for (int i = 0; i < list.size(); i++) {
						Node node =  (Node)list.get(i);
						node.getParent().remove(node);
					}
				}
				dom4JDocument.normalize();
			} catch (Exception e) {
				e.printStackTrace();
				throw new XMLException(e.getMessage());
			}
		}
	}
	public String getXML(String encoding,boolean indent) throws XMLException {
		if(dom4JDocument!=null){
			OutputFormat outputFormat = new OutputFormat();
			outputFormat.setEncoding(encoding);
			StringWriter stringWriter = new StringWriter();
			XMLWriter xmlWriter = null;
			if(indent){		
			    outputFormat.setNewlines(true);
			    outputFormat.setIndent(true);
			    outputFormat.setIndentSize(2);							    
			    xmlWriter = new XMLWriter(stringWriter, outputFormat);
			    xmlWriter.setMaximumAllowedCharacter(255);
			    try {
					xmlWriter.write(dom4JDocument);
					xmlWriter.flush();
					xmlWriter.close();
					stringWriter.flush();
					stringWriter.close();
				} catch (IOException e) {
					throw new XMLException(e.getMessage());
				}
			    return stringWriter.toString();
			}else{
			    xmlWriter = new XMLWriter(stringWriter, outputFormat);
			    xmlWriter.setMaximumAllowedCharacter(255);
			    try {
					xmlWriter.write(dom4JDocument);
					xmlWriter.flush();
					xmlWriter.close();
					stringWriter.flush();
					stringWriter.close();
				} catch (IOException e) {
					throw new XMLException(e.getMessage());
				}
				return stringWriter.toString();
			}
		}else{
			throw new XMLException("Invalid operation! Document = null");
		}
	}	
	public String getXML(String encoding) throws XMLException {
			return getXML(encoding,false);
	}	
	public org.jdom2.Document getJDomDocument() throws XMLException {
		org.dom4j.io.DOMWriter d4Writer = new org.dom4j.io.DOMWriter();
		try {
			DomDocument = d4Writer.write(dom4JDocument);
		} catch (DocumentException e) {
			throw new XMLException(e.getMessage());
		}
		JDomDocument = new DOMBuilder().build(DomDocument);
		if (JDomDocument != null)
			return JDomDocument;
		else
			throw new XMLException("Invalid operation! Document = null");
	}
	public org.w3c.dom.Document getDomDocument() throws XMLException {
		try {
			org.dom4j.io.DOMWriter d4Writer = new org.dom4j.io.DOMWriter();
			org.w3c.dom.Document domDocument = d4Writer.write(dom4JDocument);
			return domDocument;
		} catch (Exception e) {
			throw new XMLException(e.getMessage());
		}
	}
	private String testXPath(String xpath){
		String result="";
		try {
			String xpathToTest = xpath.replaceAll("\"", "'");
			if (xpathToTest.indexOf("[") != -1) {
			   int start=xpathToTest.indexOf("[") + 1;
			   int end=xpathToTest.indexOf("]");
			   String toTest=xpathToTest.substring(xpathToTest.indexOf("[") + 1, xpathToTest.indexOf("]"));
			   try{
					Integer.parseInt(toTest);
				    result=xpathToTest;
			   }catch (NumberFormatException e) {
						toTest=toTest.replace('/','~');
						result=xpathToTest.substring(0,start)+toTest+xpathToTest.substring(end,xpathToTest.length());
			   }
			}
			else{
				result=xpathToTest;
			}
		} catch (StringIndexOutOfBoundsException e) {
			throw e;			
		}
		return result;
	}
	public boolean testNode(String xPath){
			Node node = dom4JDocument.selectSingleNode(xPath);
			boolean result = false;
			if (node != null) {
				if (node.getText() != null && node.getText() != "") {
					result = true;
				} else {
					result = false;
				}
			} else {
				result = false;
			}
			return result;
		}

		public int countNodes(String xPath){
			int nodes = 0;
			List<?> nodeList = dom4JDocument.selectNodes(xPath);
			if (nodeList != null) {
				nodes = nodeList.size();
			}
			return nodes;
		}
		private String getValue(String xPath){
			String value = "";
			if (xPath.endsWith("/text()")) {
				List<?> nodeList = dom4JDocument.selectNodes(xPath);
				for (int i = 0; i < nodeList.size(); i++) {
					try {
						if(((Node)nodeList.get(i)).getText().indexOf("\\n")!=-1)
							value += ((Node)nodeList.get(i)).getText() + "\n";
						else		
							value += ((Node)nodeList.get(i)).getText();	
					} catch (NullPointerException exc) {
					}
				}
			} else {
				Node ilNodo = dom4JDocument.selectSingleNode(xPath);
				if (ilNodo != null) {
					value = ilNodo.getText();
				} else {
					value = "";
				}
			}
			if (value.equals(null)) {
				value = "";
			}
			OutputFormat outputFormat = new OutputFormat();
			if(dom4JDocument.getXMLEncoding()!=null)
				outputFormat.setEncoding(dom4JDocument.getXMLEncoding());
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
			return stringWriter.toString();						
		}
	    
		public String getNodeValue(String xPath){
			String value = getValue(xPath);
			value = ((value.replaceAll("\"", "&#34;")).replaceAll("&#60;", "<")).replaceAll("&#62;", ">");
			return value.trim();
		}

		public String getNodeValue(String xPath, boolean doConvert){
			String value = getValue(xPath);
			if (doConvert)
				value = ((value.replaceAll("\"", "&#34;")).replaceAll("&#60;", "<")).replaceAll("&#62;", ">");
			return value.trim();
		}

		public String getNodeValue(String xPath, String returnChar){
			String value = getValue(xPath);
			value = ((value.replaceAll("\"", "&#34;")).replaceAll("&#60;", "<")).replaceAll("&#62;", ">");
			return (value.replaceAll("\n", returnChar)).trim();
		}

		public String getHTMLNodeValue(String xPath, String returnChar){
			String value = getValue(xPath);
			value = value.replaceAll("\"", "&quot;");
			value = value.replaceAll("<em>", "IniZioTagHtMlemFinETagHtMl");
			value = value.replaceAll("</em>", "IniZioTagHtMl/emFinETagHtMl");
			value = value.replaceAll("<EM>", "IniZioTagHtMlemFinETagHtMl");
			value = value.replaceAll("</EM>", "IniZioTagHtMl/emFinETagHtMl");
			value = value.replaceAll("<P>", "IniZioTagHtMlpFinETagHtMl");
			value = value.replaceAll("</P>", "IniZioTagHtMl/pFinETagHtMl");
			value = value.replaceAll("<p>", "IniZioTagHtMlpFinETagHtMl");
			value = value.replaceAll("</p>", "IniZioTagHtMl/pFinETagHtMl");
			value = value.replaceAll("<STRONG>", "IniZioTagHtMlstrongFinETagHtMl");
			value = value.replaceAll("</STRONG>", "IniZioTagHtMl/strongFinETagHtMl");
			value = value.replaceAll("<strong>", "IniZioTagHtMlstrongFinETagHtMl");
			value = value.replaceAll("</strong>", "IniZioTagHtMl/strongFinETagHtMl");
			value = value.replaceAll("<br>", "IniZioTagHtMlbrFinETagHtMl");
			value = value.replaceAll("<BR>", "IniZioTagHtMlbrFinETagHtMl");
			value = (value.replaceAll("&#60;", "<")).replaceAll("&#62;", ">");
			value = (value.replaceAll("IniZioTagHtMl", "<")).replaceAll("FinETagHtMl", ">");
			return (value.replaceAll("\n", returnChar)).trim();
		}

		public String getHTMLNodeValue(String xPath){
			String value = getValue(xPath);
			value = value.replaceAll("\"", "&quot;");
			value = value.replaceAll("<em>", "IniZioTagHtMlemFinETagHtMl");
			value = value.replaceAll("</em>", "IniZioTagHtMl/emFinETagHtMl");
			value = value.replaceAll("<EM>", "IniZioTagHtMlemFinETagHtMl");
			value = value.replaceAll("</EM>", "IniZioTagHtMl/emFinETagHtMl");
			value = value.replaceAll("<P>", "IniZioTagHtMlpFinETagHtMl");
			value = value.replaceAll("</P>", "IniZioTagHtMl/pFinETagHtMl");
			value = value.replaceAll("<p>", "IniZioTagHtMlpFinETagHtMl");
			value = value.replaceAll("</p>", "IniZioTagHtMl/pFinETagHtMl");
			value = value.replaceAll("<STRONG>", "IniZioTagHtMlstrongFinETagHtMl");
			value = value.replaceAll("</STRONG>", "IniZioTagHtMl/strongFinETagHtMl");
			value = value.replaceAll("<strong>", "IniZioTagHtMlstrongFinETagHtMl");
			value = value.replaceAll("</strong>", "IniZioTagHtMl/strongFinETagHtMl");
			value = value.replaceAll("<br>", "IniZioTagHtMlbrFinETagHtMl");
			value = value.replaceAll("<BR>", "IniZioTagHtMlbrFinETagHtMl");
			value = (value.replaceAll("&#60;", "<")).replaceAll("&#62;", ">");
			value = (value.replaceAll("IniZioTagHtMl", "<")).replaceAll("FinETagHtMl", ">");
			return value.trim();
		}

		public String getNodeValue(String xPath, String returnChar, String toHighlight){
			String value = getValue(xPath);
			value = ((value.replaceAll("\"", "&#34;")).replaceAll("&#60;", "<")).replaceAll("&#62;", ">");
			String[] strToHighlight = toHighlight.split(",");
			for (int i = 0; i < strToHighlight.length; i++) {
				value = value.replaceAll(strToHighlight[i], "<span class=\""+htmlTagClass+"\">" + strToHighlight[i] + "</span>");
			}
			return (value.replaceAll("\n", returnChar)).trim();
		}
		private class Attribute {
		   private String attributeName="";
		   private String attributeValue="";
		public Attribute() {

		}
		public Attribute(String attributeName,String attributeValue) {
			this.attributeName = attributeName;
			this.attributeValue = attributeValue;
		}
		public String getAttributeName() {
			return attributeName;
		}

		/**
		 * @return
		 */
		public String getAttributeValue() {
			return attributeValue;
		}

		/**
		 * @param string
		 */
		public void setAttributeName(String string) {
			attributeName = string;
		}

		/**
		 * @param string
		 */
		public void setAttributeValue(String string) {
			attributeValue = string;
		}

		}
	/**
	 * @return
	 */
	public String getHtmlTagClass() {
		return htmlTagClass;
	}

	/**
	 * @param string
	 */
	public void setHtmlTagClass(String string) {
		htmlTagClass = string;
	}
	
	public void addNodeNamespaceURIs(String xPath,Map<?, ?> uris) throws XMLException {
		List<?> list = dom4JDocument.selectNodes(xPath);
		if(list!=null && list.size()>0){
			Set<?> set = uris.keySet();
			for (int i = 0; i < list.size(); i++) {
			    Iterator<?> iterator = set.iterator();		
				while(iterator.hasNext()){
					String keyName = (String) iterator.next();
					String keyValue = (String) uris.get(keyName);
					((Element)list.get(i)).add(Namespace.get(keyName, keyValue));
				}
	        }
		}
	}
	public void removeEmptyNodes() throws XMLException{		
		try {
			removeEmptyTextNode();
			List<?> list = dom4JDocument.selectNodes("//*[not(node())]");
			if (list != null && list.size() != 0) {
				for (int i = 0; i < list.size(); i++) {
					Node node =  (Node)list.get(i);
					node.getParent().remove(node);
				}
			}
			dom4JDocument.normalize();
		} catch (Exception e) {
			e.printStackTrace();
			throw new XMLException(e.getMessage());
		}
	}
	public void removeEmptyTextNode() throws XMLException{
		try {
			
			List<?> list = dom4JDocument.selectNodes("//text()");
			if (list != null && list.size() != 0) {
				for (int i = 0; i < list.size(); i++) {
					Node node =  (Node)list.get(i);
	                String value = node.getText().trim();
	                if(value.equals("")){
	                	node.getParent().remove(node);
	                }
				}
			}
			dom4JDocument.normalize();
		} catch (Exception e) {
			e.printStackTrace();
			throw new XMLException(e.getMessage());
		}
	}
	public void addNameSpace(String prefix,String uri){
		dom4JDocument.getRootElement().add(new Namespace(prefix, uri));
	}
	public void testEscapeXML() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OutputFormat format = new OutputFormat(null, false, "ISO-8859-2");
        format.setSuppressDeclaration(true);

        XMLWriter writer = new XMLWriter(os, format);

        Document document = DocumentFactory.getInstance().createDocument();
        Element root = document.addElement("root");
        root.setText("bla &#c bla");

        writer.write(document);

        String result = os.toString();

        Document doc2 = DocumentHelper.parseText(result);
        doc2.normalize(); 
    }

		private String parseAttribute(String xml){
			String[] strings = xml.split("[\\<]");
			String result="";
			for (int i = 0; i < strings.length; i++) {
				if(!strings[i].trim().equals("")){
						String node="<"+strings[i];
						String toTest=node.substring(node.indexOf("<"), node.indexOf(">")+1);
						if(toTest.indexOf("\"")!=-1){//se ha attributi
							char[] chars = toTest.toCharArray();
							ArrayList<CharUtil> vector = new ArrayList<CharUtil>();							
							for (int j = 0; j < chars.length; j++) {
								char c = chars[j];
								if(c=='<' || c=='>' || c=='=' || c=='"'){
									vector.add(new CharUtil(new Character(c).toString(),j));
								}
							}
							int lastPosition = -1;
							boolean start = false; 
							for (int j = 0; j < vector.size(); j++) {
								CharUtil charUtil = (CharUtil)vector.get(j);
								if(charUtil.c.equals("\"")){
								   if(start==false){
									   start=true;
								   }else{
									   lastPosition=j;
									   charUtil.c="&quot;"; 
									   vector.set(j, charUtil);
								   }
								}else if(charUtil.c.equals("=") || charUtil.c.equals(">")){	
									if(lastPosition!=-1){
										((CharUtil)vector.get(lastPosition)).c="\"";
										start = false;
									}
								}								
							}
							String parsed="";
							for (int k = 0; k < chars.length; k++) {
								boolean addChar=true;
								String addString="";
								for (int j = 0; j < vector.size(); j++) {
								     CharUtil charUtil = (CharUtil)vector.get(j);
								     if(k==charUtil.position){
								    	 addString=charUtil.c;
										 addChar=false;
										 break;
									 }else{
										 addChar=true;
									 }						
								}
								if(addChar)
									parsed+=chars[k];
								else
									parsed+=addString;
							}
							result+=parsed;
						}else{
							result+=toTest;
						}
						try {
							result+=node.substring(node.indexOf(">")+1,node.length());
						} catch (StringIndexOutOfBoundsException e) {							
						}
				}
			}		
			return result;
		}
		private class CharUtil {
			protected String c;
			protected int position=-1;
			public CharUtil(String c,int position) {
               this.c=c;
               this.position=position;
			}
		}
}
