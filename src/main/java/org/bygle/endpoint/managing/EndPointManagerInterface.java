package org.bygle.endpoint.managing;

import org.springframework.http.ResponseEntity;

public interface EndPointManagerInterface {

	public void publishRecord(byte[] rdf, String rdfAbout, String host) throws Exception;

	public void dePublishRecord(byte[] rdf, String rdfAbout, String host) throws Exception;

	public void rePublishRecord(byte[] rdf, String rdfAbout, String host) throws Exception;

	public ResponseEntity<?> query(String defaultGraphUri, String sparqlQuery, int outputFormat) throws Exception;

	public void executeImport() throws Exception;

	public void executePublishing() throws Exception;

	public void resetEndpoint() throws Exception;

	public void dropEndpoint() throws Exception;

}
