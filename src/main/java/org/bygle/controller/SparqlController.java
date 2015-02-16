package org.bygle.controller;

import org.bygle.service.SparqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SparqlController {
	
	//private static final Logger logger = LoggerFactory.getLogger(SparqlController.class);
	@Autowired
	SparqlService sparqlService;
	
	@RequestMapping(value = "/sparql")
	public String sparql(Model model) {
		return "sparql";
	}
	
	@RequestMapping(value = "/sparql/query")
	public ResponseEntity<?> query(@RequestParam(required=false) String defaultGraphUri, @RequestParam String sparqlQuery,@RequestParam(required=false) Integer outpuFormat) {
		try {
			return sparqlService.query(defaultGraphUri,sparqlQuery, outpuFormat);
		} catch (Exception e) {
			return  new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
	}
	
}
