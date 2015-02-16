package org.bygle.controller;

import javax.servlet.http.HttpServletRequest;

import org.bygle.service.LDPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LDPController {
	
	private static final Logger logger = LoggerFactory.getLogger(LDPController.class);
	@Autowired
	LDPService ldpService;
	
	@RequestMapping(value = {"", "{path:(?!resources|error|sparql).*$}", "{path:(?!resources|error|sparql).*$}/**" }, method = RequestMethod.GET)
	public ResponseEntity<?> get(HttpServletRequest request) {
		logger.info("RequestMethod.GET");
		try {
			String about = ldpService.getAbout(request);
			if(request.getHeader("Accept").indexOf("text/html")!=-1){
				return ldpService.getResource(about,null);
			}else{
				return ldpService.get(about, request.getHeader("Accept"),request.getHeader("Prefer"),request.getHeader("Host"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return  new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = {"", "{path:(?!resources|error|sparql).*$}", "{path:(?!resources|error|sparql).*$}/**" }, method = RequestMethod.POST)
	public ResponseEntity<String> post(@RequestBody byte[] data,HttpServletRequest request) {
		try {
			logger.info("RequestMethod.POST");
			String about = ldpService.getAbout(request);
			return ldpService.post(data, request.getHeader("Content-Type"),about,request.getHeader("Slug"),request.getHeader("Host"),request.getHeader("Link"),request.getHeader("MD5"));
		} catch (Exception e) {
			e.printStackTrace();
			return  new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	 
	@RequestMapping(value = {"", "{path:(?!resources|error|sparql).*$}", "{path:(?!resources|error|sparql).*$}/**"  }, method = RequestMethod.DELETE)
	public ResponseEntity<String> delete(HttpServletRequest request) {
		try {
			logger.info("RequestMethod.DELETE");
			String about = ldpService.getAbout(request);
			return ldpService.delete(about,request.getHeader("Host"));
		} catch (Exception e) {
			e.printStackTrace();
			return  new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	 
	@RequestMapping(value = {"", "{path:(?!resources|error|sparql).*$}", "{path:(?!resources|error|sparql).*$}/**" }, method = RequestMethod.OPTIONS)
	public ResponseEntity<String> options(HttpServletRequest request) {
		try {
			logger.info("RequestMethod.OPTIONS");
			String about = ldpService.getAbout(request);
			return ldpService.options(about,request.getHeader("Host"));
		} catch (Exception e) {
			e.printStackTrace();
			return  new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = {"", "{path:(?!resources|error|sparql).*$}", "{path:(?!resources|error|sparql).*$}/**" }, method = RequestMethod.HEAD)
	public ResponseEntity<String> head(HttpServletRequest request) {
		try {
			logger.info("RequestMethod.HEAD");
			String about = ldpService.getAbout(request);
			return ldpService.head(about,request.getHeader("Host"));
		} catch (Exception e) {
			e.printStackTrace();
			return  new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = {"", "{path:(?!resources|error|sparql).*$}", "{path:(?!resources|error|sparql).*$}/**" }, method = RequestMethod.PUT)
	public ResponseEntity<String> put(@RequestBody String data,HttpServletRequest request) {
		try {
			logger.info("RequestMethod.PUT");
			String about = ldpService.getAbout(request);
			return ldpService.put(data.getBytes(), request.getHeader("Content-Type"),about,request.getHeader("If-Match"),request.getHeader("Host"),request.getHeader("Slug"),request.getHeader("Link"));
		} catch (Exception e) {
			e.printStackTrace();
			return  new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = {"", "{path:(?!resources|error|sparql).*$}", "{path:(?!resources|error|sparql).*$}/**" }, method = RequestMethod.PATCH)
	public ResponseEntity<String> patch(@RequestBody String data,HttpServletRequest request) {
		try {
			logger.info("RequestMethod.PATCH");
			String about = ldpService.getAbout(request);
			return ldpService.patch(data.getBytes(), request.getHeader("Content-Type"),about,request.getHeader("If-Match"),request.getHeader("Host"),request.getHeader("Slug"),request.getHeader("Link"));
		} catch (Exception e) {
			return  new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
