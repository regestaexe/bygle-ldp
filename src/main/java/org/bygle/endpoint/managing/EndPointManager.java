package org.bygle.endpoint.managing;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.commons.configuration.ConfigurationException;
import org.bygle.utils.BygleSystemUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.ServletContextAware;

public abstract class EndPointManager implements EndPointManagerInterface,ServletContextAware {
	
	public String defaultDomain;
	
	public String importDirectory;
	
	public String publishDirectory;
	
	public ServletContext servletConext;
	

	@Override
	public void publishRecord(byte[] rdf,String rdfAbout) throws Exception {}

	@Override
	public void dePublishRecord(byte[] rdf,String rdfAbout) throws Exception {}

	@Override
	public void rePublishRecord(byte[] rdf,String rdfAbout) throws Exception {}
	
	@Override
	public ResponseEntity<?> query(String defaultGraphUri,String sparqlQuery,int outputFormat) throws Exception{ return null;}

	@Override
	public void executeImport() throws Exception {}
	
	@Override
	public void executePublishing() throws Exception {}
	
	@Override
	public void resetEndpoint() throws Exception {}
	
	@Override
	public void dropEndpoint() throws Exception {}

	public String getDefaultDomain() {
		return defaultDomain;
	}
	
	public void setDefaultDomain(String defaultDomain) {
		this.defaultDomain = defaultDomain;
	}
	
	public String getImportDirectory() {
		return importDirectory;
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletConext = servletContext;
		try {
			File importDirectory = new File(BygleSystemUtils.getStringProperty("endpoint.import.directory"));
			if(importDirectory.isAbsolute()){
				this.importDirectory = BygleSystemUtils.getStringProperty("endpoint.import.directory");
			}else{
				this.importDirectory = servletContext.getRealPath("")+"/"+BygleSystemUtils.getStringProperty("endpoint.import.directory");
			}
			File publishDirectory = new File(BygleSystemUtils.getStringProperty("endpoint.publish.directory"));
			if(publishDirectory.isAbsolute()){
				this.publishDirectory = BygleSystemUtils.getStringProperty("endpoint.publish.directory");
			}else{
				this.publishDirectory = servletContext.getRealPath("")+"/"+BygleSystemUtils.getStringProperty("endpoint.publish.directory");
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		
	}

}
