package org.bygle.xslt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.XPathException;

public class TrasformXslt {

	private static Configuration configuration = Configuration.newConfiguration();

	public TrasformXslt() {

	}
    
	
	public static String xslt(String xmlInput, Controller controller) throws Exception {
		String strResult = "";
		try {
			Source sourceInput = new StreamSource(new StringReader(xmlInput));
			StringWriter outWriter = new StringWriter();
			javax.xml.transform.Result result = new StreamResult(outWriter);
			try {
				controller.transform(sourceInput, result);
				strResult = outWriter.getBuffer().toString();
			} catch (XPathException err) {
				if (!err.hasBeenReported()) {
					err.printStackTrace();
				}
				throw new XPathException("Run-time errors were reported");
			}
		} catch (Exception e) {
			throw e;
		}
		return strResult;
	}
	
	
	public static String xslt(InputStream xmlInput, Controller controller) {
		String strResult = null;
		try {
			Source sourceInput = new StreamSource(xmlInput);
			StringWriter outWriter = new StringWriter();
			javax.xml.transform.Result result = new StreamResult(outWriter);
			try {
				controller.transform(sourceInput, result);
				strResult = outWriter.getBuffer().toString();
			} catch (XPathException err) {
				if (!err.hasBeenReported()) {
					err.printStackTrace();
				}
				throw new XPathException("Run-time errors were reported");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strResult;
	}
		
	public static String xslt(String xmlInput, InputStream xsltTrasform) throws Exception {
		String strResult = "";
		try {
			Source sourceInput = new StreamSource(new StringReader(xmlInput));
			Source styleSource = new StreamSource(xsltTrasform);
			CompilerInfo compilerInfo = configuration.getDefaultXsltCompilerInfo();
			PreparedStylesheet sheet = PreparedStylesheet.compile(styleSource, configuration, compilerInfo);
			Controller controller = (Controller) sheet.newTransformer();
			StringWriter outWriter = new StringWriter();
			javax.xml.transform.Result result = new StreamResult(outWriter);
			try {
				controller.transform(sourceInput, result);
				strResult = outWriter.getBuffer().toString();
			} catch (XPathException err) {
				if (!err.hasBeenReported()) {
					err.printStackTrace();
				}
				throw new XPathException("Run-time errors were reported");
			}
		} catch (Exception e) {
			throw e;
		}
		return strResult;
	}

	public String xslt(InputStream xmlInput, String xsltTrasform) {
		String strResult = null;
		try {
			Source sourceInput = new StreamSource(xmlInput);
			Source styleSource = new StreamSource(new StringReader(xsltTrasform));
			CompilerInfo compilerInfo = configuration.getDefaultXsltCompilerInfo();
			PreparedStylesheet sheet = PreparedStylesheet.compile(styleSource, configuration, compilerInfo);
			Controller controller = (Controller) sheet.newTransformer();
			StringWriter outWriter = new StringWriter();
			javax.xml.transform.Result result = new StreamResult(outWriter);
			try {
				controller.transform(sourceInput, result);
				strResult = outWriter.getBuffer().toString();
			} catch (XPathException err) {
				if (!err.hasBeenReported()) {
					err.printStackTrace();
				}
				throw new XPathException("Run-time errors were reported");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strResult;
	}
    
	public static String xslt(String xmlInput, String xsltTrasform) throws Exception {
		return xslt(xmlInput, xsltTrasform, null);
	}

	public static String xslt(InputStream xmlInput, InputStream xsltTrasform, Map<String, String> mapParams) throws Exception {
		String strResult = "";
		try {
			Source sourceInput = new StreamSource(xmlInput);
			Source styleSource = new StreamSource(xsltTrasform);
			CompilerInfo compilerInfo = configuration.getDefaultXsltCompilerInfo();
			PreparedStylesheet sheet = PreparedStylesheet.compile(styleSource, configuration, compilerInfo);
			Controller controller = (Controller) sheet.newTransformer();
			if (mapParams != null) {
				for (Entry<String, String> entry : mapParams.entrySet()) {
					controller.setParameter(entry.getKey(), entry.getValue());
				}
			}
			StringWriter outWriter = new StringWriter();
			javax.xml.transform.Result result = new StreamResult(outWriter);
			try {
				controller.transform(sourceInput, result);
				strResult = outWriter.getBuffer().toString();
			} catch (XPathException err) {
				if (!err.hasBeenReported()) {
					err.printStackTrace();
				}
				throw new XPathException("Run-time errors were reported");
			}
		} catch (Exception e) {
			throw e;
		}
		return strResult;
	}

	public static String xslt(String xmlInput, String xsltTrasform, Map<String, String> mapParams) throws Exception {
		String strResult = "";
		try {
			Source sourceInput = new StreamSource(new StringReader(xmlInput));
			Source styleSource = new StreamSource(new StringReader(xsltTrasform));
			CompilerInfo compilerInfo = configuration.getDefaultXsltCompilerInfo();
			PreparedStylesheet sheet = PreparedStylesheet.compile(styleSource, configuration, compilerInfo);
			Controller controller = (Controller) sheet.newTransformer();
			if (mapParams != null) {
				for (Entry<String, String> entry : mapParams.entrySet()) {
					controller.setParameter(entry.getKey(), entry.getValue());
				}
			}

			StringWriter outWriter = new StringWriter();
			javax.xml.transform.Result result = new StreamResult(outWriter);
			try {
				controller.transform(sourceInput, result);
				strResult = outWriter.getBuffer().toString();
			} catch (XPathException err) {
				if (!err.hasBeenReported()) {
					err.printStackTrace();
				}
				throw new XPathException("Run-time errors were reported");
			}
		} catch (Exception e) {
			throw e;
		}
		return strResult;
	}

	public static String xsltFromFile(String inFilename, String xslFilename) {
		String strResult = "";
		try {
			Source sourceInput = new StreamSource(new FileInputStream(inFilename));
			Source styleSource = new StreamSource(new FileInputStream(xslFilename));
			CompilerInfo compilerInfo = configuration.getDefaultXsltCompilerInfo();
			PreparedStylesheet sheet = PreparedStylesheet.compile(styleSource, configuration, compilerInfo);
			Controller controller = (Controller) sheet.newTransformer();
			StringWriter outWriter = new StringWriter();
			javax.xml.transform.Result result = new StreamResult(outWriter);
			try {
				controller.transform(sourceInput, result);
				strResult = outWriter.getBuffer().toString();
			} catch (XPathException err) {
				if (!err.hasBeenReported()) {
					err.printStackTrace();
				}
				throw new XPathException("Run-time errors were reported");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strResult;
	}

	public static void xsltFromFile(String inFilename, String xslFilename, String outFilename) {
		try {
			Source sourceInput = new StreamSource(new FileInputStream(inFilename));
			Source styleSource = new StreamSource(new FileInputStream(xslFilename));
			CompilerInfo compilerInfo = configuration.getDefaultXsltCompilerInfo();
			PreparedStylesheet sheet = PreparedStylesheet.compile(styleSource, configuration, compilerInfo);
			Controller controller = (Controller) sheet.newTransformer();
			javax.xml.transform.Result result = new StreamResult(new File(outFilename));
			try {
				controller.transform(sourceInput, result);
			} catch (XPathException err) {
				if (!err.hasBeenReported()) {
					err.printStackTrace();
				}
				throw new XPathException("Run-time errors were reported");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
