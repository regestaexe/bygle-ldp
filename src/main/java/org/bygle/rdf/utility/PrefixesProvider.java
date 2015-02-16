package org.bygle.rdf.utility;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.bygle.xml.XMLReader;
import org.dom4j.DocumentException;
import org.springframework.web.context.ServletContextAware;


public class PrefixesProvider implements ServletContextAware {

	private ServletContext servletContext;
	private boolean use_external_conf_location = false;
	private String configuration_location = "/WEB-INF/configuration/ontology";
	private String file_name = "prefixes.xml";
	private String real_path = "";

	private static HashMap<String, String> prefixes = new HashMap<String, String>();
	private static HashMap<String, String> baseIRIs = new HashMap<String, String>();
	private static String prefixesDeclaration = "";
	private static String mainBase = "";;
	private static String mainPrefix = "";

	private void generateMaps(XMLReader xmlReader) {
		int count = xmlReader.getNodeCount("/prefixes/prefix");
		prefixesDeclaration = "";
		for (int i = 0; i < count; i++) {
			String abbr = xmlReader.getNodeValue("/prefixes/prefix[" + (i + 1) + "]/@abbr");
			String prefix = xmlReader.getNodeValue("/prefixes/prefix[" + (i + 1) + "]/text()").trim();
			baseIRIs.put(prefix, abbr);
			prefixes.put(abbr, prefix);
			if (!abbr.equals("") && !prefix.equals("")) {
				prefixesDeclaration += " xmlns:" + abbr + "=\"" + prefix + "\"";
			}

		}
		mainBase = xmlReader.getNodeValue("/prefixes/mainBase/text()").trim();
		mainPrefix = xmlReader.getNodeValue("/prefixes/mainPrefix/text()").trim();
	}

	public void setServletContext(ServletContext arg0) {
		servletContext = arg0;
		if (!use_external_conf_location)
			real_path = servletContext.getRealPath("");
		try {
			if (configuration_location != null) {
				String path = real_path + configuration_location + "/" + file_name;
				if (use_external_conf_location) {
					path = configuration_location + "/" + file_name;
				}
				File fileConf = new File(path);
				String xml = FileUtils.readFileToString(fileConf, "UTF-8");
				XMLReader xmlReader = new XMLReader(xml);
				generateMaps(xmlReader);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, String> getPrefixesMap() {
		return prefixes;
	}

	public static String getPrefixesDeclaration() {

		return prefixesDeclaration;

	}

	public void setUse_external_conf_location(boolean use_external_conf_location) {
		this.use_external_conf_location = use_external_conf_location;
	}

	public void setConfiguration_location(String configuration_location) {
		this.configuration_location = configuration_location;
	}

	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}

	public void setReal_path(String real_path) {
		this.real_path = real_path;
	}

	public static String getBaseIRI(String prefix) {
		return baseIRIs.get(prefix);
	}

	public static String getPrefix(String baseIRI) {
		return prefixes.get(baseIRI);
	}

	public static String getMainBase() {
		return mainBase;
	}

	public static String getMainPrefix() {
		return mainPrefix;
	}

}
