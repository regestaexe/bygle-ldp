package org.bygle.service.bean;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.apache.commons.configuration.ConfigurationException;
import org.bygle.bean.RdfClasses;
import org.bygle.utils.BygleSystemUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class Content {
    private byte[] content;
    private int resourceType;
    private Model model;
    private String inputFormat;
    private String outputFormat;
    private Resource ENTITY_TYPE;
    private String rdfAbout;
    private RdfClasses rdfClasses;
	public Content(String rdfAbout,int resourceType,Resource ENTITY_TYPE,Model model,String inputFormat,String outputFormat) throws UnsupportedEncodingException, ConfigurationException, URISyntaxException {
		super();
		this.resourceType = resourceType;
		this.ENTITY_TYPE = ENTITY_TYPE;
		this.model = model;
		this.inputFormat = inputFormat;
		this.outputFormat = outputFormat;
		this.rdfAbout = rdfAbout;
	}
	public Content(byte[] content, String rdfAbout,int resourceType) {
		this.resourceType = resourceType;
		this.content = content;
		this.rdfAbout = rdfAbout;
	}
	public byte[] getContent() {
		if(resourceType!=BygleSystemUtils.RESOURCE_TYPE_BINARY){
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			model.write(byteArrayOutputStream,BygleSystemUtils.getWriter(outputFormat));
			content = byteArrayOutputStream.toByteArray();
		}
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	public int getResourceType() {
		return resourceType;
	}
	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
	}
	
	public Resource getENTITY_TYPE() {
		return ENTITY_TYPE;
	}
	public void setENTITY_TYPE(Resource eNTITY_TYPE) {
		ENTITY_TYPE = eNTITY_TYPE;
	}
	public Model getModel() {
		return model;
	}
	public void setModel(Model model) {
		this.model = model;
	}
	public String getInputFormat() {
		return inputFormat;
	}
	public void setInputFormat(String inputFormat) {
		this.inputFormat = inputFormat;
	}
	public String getOutputFormat() {
		return outputFormat;
	}
	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}
	
	public void add(Resource s,Property p,RDFNode o){
		model.add(s, p, o);
	}
	
	public void remove(Resource s,Property p,RDFNode o){
		model.remove(s, p, o);
	}
	public String getRdfAbout() {
		return rdfAbout;
	}
	public void setRdfAbout(String rdfAbout) {
		this.rdfAbout = rdfAbout;
	}
	public RdfClasses getRdfClasses() {
		return rdfClasses;
	}
	public void setRdfClasses(RdfClasses rdfClasses) {
		this.rdfClasses = rdfClasses;
	}
	
}
