package org.bygle.service;

import org.bygle.endpoint.managing.EndPointManagerInterface;
import org.bygle.endpoint.managing.EndPointManagerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service("sparqlService")
public class SparqlService {
	
	@Autowired
	EndPointManagerProvider endPointManagerProvider;
	
	
	public ResponseEntity<?> query(String defaultGraphUri,String sparqlQuery,Integer outputFormat) throws Exception{
		EndPointManagerInterface endPointManager = endPointManagerProvider.getEndPointManager();
		try {
			return endPointManager.query(defaultGraphUri, sparqlQuery, outputFormat.intValue());
		} catch (Exception e) {
			return  new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
}
