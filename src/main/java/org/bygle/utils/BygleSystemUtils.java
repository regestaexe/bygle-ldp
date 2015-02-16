package org.bygle.utils;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.trans.CompilerInfo;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.sdb.store.DatabaseType;


@Component("BygleSystemUtils")
public class BygleSystemUtils {
	public final static String defaultOutputFormat = "text/turtle";
	public final static String defaultWriter = "TURTLE";
	
	public final static String acceptPostData = "text/turtle, application/turtle, application/x-turtle, application/rdf+xml, application/rdf+xml-abbr, application/rdf+json, application/ld+json, application/json, application/n-triples, text/rdf+n3, application/n3, text/n3, image/bmp, image/jpeg";
	public final static String acceptPatchData = "application/rdf+xml-abbr";
	public final static String acceptPostFile = "image/bmp, image/jpeg, application/octet-stream, text/plain";
	public final static AcceptList offeringRDF = new AcceptList(acceptPostData);
	public final static String[] allowedPostRDFMethods = {"GET","POST","PUT","DELETE","OPTIONS","HEAD","PATCH"};
	public final static String[] allowedPostContainerMethods = {"GET","POST","PUT","DELETE","OPTIONS","HEAD","PATCH"};
	public final static String[] allowedPostNonRDFMethods = {"GET","POST","PUT","DELETE","OPTIONS","HEAD"};
	
	public static final int RESOURCE_TYPE_RDF_CONTAINER = 1;
	public static final int RESOURCE_TYPE_RDF_BASIC_CONTAINER = 2;
	public static final int RESOURCE_TYPE_RDF_DIRECT_CONTAINER = 3;
	public static final int RESOURCE_TYPE_RDF_INDIRECT_CONTAINER = 4;
	public static final int RESOURCE_TYPE_RDF_RESOURCE = 5;
	public static final int RESOURCE_TYPE_BINARY = 6;
	
	
	public static final int ACTION_POST = 1;
	public static final int ACTION_PUT = 2;
	public static final int ACTION_PATCH = 3;
	public static final int ACTION_DELETE = 4;
	
	
	public static final int OUTPUTFORMAT_BIO = 1;
	public static final int OUTPUTFORMAT_CSV = 2;
	public static final int OUTPUTFORMAT_JSON = 3;
	public static final int OUTPUTFORMAT_RDF = 4;
	public static final int OUTPUTFORMAT_RDF_ABBR = 5;
	public static final int OUTPUTFORMAT_TSV = 6;
	public static final int OUTPUTFORMAT_XML = 7;
	public static final int OUTPUTFORMAT_N_TRIPLE = 8;
	public static final int OUTPUTFORMAT_TURTLE = 9;
	public static final int OUTPUTFORMAT_HTML = 10;
	
	
	public static final String INPUTFORMAT_TEXT_TURTLE = "text/turtle";
	public static final String INPUTFORMAT_APPLICATION_RDF_XML = "application/rdf+xml";
	public static final String INPUTFORMAT_APPLICATION_JSON = "application/json";
	public static final String INPUTFORMAT_APPLICATION_LD_JSON = "application/ld+json";
	public static final String INPUTFORMAT_APPLICATION_TURTLE = "application/turtle";
	public static final String INPUTFORMAT_APPLICATION_X_TURTLE = "application/x-turtle";
	public static final String INPUTFORMAT_APPLICATION_RDF_XML_ABBR = "application/rdf+xml-abbr";
	public static final String INPUTFORMAT_APPLICATION_RDF_JSON = "application/rdf+json";
	public static final String INPUTFORMAT_APPLICATION_NTRIPLES = "application/n-triples";
	public static final String INPUTFORMAT_TEXT_RDF_N3 = "text/rdf+n3";
	public static final String INPUTFORMAT_APPLICATION_N3 = "application/n3";
	public static final String INPUTFORMAT_TEXT_N3 = "text/n3";
	public static final String INPUTFORMAT_IMAGE_BMP = "image/bmp";
	public static final String INPUTFORMAT_IMAGE_JPEG = "image/jpeg";
	
	
	
	private static final String PROPERTIES_FILE_NAME = "bygle.properties";
	private static PropertiesConfiguration propertiesConfiguration;
	
	private static Configuration configuration = Configuration.newConfiguration();
	private static Configuration htmlconfiguration = Configuration.newConfiguration();
	private static Controller controller;
	private static Controller htmlController;
    
	public static PropertiesConfiguration getPropertiesConfiguration() throws ConfigurationException {
		loadProperties();
		return propertiesConfiguration;
	}
	public static String getStringProperty(String key) throws ConfigurationException{
    	loadProperties();
    	return propertiesConfiguration.getString(key).trim();
    }
    public static boolean getBooleanProperty(String key) throws ConfigurationException{
    	loadProperties();
    	return propertiesConfiguration.getBoolean(key);
    }
    public static int getIntProperty(String key) throws ConfigurationException{
    	loadProperties();
    	return propertiesConfiguration.getInt(key);
    }
    private static void loadProperties() throws ConfigurationException{
    	if(propertiesConfiguration==null){
    		propertiesConfiguration = new PropertiesConfiguration(BygleSystemUtils.class.getClassLoader().getResource(PROPERTIES_FILE_NAME));
    	}
    }
    
    public static Controller getXSLTController() throws ConfigurationException, TransformerConfigurationException{
    	if(controller==null){
    		Source styleSource = new StreamSource( BygleSystemUtils.class.getClassLoader().getResourceAsStream("sort-toUTF8.xsl"));
			CompilerInfo compilerInfo = configuration.getDefaultXsltCompilerInfo();
			PreparedStylesheet sheet = PreparedStylesheet.compile(styleSource, configuration, compilerInfo);
			controller = (Controller) sheet.newTransformer();
    	}
    	return controller;
    }
    
    public static Controller getXSLHTMLTController() throws ConfigurationException, TransformerConfigurationException{
    	if(htmlController==null){
    		Source styleSource = new StreamSource( BygleSystemUtils.class.getClassLoader().getResourceAsStream("xml-to-html.xsl"));
			CompilerInfo compilerInfo = htmlconfiguration.getDefaultXsltCompilerInfo();
			PreparedStylesheet sheet = PreparedStylesheet.compile(styleSource, htmlconfiguration, compilerInfo);
			htmlController = (Controller) sheet.newTransformer();
    	}
    	return htmlController;
    }
    
    public static String getWriter(String outputFormat){
		MediaType matchItemInput;
		try {
			String format = outputFormat.toLowerCase();
			AcceptList input = AcceptList.create(format.split(","));
			matchItemInput = AcceptList.match(BygleSystemUtils.offeringRDF, input);
			Lang lang = RDFLanguages.contentTypeToLang(matchItemInput.getContentType());
			return lang.getName();
		} catch (Exception e) {
			return defaultWriter;
		}
	}
    
    public static DatabaseType getDBType(String databaseType){
		switch (databaseType) {
			case "derby":
				return DatabaseType.Derby;
			case "H2":
				return DatabaseType.H2;
			case "HSQLDB":
				return DatabaseType.HSQLDB;
			case "MySQL":
				return DatabaseType.MySQL;
			case "PostgreSQL":
				return DatabaseType.PostgreSQL;
			case "SQLServer":
				return DatabaseType.SQLServer;
			case "Oracle":
				return DatabaseType.Oracle;
			case "DB2":
				return DatabaseType.DB2;
			case "sap":
				return DatabaseType.SAP;
			default:
				return null;
		}
	}
}
