package org.bygle.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.bygle.bean.RdfClasses;
import org.bygle.bean.RecordTypes;
import org.bygle.bean.Records;
import org.bygle.bean.Slugs;
import org.bygle.db.services.BygleService;
import org.bygle.service.bean.Content;
import org.bygle.utils.BygleSystemUtils;
import org.bygle.utils.io.FileInfoReader;
import org.bygle.vocabulary.LDPVoc;
import org.bygle.xml.XMLBuilder;
import org.bygle.xml.XMLReader;
import org.bygle.xml.exception.XMLException;
import org.bygle.xslt.TrasformXslt;
import org.dom4j.DocumentException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

@Service(value = "ldpService")
public class LDPService {

	@javax.annotation.Resource(name = "rdfPresentationIncludeMap")
	HashMap<String, ArrayList<String>> rdfPresentationIncludeMap;

	@javax.annotation.Resource(name = "rdfPresentationOmitMap")
	HashMap<String, ArrayList<String>> rdfPresentationOmitMap;

	@javax.annotation.Resource(name = "defaultNamespaces")
	HashMap<String, String> defaultNamespaces;

	@javax.annotation.Resource(name = "readOnlyProperties")
	ArrayList<String> readOnlyProperties;

	@javax.annotation.Resource(name = "invalidProperties")
	ArrayList<String> invalidProperties;

	@Autowired
	RelationsService relationsService;
	@Autowired
	BygleService bygleService;

	private String defaultDomain;
	private String defaultEncoding;

	public String getAbout(HttpServletRequest request) {
		String result = "";
		try {
			result = URLDecoder.decode(StringUtils.substringAfter(request.getRequestURI(), request.getContextPath()), defaultEncoding);
		} catch (UnsupportedEncodingException e) {
			result = StringUtils.substringAfter(request.getRequestURI(), request.getContextPath());
		}
		return result.isEmpty() ? "/" : result.replaceAll("^/", "");
	}

	public ResponseEntity<?> get(String about, String accept, String prefer, String headerHost) throws ConfigurationException, XMLException, UnsupportedEncodingException, URISyntaxException {
		String rdfAbout = about;// + (!about.endsWith("/") ? "/" : "");
		String host = defaultDomain;
		if (headerHost != null && !headerHost.trim().isEmpty())
			host = headerHost;
		host = host.replaceAll("/$", "") + "/";
		host = host.startsWith("http://") ? host : "http://" + host;
		List<?> recordsList = bygleService.getList(getCriteria(Records.class, Restrictions.or(Restrictions.eq("rdfAbout", rdfAbout), Restrictions.eq("rdfAbout", about)), Restrictions.eq("host", host)));
		if (recordsList.size() > 0) {
			Records records = (Records) recordsList.get(0);
			Content content = getContent(records.getRdf(), about, records.getContentType(), accept, records.getHost());
			if (content.getResourceType() == BygleSystemUtils.RESOURCE_TYPE_BINARY) {
				HttpHeaders headers = new HttpHeaders();
				headers.add("Content-Type", records.getContentType() + "; charset=" + defaultEncoding);
				headers.add("Content-Length", Integer.toString(content.getContent().length));
				headers.add("ETAG", "\"" + records.getEtag() + "\"");
				headers.add("Host", records.getHost());
				headers.add("Link", "<" + LDPVoc.Resource.stringValue() + ">; rel=\"type\", <" + LDPVoc.NonRDFSource.stringValue() + ">; rel=\"type\"");
				String describedby = about.endsWith("/") ? about + "meta" : about + "/meta";
				headers.add("Link", "<" + joinUrl(records.getHost(), describedby) + ">; rel=\"describedby\" anchor=\"" + joinUrl(records.getHost(), about) + "\"");
				headers.add("Allow", getAllowPostList(BygleSystemUtils.allowedPostNonRDFMethods));
				headers.add("Accept-Post", BygleSystemUtils.acceptPostFile);
				if (records.getCreationDate() != null)
					headers.add("Creation-Date", records.getCreationDate().toString());
				if (records.getModifyDate() != null)
					headers.add("Modify-Date", records.getModifyDate().toString());
				if (records.getCreationEtag() != null)
					headers.add("Creation-Etag", records.getCreationEtag());
				return new ResponseEntity<byte[]>(content.getContent(), headers, HttpStatus.OK);
			} else {
				if (prefer != null && prefer.indexOf("representation") != -1) {
					Content newContent = getContent(records.getRdf(), about, records.getContentType(), records.getContentType(), records.getHost());
					if (prefer.indexOf("include") != -1) {
						byte[] cleanedContent = cleanRDF(newContent.getContent(), StringUtils.substringBetween(prefer, "include=\"", "\"").trim(), rdfPresentationIncludeMap);
						content = getContent(cleanedContent, about, records.getContentType(), accept, records.getHost(), null, null);
					} else if (prefer.indexOf("omit") != -1) {
						byte[] cleanedContent = cleanRDF(newContent.getContent(), StringUtils.substringBetween(prefer, "omit=\"", "\"").trim(), rdfPresentationOmitMap);
						content = getContent(cleanedContent, about, records.getContentType(), accept, records.getHost(), null, null);
					}
				}
				String result = new String(content.getContent());
				HttpHeaders headers = new HttpHeaders();
				headers.add("Content-Type", accept + "; charset=" + defaultEncoding);
				String relType = "";
				Iterator<Statement> iterator = content.getModel().getResource(joinUrl(records.getHost(), about)).listProperties(RDF.type);
				while (iterator.hasNext()) {
					relType += "<" + iterator.next().getResource().toString() + ">; rel=\"type\", ";
				}
				relType = StringUtils.substringBeforeLast(relType, ", ");
				headers.add("Link", relType);
				if (content.getModel().contains(content.getModel().getResource(defaultDomain + about), content.getModel().createProperty(LDPVoc.contains.stringValue()))) {
					headers.add("Allow", getAllowPostList(BygleSystemUtils.allowedPostContainerMethods));
				} else {
					headers.add("Allow", getAllowPostList(BygleSystemUtils.allowedPostRDFMethods));
				}
				headers.add("Accept-Post", BygleSystemUtils.acceptPostData + ", " + BygleSystemUtils.acceptPostFile);
				headers.add("Accept-Patch", BygleSystemUtils.acceptPatchData);
				headers.add("Content-Length", Integer.toString(result.getBytes().length));
				headers.add("ETAG", "\"" + records.getEtag() + "\"");
				headers.add("Host", records.getHost());
				headers.add("Location", joinUrl(records.getHost(), about));
				if (records.getCreationDate() != null)
					headers.add("Creation-Date", records.getCreationDate().toString());
				if (records.getModifyDate() != null)
					headers.add("Modify-Date", records.getModifyDate().toString());
				if (records.getCreationEtag() != null)
					headers.add("Creation-Etag", records.getCreationEtag());
				return new ResponseEntity<String>(result, headers, HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity<?> getResource(String about, String headerHost) throws TransformerConfigurationException, Exception {
		String rdfAbout = about + (!about.endsWith("/") ? "/" : "");
		String host = defaultDomain;
		if (headerHost != null && !headerHost.trim().isEmpty())
			host = headerHost;
		List<?> recordsList = bygleService.getList(getCriteria(Records.class, Restrictions.or(Restrictions.eq("rdfAbout", rdfAbout), Restrictions.eq("rdfAbout", about)), Restrictions.eq("host", host)));
		if (recordsList.size() > 0) {
			Records records = (Records) recordsList.get(0);
			Content content = getContent(records.getRdf(), about, records.getContentType(), records.getContentType(), records.getHost());
			if (content.getResourceType() == BygleSystemUtils.RESOURCE_TYPE_BINARY) {
				HttpHeaders headers = new HttpHeaders();
				headers.add("Content-Type", records.getContentType() + ";");
				headers.add("Content-Length", Integer.toString(content.getContent().length));
				headers.add("Host", records.getHost());
				headers.add("ETAG", "\"" + records.getEtag() + "\"");
				headers.add("Location", joinUrl(records.getHost(), about));
				if (records.getCreationDate() != null)
					headers.add("Creation-Date", records.getCreationDate().toString());
				if (records.getModifyDate() != null)
					headers.add("Modify-Date", records.getModifyDate().toString());
				if (records.getCreationEtag() != null)
					headers.add("Creation-Etag", records.getCreationEtag());
				return new ResponseEntity<byte[]>(content.getContent(), headers, HttpStatus.OK);
			} else {
				HttpHeaders headers = new HttpHeaders();
				headers.add("Content-Type", BygleSystemUtils.INPUTFORMAT_APPLICATION_RDF_XML + "; charset=" + defaultEncoding);
				headers.add("Content-Disposition", "attachment; filename=" + about + ".rdf");
				headers.add("Host", records.getHost());
				headers.add("Location", joinUrl(records.getHost(), about));
				headers.add("ETAG", "\"" + records.getEtag() + "\"");
				if (records.getCreationDate() != null)
					headers.add("Creation-Date", records.getCreationDate().toString());
				if (records.getModifyDate() != null)
					headers.add("Modify-Date", records.getModifyDate().toString());
				if (records.getCreationEtag() != null)
					headers.add("Creation-Etag", records.getCreationEtag());
				return new ResponseEntity<byte[]>(records.getRdf(), headers, HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<String>(HttpStatus.GONE);
		}
	}

	private boolean isAlreadyUsedSlug(String slug) {
		List<?> recordsList = bygleService.getList(getCriteria(Slugs.class, Restrictions.eq("slug", slug)));
		if (recordsList.size() > 0) {
			return true;
		} else {
			Slugs newSlug = new Slugs(slug);
			bygleService.add(newSlug);
			return false;
		}
	}

	public ResponseEntity<String> post(byte[] content, String contentType, String about, String slug, String headerHost, String link, String md5) throws Exception {
		String rdfAbout = about.replaceAll("^/", "");

		rdfAbout = slug != null && !slug.isEmpty() ? joinUrl(rdfAbout, slug) : rdfAbout;
		String host = defaultDomain;
		if (headerHost != null && !headerHost.trim().isEmpty())
			host = headerHost;

		host = host.replaceAll("/$", "") + "/";
		host = host.startsWith("http://") ? host : "http://" + host;

		Content contentToInsert = getContent(content, rdfAbout, contentType, BygleSystemUtils.INPUTFORMAT_APPLICATION_RDF_XML, host, link, slug);
		try {
			List<?> recordsList = bygleService.getList(getCriteria(Records.class, Restrictions.eq("rdfAbout", contentToInsert.getRdfAbout()), Restrictions.eq("host", host)));
			if (recordsList.size() == 0) {
				RecordTypes recordTypes = (RecordTypes) bygleService.getObject(RecordTypes.class, contentToInsert.getResourceType());
				Records container = getContainer(contentToInsert.getRdfAbout());
				if (contentToInsert.getResourceType() == BygleSystemUtils.RESOURCE_TYPE_BINARY) {
					ByteArrayInputStream fis = new ByteArrayInputStream(content);
					String md5ETag = DigestUtils.md5Hex(fis);
					fis.close();
					Records records = new Records(recordTypes, null, content, new Date(), null, contentToInsert.getRdfAbout(), contentType, md5ETag, md5, host);
					bygleService.add(records);
					if (container != null) {
						Content containerContent = getContent(container.getRdf(), container.getRdfAbout(), container.getContentType(), BygleSystemUtils.INPUTFORMAT_APPLICATION_RDF_XML, host);
						if (containerContent.getResourceType() == BygleSystemUtils.RESOURCE_TYPE_RDF_CONTAINER) {
							addContainerTriples(containerContent, joinUrl(defaultDomain, container.getRdfAbout()), joinUrl(defaultDomain, contentToInsert.getRdfAbout()));
						} else if (containerContent.getResourceType() == BygleSystemUtils.RESOURCE_TYPE_RDF_BASIC_CONTAINER) {
							addContainerTriples(containerContent, joinUrl(defaultDomain, container.getRdfAbout()), joinUrl(defaultDomain, contentToInsert.getRdfAbout()));
						} else if (containerContent.getResourceType() == BygleSystemUtils.RESOURCE_TYPE_RDF_DIRECT_CONTAINER) {
							addDirectContainerTriples(containerContent, joinUrl(defaultDomain, container.getRdfAbout()), joinUrl(defaultDomain, contentToInsert.getRdfAbout()), contentToInsert);
						} else if (containerContent.getResourceType() == BygleSystemUtils.RESOURCE_TYPE_RDF_INDIRECT_CONTAINER) {
							addIndirectContainerTriples(containerContent, joinUrl(defaultDomain, container.getRdfAbout()), joinUrl(defaultDomain, contentToInsert.getRdfAbout()));
						} else if (containerContent.getResourceType() != BygleSystemUtils.RESOURCE_TYPE_BINARY) {
							makeResourceAsContainer(containerContent, joinUrl(defaultDomain, container.getRdfAbout()), joinUrl(defaultDomain, contentToInsert.getRdfAbout()));
							container.setRecordTypes((RecordTypes) bygleService.getObject(RecordTypes.class, BygleSystemUtils.RESOURCE_TYPE_RDF_CONTAINER));
						}
						String rdfContainer = new String(containerContent.getContent());
						rdfContainer = TrasformXslt.xslt(rdfContainer, BygleSystemUtils.getXSLTController());
						String md5ETagContainer = DigestUtils.md5Hex(rdfContainer);
						container.setEtag(md5ETagContainer);
						container.setRdf(rdfContainer.getBytes());
						container.setModifyDate(new Date());
						bygleService.update(container);
						relationsService.addRelations(container);
					}
					String describedby = createMetaRdf(content, joinUrl(host, contentToInsert.getRdfAbout()), contentToInsert.getRdfAbout(), host, contentType, new Date().toString(), host);
					HttpHeaders headers = new HttpHeaders();
					headers.add("Location", joinUrl(host, contentToInsert.getRdfAbout()));
					headers.add("Accept-Post", BygleSystemUtils.acceptPostFile);
					headers.add("Link", "<" + LDPVoc.Resource.stringValue() + ">; rel=\"type\", <" + LDPVoc.NonRDFSource.stringValue() + ">; rel=\"type\"");
					headers.add("Link", "<" + joinUrl(defaultDomain, describedby) + ">; rel=\"describedby\" anchor=\"" + joinUrl(host, contentToInsert.getRdfAbout()) + "\"");
					headers.add("ETAG", "\"" + records.getEtag() + "\"");
					headers.add("Content-Length", "0");
					return new ResponseEntity<String>("", headers, HttpStatus.CREATED);
				} else {
					String rdf = new String(contentToInsert.getContent());
					rdf = TrasformXslt.xslt(rdf, BygleSystemUtils.getXSLTController());
					String md5ETag = DigestUtils.md5Hex(rdf);
					Records records = new Records(recordTypes, contentToInsert.getRdfClasses(), rdf.getBytes(), new Date(), null, contentToInsert.getRdfAbout(), BygleSystemUtils.INPUTFORMAT_APPLICATION_RDF_XML, md5ETag, md5, host);
					bygleService.add(records);
					relationsService.addRelations(records);
					if (container != null && container.getRecordTypes().getIdRecordType() != BygleSystemUtils.RESOURCE_TYPE_BINARY) {
						Content containerContent = getContent(container.getRdf(), container.getRdfAbout(), container.getContentType(), BygleSystemUtils.INPUTFORMAT_APPLICATION_RDF_XML, container.getHost());
						if (containerContent.getResourceType() == BygleSystemUtils.RESOURCE_TYPE_RDF_CONTAINER) {
							addContainerTriples(containerContent, joinUrl(defaultDomain, container.getRdfAbout()), joinUrl(defaultDomain, contentToInsert.getRdfAbout()));
						} else if (containerContent.getResourceType() == BygleSystemUtils.RESOURCE_TYPE_RDF_BASIC_CONTAINER) {
							addContainerTriples(containerContent, joinUrl(defaultDomain, container.getRdfAbout()), joinUrl(defaultDomain, contentToInsert.getRdfAbout()));
						} else if (containerContent.getResourceType() == BygleSystemUtils.RESOURCE_TYPE_RDF_DIRECT_CONTAINER) {
							addDirectContainerTriples(containerContent, joinUrl(defaultDomain, container.getRdfAbout()), joinUrl(defaultDomain, contentToInsert.getRdfAbout()), contentToInsert);
						} else if (containerContent.getResourceType() == BygleSystemUtils.RESOURCE_TYPE_RDF_INDIRECT_CONTAINER) {
							addIndirectContainerTriples(containerContent, joinUrl(defaultDomain, container.getRdfAbout()), joinUrl(defaultDomain, contentToInsert.getRdfAbout()));
						} else if (containerContent.getResourceType() != BygleSystemUtils.RESOURCE_TYPE_BINARY) {
							makeResourceAsContainer(containerContent, joinUrl(defaultDomain, container.getRdfAbout()), joinUrl(defaultDomain, contentToInsert.getRdfAbout()));
							container.setRecordTypes((RecordTypes) bygleService.getObject(RecordTypes.class, BygleSystemUtils.RESOURCE_TYPE_RDF_CONTAINER));
						}
						String rdfContainer = new String(containerContent.getContent());
						rdfContainer = TrasformXslt.xslt(rdfContainer, BygleSystemUtils.getXSLTController());
						String md5ETagContainer = DigestUtils.md5Hex(rdfContainer);
						container.setEtag(md5ETagContainer);
						container.setRdf(rdfContainer.getBytes());
						container.setModifyDate(new Date());
						bygleService.update(container);
						relationsService.addRelations(container);
					}
					HttpHeaders headers = new HttpHeaders();
					headers.add("Location", joinUrl(host, contentToInsert.getRdfAbout()));
					headers.add("Accept-Post", BygleSystemUtils.acceptPostData + ", " + BygleSystemUtils.acceptPostFile);
					headers.add("Accept-Patch", BygleSystemUtils.acceptPatchData);
					headers.add("ETAG", "\"" + records.getEtag() + "\"");
					String relType = "";
					Iterator<Statement> iterator = contentToInsert.getModel().getResource(joinUrl(records.getHost(), contentToInsert.getRdfAbout())).listProperties(RDF.type);
					while (iterator.hasNext()) {
						relType += "<" + iterator.next().getResource().toString() + ">; rel=\"type\", ";
					}
					relType = StringUtils.substringBeforeLast(relType, ", ");
					headers.add("Link", relType);
					headers.add("Content-Length", "0");
					return new ResponseEntity<String>("", headers, HttpStatus.CREATED);
				}
			} else {
				return new ResponseEntity<String>("The resource " + contentToInsert.getRdfAbout() + " already exists.", HttpStatus.CONFLICT);
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private String createMetaRdf(byte[] content, String hostAbout, String about, String host, String contentType, String created, String headerHost) {
		try {
			Model model = ModelFactory.createDefaultModel();
			String metaResource = hostAbout.endsWith("/") ? hostAbout + "meta" : hostAbout + "/meta";
			String rdfAbout = about.endsWith("/") ? about + "meta" : about + "/meta";
			Resource resource = model.createResource(metaResource);
			resource.addProperty(RDF.type, model.createResource(LDPVoc.RDFSource.stringValue()));
			resource.addProperty(RDF.type, model.createResource(LDPVoc.Resource.stringValue()));
			resource.addProperty(DC.title, about);
			resource.addProperty(DCTerms.format, contentType);
			resource.addProperty(DCTerms.extent, FileInfoReader.getFileSizeString(content.length));
			resource.addProperty(DCTerms.created, created);
			resource.addProperty(DCTerms.references, model.createResource(hostAbout));
			try {
				resource.addProperty(DCTerms.description, FileInfoReader.extractStringMetaData(new ByteArrayInputStream(content)).toString());
			} catch (Exception e) {
			}
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			model.write(byteArrayOutputStream, BygleSystemUtils.getWriter("text/turtle"));
			post(byteArrayOutputStream.toByteArray(), "text/turtle", rdfAbout, null, headerHost, null, null);
			return rdfAbout;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void updateMetaRdf(byte[] content, String about, String contentType, String headerHost) {
		try {
			String rdfAbout = about.endsWith("/") ? about + "meta" : about + "/meta";
			String host = headerHost.replaceAll("/$", "") + "/";
			host = host.startsWith("http://") ? host : "http://" + host;
			List<?> recordsList = bygleService.getList(getCriteria(Records.class, Restrictions.eq("rdfAbout", rdfAbout), Restrictions.eq("host", host)));
			if (recordsList.size() > 0) {
				Records records = (Records) recordsList.get(0);
				XMLBuilder xmlBuilder = new XMLBuilder(records.getRdf());
				xmlBuilder.insertValueAt("/rdf:RDF/*/dcterms:modified/text()", new Date().toString());
				xmlBuilder.insertValueAt("/rdf:RDF/*/dcterms:extent/text()", FileInfoReader.getFileSizeString(content.length));
				xmlBuilder.insertValueAt("/rdf:RDF/*/dcterms:format/text()", contentType);
				try {
					xmlBuilder.insertNode("/rdf:RDF/*/dcterms:description/text()", FileInfoReader.extractStringMetaData(new ByteArrayInputStream(content)).toString(), true);
				} catch (Exception e) {
				}
				String rdf = xmlBuilder.getXML(defaultEncoding);
				rdf = TrasformXslt.xslt(rdf, BygleSystemUtils.getXSLTController());
				String md5ETag = DigestUtils.md5Hex(rdf);
				records.setModifyDate(new Date());
				records.setEtag(md5ETag);
				records.setRdf(rdf.getBytes());
				bygleService.update(records);
			}

		} catch (Exception e) {
		}
	}

	private void deleteMetaRdf(String about, String headerHost) {
		try {
			String rdfAbout = about.endsWith("/") ? about + "meta" : about + "/meta";
			String host = headerHost.replaceAll("/$", "") + "/";
			host = host.startsWith("http://") ? host : "http://" + host;
			List<?> recordsList = bygleService.getList(getCriteria(Records.class, Restrictions.eq("rdfAbout", rdfAbout), Restrictions.eq("host", host)));
			if (recordsList.size() > 0) {
				Records records = (Records) recordsList.get(0);
				relationsService.cleanRelateds(records);
				bygleService.remove(records);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ResponseEntity<String> put(byte[] content, String contentType, String about, String eTag, String headerHost, String slug, String link) throws Exception {
		try {
			String rdfAbout = about;// + (!about.endsWith("/") ? "/" : "");
			String host = defaultDomain;
			if (headerHost != null && !headerHost.trim().isEmpty())
				host = headerHost;
			host = host.replaceAll("/$", "") + "/";
			host = host.startsWith("http://") ? host : "http://" + host;
			//Content contentToUpdate = getContent(content, about, contentType, BygleSystemUtils.INPUTFORMAT_APPLICATION_RDF_XML, host);
			Content contentToUpdate = getContent(content, rdfAbout, contentType, BygleSystemUtils.INPUTFORMAT_APPLICATION_RDF_XML, host, link, slug);
			try {
				String invalidRelation = relationsService.testLDPRelations(contentToUpdate.getContent());
				if (invalidRelation != null) {
					HttpHeaders headers = new HttpHeaders();
					headers.add("Link", "<" + invalidRelation + ">; rel=\"" + LDPVoc.constrainedBy.stringValue() + "\"");
					return new ResponseEntity<String>(HttpStatus.CONFLICT);
				}
			} catch (Exception e) {

			}
			if (eTag != null) {
				List<?> recordsList = bygleService.getList(getCriteria(Records.class, Restrictions.or(Restrictions.eq("rdfAbout", rdfAbout), Restrictions.eq("rdfAbout", about)), Restrictions.eq("etag", eTag.replaceAll("\"", "")), Restrictions.eq("host", host)));
				if (recordsList.size() > 0) {
					Records records = (Records) recordsList.get(0);
					if (contentToUpdate.getResourceType() == BygleSystemUtils.RESOURCE_TYPE_BINARY) {
						ByteArrayInputStream fis = new ByteArrayInputStream(content);
						String md5ETag = DigestUtils.md5Hex(fis);
						fis.close();
						records.setModifyDate(new Date());
						records.setEtag(md5ETag);
						records.setRdf(content);
						bygleService.update(records);
						updateMetaRdf(content, records.getRdfAbout(), contentType, headerHost);
						HttpHeaders headers = new HttpHeaders();
						headers.add("Link", "<" + LDPVoc.Resource.stringValue() + ">; rel=\"type\", <" + LDPVoc.NonRDFSource.stringValue() + ">; rel=\"type\"");
						headers.add("ETAG", "\"" + records.getEtag() + "\"");
						headers.add("Content-Length", "0");
						return new ResponseEntity<String>("", headers, HttpStatus.NO_CONTENT);
					} else {
						byte[] oldRdf = records.getRdf();
						String readOnlyPropertiesChanged = readOnlyPropertiesChanged(oldRdf, contentToUpdate.getContent(), joinUrl(records.getHost(), records.getRdfAbout()));
						// String invalidProperty =
						// findInvalidProperty(contentToUpdate.getContent(),joinUrl(records.getHost(),
						// records.getRdfAbout()));
						if (readOnlyPropertiesChanged != null) {
							HttpHeaders headers = new HttpHeaders();
							headers.add("Link", "<" + readOnlyPropertiesChanged + ">; rel=\"" + LDPVoc.constrainedBy.stringValue() + "\"");
							return new ResponseEntity<String>("you can't modify the read only property " + readOnlyPropertiesChanged, headers, HttpStatus.CONFLICT);
						}/*
						 * else if(invalidProperty!=null){ HttpHeaders headers =
						 * new HttpHeaders(); headers.add("Link",
						 * "<"+invalidProperty
						 * +">; rel=\""+LDPVoc.constrainedBy.stringValue
						 * ()+"\""); return new ResponseEntity<String>(
						 * "you can't modify the read only property "
						 * +readOnlyPropertiesChanged
						 * ,headers,HttpStatus.CONFLICT); }
						 */else {
							String rdf = new String(contentToUpdate.getContent());
							rdf = TrasformXslt.xslt(rdf, BygleSystemUtils.getXSLTController());
							String md5ETag = DigestUtils.md5Hex(rdf);
							records.setModifyDate(new Date());
							records.setEtag(md5ETag);
							records.setRdf(rdf.getBytes());
							bygleService.update(records);
							relationsService.updateRelations(records, oldRdf);
							HttpHeaders headers = new HttpHeaders();
							String relType = "";
							Iterator<Statement> iterator = contentToUpdate.getModel().getResource(joinUrl(records.getHost(), contentToUpdate.getRdfAbout())).listProperties(RDF.type);
							while (iterator.hasNext()) {
								relType += "<" + iterator.next().getResource().toString() + ">; rel=\"type\", ";
							}
							relType = StringUtils.substringBeforeLast(relType, ", ");
							headers.add("Link", relType);
							headers.add("Content-Length", "0");
							headers.add("ETAG", "\"" + records.getEtag() + "\"");
							return new ResponseEntity<String>("", headers, HttpStatus.NO_CONTENT);
						}
					}
				} else {
					return new ResponseEntity<String>("The resource " + about + " and the ETag " + eTag + " doesn't match.", HttpStatus.PRECONDITION_FAILED);
				}
			} else {
				if (isContainerOrNotExist(rdfAbout, about)) {
					if (!isAlreadyUsedSlug(about)) {
						return post(content, contentType, about, slug, headerHost, link, null);
					} else {
						return new ResponseEntity<String>("You can't reuse uri " + about + " to put a new resource", HttpStatus.PRECONDITION_REQUIRED);
					}
				} else {
					return new ResponseEntity<String>(HttpStatus.PRECONDITION_REQUIRED);
				}

			}

		} catch (FileNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	// private String findInvalidProperty(byte[] content,String uri) throws
	// DocumentException {
	// if(readOnlyProperties==null || readOnlyProperties.size()==0)
	// return null;
	// Model newModel=ModelFactory.createDefaultModel();
	// newModel.read(new ByteArrayInputStream(content),uri);
	// Resource newResource = newModel.getResource(uri);
	// for (int i = 0; i < invalidProperties.size(); i++) {
	// String invalidProperty = invalidProperties.get(i);
	// if(newResource.hasProperty(newModel.createProperty(invalidProperty))){
	// return invalidProperty;
	// }
	// }
	// return null;
	// }

	private String readOnlyPropertiesChanged(byte[] oldRdf, byte[] content, String uri) throws DocumentException {
		if (readOnlyProperties == null || readOnlyProperties.size() == 0)
			return null;
		Model oldModel = ModelFactory.createDefaultModel();
		oldModel.read(new ByteArrayInputStream(oldRdf), uri);
		Model newModel = ModelFactory.createDefaultModel();
		newModel.read(new ByteArrayInputStream(content), uri);
		Resource newResource = newModel.getResource(uri);
		Resource oldResource = oldModel.getResource(uri);
		for (int i = 0; i < readOnlyProperties.size(); i++) {
			String readOnlyPropertie = readOnlyProperties.get(i);
			if (newResource.hasProperty(newModel.createProperty(readOnlyPropertie)) && oldResource.hasProperty(oldModel.createProperty(readOnlyPropertie))) {
				String newValue = newResource.getProperty(newModel.createProperty(readOnlyPropertie)).getObject().toString();
				String oldValue = oldResource.getProperty(oldModel.createProperty(readOnlyPropertie)).getObject().toString();
				if (!newValue.equals(oldValue))
					return readOnlyPropertie;
			} else if (newResource.hasProperty(newModel.createProperty(readOnlyPropertie)) && !oldResource.hasProperty(oldModel.createProperty(readOnlyPropertie))) {
				return readOnlyPropertie;
			} else if (!newResource.hasProperty(newModel.createProperty(readOnlyPropertie)) && oldResource.hasProperty(oldModel.createProperty(readOnlyPropertie))) {
				return readOnlyPropertie;
			}
		}
		return null;
	}

	public ResponseEntity<String> patch(byte[] content, String contentType, String about, String eTag, String headerHost, String slug, String link) throws Exception {
		try {
			String rdfAbout = about;// + (!about.endsWith("/") ? "/" : "");
			String host = defaultDomain;
			if (headerHost != null && !headerHost.trim().isEmpty())
				host = headerHost;
			host = host.replaceAll("/$", "") + "/";
			host = host.startsWith("http://") ? host : "http://" + host;
			Content contentToUpdate = getContent(content, about, contentType, BygleSystemUtils.INPUTFORMAT_APPLICATION_RDF_XML, host);
			if (eTag != null) {
				List<?> recordsList = bygleService.getList(getCriteria(Records.class, Restrictions.or(Restrictions.eq("rdfAbout", rdfAbout), Restrictions.eq("rdfAbout", about)), Restrictions.eq("etag", eTag.replaceAll("\"", "")), Restrictions.eq("host", host)));
				if (recordsList.size() > 0) {
					Records records = (Records) recordsList.get(0);
					String rdf = new String();
					byte[] mergedRdf = applyPatch(records.getRdf(), rdf);
					rdf = TrasformXslt.xslt(rdf, BygleSystemUtils.getXSLTController());
					String md5ETag = DigestUtils.md5Hex(rdf);
					records.setModifyDate(new Date());
					records.setEtag(md5ETag);
					records.setRdf(rdf.getBytes());
					bygleService.update(records);
					relationsService.updateRelations(records, mergedRdf);
					HttpHeaders headers = new HttpHeaders();
					String relType = "";
					Iterator<Statement> iterator = contentToUpdate.getModel().getResource(joinUrl(records.getHost(), contentToUpdate.getRdfAbout())).listProperties(RDF.type);
					while (iterator.hasNext()) {
						relType += "<" + iterator.next().getResource().toString() + ">; rel=\"type\", ";
					}
					relType = StringUtils.substringBeforeLast(relType, ", ");
					headers.add("Link", relType);
					headers.add("Content-Length", "0");
					headers.add("ETAG", "\"" + records.getEtag() + "\"");
					return new ResponseEntity<String>("", headers, HttpStatus.NO_CONTENT);
				} else {
					return new ResponseEntity<String>("The resource " + about + " and the ETag " + eTag + " doesn't match.", HttpStatus.PRECONDITION_FAILED);
				}
			} else {
				return new ResponseEntity<String>(HttpStatus.PRECONDITION_REQUIRED);
			}

		} catch (FileNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	public XMLBuilder applyByglePatch(XMLBuilder xmlBuilder, String xpath, XMLReader xmlReader, String action) throws XMLException {
		int nodeCount = xmlReader.getNodeCount(xpath);
		for (int i = 0; i < nodeCount; i++) {
			String path = xmlReader.getNodeValue(xpath + "[" + (i + 1) + "]/bygle:path/text()");
			String value = xmlReader.getNodeValue(xpath + "[" + (i + 1) + "]/bygle:value/text()");
			if (action.equalsIgnoreCase("insert")) {
				xmlBuilder.insertNode(path, value);
			} else if (action.equalsIgnoreCase("update")) {
				xmlBuilder.insertValueAt(path, value);
			} else if (action.equalsIgnoreCase("delete")) {
				xmlBuilder.deleteNode(path);
			}
		}
		return xmlBuilder;
	}

	/*
	 * Simple bygle patch example in RDF/XML
	 * 
	 * <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	 * xmlns:bygle="http://www.bygle.org/bygle/"> <bygle:Patch> <bygle:update
	 * rdf
	 * :parseType="Literal"><bygle:path><![CDATA[/rdf:RDF/ldp:BasicContainer[1
	 * ]/dcterms:title/text()]]></bygle:path><bygle:value>A very simple
	 * container 2</bygle:value></bygle:update> <bygle:insert
	 * rdf:parseType="Literal"
	 * ><bygle:path><![CDATA[/rdf:RDF/ldp:BasicContainer[1
	 * ]/dc:title/text()]]></bygle:path><bygle:value>Nuovo
	 * titolo</bygle:value></bygle:insert> <bygle:delete
	 * rdf:parseType="Literal">
	 * <bygle:path><![CDATA[/rdf:RDF/ldp:BasicContainer[1
	 * ]/ldp:contains[@rdf:resource
	 * ='http://www.bygle.com/alice/member1']]]></bygle:path></bygle:delete>
	 * <bygle:insert
	 * rdf:parseType="Literal"><bygle:path><![CDATA[/rdf:RDF/ldp:BasicContainer
	 * [1
	 * ]/ldp:contains/@rdf:resource]]></bygle:path><bygle:value>http://www.bygle
	 * .com/alice/member4</bygle:value></bygle:insert> </bygle:Patch> </rdf:RDF>
	 * 
	 * 
	 * The same bygle patch example in TURTLE
	 * 
	 * @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
	 * 
	 * @prefix bygle: <http://www.bygle.org/bygle/> .
	 * 
	 * @prefix owl: <http://www.w3.org/2002/07/owl#> .
	 * 
	 * @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
	 * 
	 * @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
	 * 
	 * [ a bygle:Patch ; bygle:delete
	 * "<bygle:path xmlns:bygle=\"http://www.bygle.org/bygle/\">/rdf:RDF/ldp:BasicContainer[1]/ldp:contains[@rdf:resource='http://www.bygle.com/alice/member1']</bygle:path>"
	 * ^^rdf:XMLLiteral ; bygle:insert
	 * "<bygle:path xmlns:bygle=\"http://www.bygle.org/bygle/\">/rdf:RDF/ldp:BasicContainer[1]/ldp:contains/@rdf:resource</bygle:path><bygle:value xmlns:bygle=\"http://www.bygle.org/bygle/\">http://www.bygle.com/alice/member4</bygle:value>"
	 * ^^rdf:XMLLiteral ,
	 * "<bygle:path xmlns:bygle=\"http://www.bygle.org/bygle/\">/rdf:RDF/ldp:BasicContainer[1]/dc:title/text()</bygle:path><bygle:value xmlns:bygle=\"http://www.bygle.org/bygle/\">Nuovo titolo</bygle:value>"
	 * ^^rdf:XMLLiteral ; bygle:update
	 * "<bygle:path xmlns:bygle=\"http://www.bygle.org/bygle/\">/rdf:RDF/ldp:BasicContainer[1]/dcterms:title/text()</bygle:path><bygle:value xmlns:bygle=\"http://www.bygle.org/bygle/\">A very simple container 2</bygle:value>"
	 * ^^rdf:XMLLiteral ] .
	 */
	public byte[] applyPatch(byte[] oldResource, String byglePatch) {
		try {
			XMLBuilder xmlBuilder = new XMLBuilder(oldResource);
			XMLReader newResourceXmlReader = new XMLReader(byglePatch);
			applyByglePatch(xmlBuilder, "/rdf:RDF/bygle:Patch/bygle:insert", newResourceXmlReader, "insert");
			applyByglePatch(xmlBuilder, "/rdf:RDF/bygle:Patch/bygle:update", newResourceXmlReader, "update");
			applyByglePatch(xmlBuilder, "/rdf:RDF/bygle:Patch/bygle:delete", newResourceXmlReader, "delete");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public ResponseEntity<String> delete(String about, String headerHost) throws Exception {
		String rdfAbout = about; // + (!about.endsWith("/") ? "/" : "");
		String host = defaultDomain;
		if (headerHost != null && !headerHost.trim().isEmpty())
			host = headerHost;
		host = host.replaceAll("/$", "") + "/";
		host = host.startsWith("http://") ? host : "http://" + host;
		List<?> recordsList = bygleService.getList(getCriteria(Records.class, Restrictions.or(Restrictions.eq("rdfAbout", rdfAbout), Restrictions.eq("rdfAbout", about)), Restrictions.eq("host", host)));
		if (recordsList.size() > 0) {
			Records records = (Records) recordsList.get(0);
			if (records.getRecordTypes().getIdRecordType() == BygleSystemUtils.RESOURCE_TYPE_BINARY) {
				deleteMetaRdf(records.getRdfAbout(), headerHost);
			}
			relationsService.cleanRelateds(records);
			bygleService.remove(records);
			HttpHeaders headers = new HttpHeaders();
			headers.add("Link", "<" + LDPVoc.Resource.stringValue() + ">; rel=\"type\"");
			headers.add("Content-Length", "0");
			return new ResponseEntity<String>("", headers, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<String>(HttpStatus.GONE);
		}
	}

	public ResponseEntity<String> options(String about, String headerHost) throws Exception {
		String rdfAbout = about;// + (!about.endsWith("/") ? "/" : "");
		String host = defaultDomain;
		if (headerHost != null && !headerHost.trim().isEmpty())
			host = headerHost;
		host = host.replaceAll("/$", "") + "/";
		host = host.startsWith("http://") ? host : "http://" + host;
		List<?> recordsList = bygleService.getList(getCriteria(Records.class, Restrictions.or(Restrictions.eq("rdfAbout", rdfAbout), Restrictions.eq("rdfAbout", about)), Restrictions.eq("host", host)));
		if (recordsList.size() > 0) {
			Records records = (Records) recordsList.get(0);
			Content content = getContent(records.getRdf(), about, records.getContentType(), records.getContentType(), host);
			if (content.getResourceType() == BygleSystemUtils.RESOURCE_TYPE_BINARY) {
				HttpHeaders headers = new HttpHeaders();
				headers.add("Content-Type", records.getContentType() + "; charset=" + defaultEncoding);
				headers.add("Content-Length", "0");
				headers.add("Host", records.getHost());
				headers.add("Location", joinUrl(records.getHost(), about));
				headers.add("Link", "<" + LDPVoc.Resource.stringValue() + ">; rel=\"type\", <" + LDPVoc.NonRDFSource.stringValue() + ">; rel=\"type\"");
				String describedby = about.endsWith("/") ? about + "meta" : about + "/meta";
				headers.add("Link", "<" + joinUrl(records.getHost(), describedby) + ">; rel=\"describedby\" anchor=\"" + joinUrl(records.getHost(), about) + "\"");
				headers.add("Allow", getAllowPostList(BygleSystemUtils.allowedPostNonRDFMethods));
				headers.add("Accept-Post", BygleSystemUtils.acceptPostFile);
				headers.add("ETAG", "\"" + records.getEtag() + "\"");
				if (records.getCreationDate() != null)
					headers.add("Creation-Date", records.getCreationDate().toString());
				if (records.getModifyDate() != null)
					headers.add("Modify-Date", records.getModifyDate().toString());
				if (records.getCreationEtag() != null)
					headers.add("Creation-Etag", records.getCreationEtag());
				return new ResponseEntity<String>("", headers, HttpStatus.NO_CONTENT);
			} else {
				HttpHeaders headers = new HttpHeaders();
				headers.add("Content-Type", records.getContentType() + "; charset=" + defaultEncoding);
				String relType = "";
				Iterator<Statement> iterator = content.getModel().getResource(joinUrl(records.getHost(), about)).listProperties(RDF.type);
				while (iterator.hasNext()) {
					relType += "<" + iterator.next().getResource().toString() + ">; rel=\"type\", ";
				}
				relType = StringUtils.substringBeforeLast(relType, ", ");
				headers.add("Link", relType);
				if (content.getModel().contains(content.getModel().getResource(defaultDomain + about), content.getModel().createProperty(LDPVoc.contains.stringValue()))) {
					headers.add("Allow", getAllowPostList(BygleSystemUtils.allowedPostContainerMethods));
				} else {
					headers.add("Allow", getAllowPostList(BygleSystemUtils.allowedPostRDFMethods));
				}
				headers.add("Accept-Post", BygleSystemUtils.acceptPostData + ", " + BygleSystemUtils.acceptPostFile);
				headers.add("Accept-Patch", BygleSystemUtils.acceptPatchData);
				headers.add("Content-Length", "0");
				headers.add("ETAG", "\"" + records.getEtag() + "\"");
				headers.add("Host", records.getHost());
				headers.add("Location", joinUrl(records.getHost(), about));
				if (records.getCreationDate() != null)
					headers.add("Creation-Date", records.getCreationDate().toString());
				if (records.getModifyDate() != null)
					headers.add("Modify-Date", records.getModifyDate().toString());
				if (records.getCreationEtag() != null)
					headers.add("Creation-Etag", records.getCreationEtag());
				return new ResponseEntity<String>("", headers, HttpStatus.NO_CONTENT);
			}
		} else {
			return new ResponseEntity<String>(HttpStatus.GONE);
		}
	}

	public ResponseEntity<String> head(String about, String headerHost) throws Exception {
		return options(about, headerHost);
	}

	private String getAllowPostList(String[] methods) {
		String accept = "";
		for (int i = 0; i < methods.length; i++) {
			accept += methods[i];
			accept += (i < methods.length - 1) ? ", " : "";
		}
		return accept;
	}

	private Content getContent(byte[] content, String about, String inputFormat, String outputFormat, String host, String link, String slug) throws ConfigurationException, UnsupportedEncodingException, URISyntaxException {
		if (isBinary(inputFormat)) {
			String rdfAbout = about;
			if (slug == null || slug.isEmpty()) {
				rdfAbout = rdfAbout.endsWith("/") ? rdfAbout + System.currentTimeMillis() : rdfAbout + "/" + System.currentTimeMillis();
			}
			return new Content(content, rdfAbout, BygleSystemUtils.RESOURCE_TYPE_BINARY);
		} else {
			return createContent(content, inputFormat, outputFormat, host, about, link, slug, false);
		}
	}

	private Content getContent(byte[] content, String about, String inputFormat, String outputFormat, String host) throws ConfigurationException, UnsupportedEncodingException, URISyntaxException {
		if (isBinary(inputFormat)) {
			return new Content(content, about, BygleSystemUtils.RESOURCE_TYPE_BINARY);
		} else {
			String hostAbout = joinUrl(host, about);
			Model modelTpl = getModel(content, inputFormat, hostAbout, null);
			return readContent(modelTpl, hostAbout, about, inputFormat, outputFormat, false);
		}
	}

	public String joinUrl(String firstPart, String secondPart) {
		String result = "";
		if (firstPart.endsWith("/") && secondPart.startsWith("/")) {
			result = firstPart + StringUtils.substringAfter(secondPart, "/");
		} else if (firstPart.endsWith("/") && !secondPart.startsWith("/")) {
			result = firstPart + secondPart;
		} else if (!firstPart.endsWith("/") && secondPart.startsWith("/")) {
			result = firstPart + secondPart;
		} else if (!firstPart.endsWith("/") && !secondPart.startsWith("/")) {
			result = firstPart + "/" + secondPart;
		}
		return result;
	}

	private Records getContainer(String rdfAbout) {
		List<?> recordsList = bygleService.getList(getContainerCriteria(rdfAbout));
		Records records = null;
		if (recordsList.size() > 0) {
			records = (Records) recordsList.get(0);
		}
		return records;
	}

	private boolean isContainerOrNotExist(String rdfAbout, String about) {
		List<?> recordsList = bygleService.getList(getCriteria(Records.class, Restrictions.or(Restrictions.eq("rdfAbout", rdfAbout), Restrictions.eq("rdfAbout", about))));
		if (recordsList.size() > 0) {
			Records records = (Records) recordsList.get(0);
			if (records.getRecordTypes().getIdRecordType().intValue() == BygleSystemUtils.RESOURCE_TYPE_RDF_RESOURCE || records.getRecordTypes().getIdRecordType().intValue() == BygleSystemUtils.RESOURCE_TYPE_BINARY) {
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	private DetachedCriteria getContainerCriteria(String rdfAbout) {
		String[] paths = rdfAbout.split("/");
		String father = "";
		for (int i = 0; i < paths.length - 1; i++) {
			father += !paths[i].isEmpty() ? "/" + paths[i] : "";
		}
		return getCriteria(Records.class, Restrictions.or(Restrictions.eq("rdfAbout", father), Restrictions.eq("rdfAbout", father + "/")));
	}

	private boolean isBinary(String inputFormat) {
		String format = inputFormat.toLowerCase();
		AcceptList input = AcceptList.create(format.split(","));
		MediaType matchItemInput = AcceptList.match(BygleSystemUtils.offeringRDF, input);
		if (matchItemInput != null) {
			return false;
		} else {
			return true;
		}

	}

	private void addIsMemberOfRelation(Content contentToinsert, String uriFather, String uriChildren) throws ConfigurationException {
		Resource s = contentToinsert.getModel().createResource(uriFather);
		Property p = contentToinsert.getModel().createProperty(LDPVoc.isMemberOfRelation.stringValue());
		Resource o = contentToinsert.getModel().createResource(uriChildren);
		contentToinsert.add(s, p, o);
		s = contentToinsert.getModel().createResource(uriFather);
		p = contentToinsert.getModel().createProperty(BygleSystemUtils.getStringProperty("endpoint.member.customRelation"));
		o = contentToinsert.getModel().createResource(uriChildren);
		contentToinsert.add(s, p, o);

	}

	private void addDirectContainerTriples(Content content, String uriFather, String uriChildren, Content contentToinsert) {
		try {
			Resource s = content.getModel().createResource(uriFather);
			Property p = content.getModel().createProperty(LDPVoc.membershipResource.stringValue());
			Resource o = content.getModel().createResource(uriChildren);
			content.add(s, p, o);
			s = content.getModel().createResource(uriFather);
			p = content.getModel().createProperty(LDPVoc.hasMemberRelation.stringValue());
			o = content.getModel().createResource(BygleSystemUtils.getStringProperty("endpoint.member.customRelation"));
			content.add(s, p, o);
			addIsMemberOfRelation(contentToinsert, uriChildren, uriFather);
		} catch (Exception e) {
		}
		addContainerTriples(content, uriFather, uriChildren);
	}

	private void addIndirectContainerTriples(Content content, String uriFather, String uriChildren) {
		addDirectContainerTriples(content, uriFather, uriChildren, null);
	}

	private void addContainerTriples(Content content, String uriFather, String uriChildren) {
		content.add(content.getModel().createResource(uriFather), content.getModel().createProperty(LDPVoc.contains.stringValue()), content.getModel().createResource(uriChildren));
	}

	private void makeResourceAsContainer(Content content, String uriFather, String uriChildren) {
		content.add(content.getModel().createResource(uriFather), RDF.type, content.getModel().createResource(LDPVoc.BasicContainer.stringValue()));
		addContainerTriples(content, uriFather, uriChildren);
	}

	private String getGeneratedId(RdfClasses rdfClasses) {
		return "/" + rdfClasses.getClassName() + rdfClasses.getCount();
	}

	private byte[] cleanRDF(byte[] content, String prefer, HashMap<String, ArrayList<String>> xpathMap) throws XMLException, ConfigurationException {
		String[] prefers = prefer.split(" ");
		XMLBuilder xmlBuilder = new XMLBuilder(new ByteArrayInputStream(content));
		for (int i = 0; i < prefers.length; i++) {
			List<String> xpaths = xpathMap.get(prefer);
			for (int x = 0; x < xpaths.size(); x++) {
				xmlBuilder.deleteNode(xpaths.get(x));
			}
		}
		String rdf = xmlBuilder.getXML(defaultEncoding, false);
		return rdf.getBytes();
	}

	public String joinUrl(String rdfAbout) throws URISyntaxException, ConfigurationException {
		URI uri = new URI(rdfAbout);
		String result = "";
		if (uri.isAbsolute()) {
			result = rdfAbout;
		} else {
			String host = defaultDomain;
			if (host.endsWith("/") && rdfAbout.startsWith("/")) {
				result = host + StringUtils.substringAfter(rdfAbout, "/");
			} else if (host.endsWith("/") && !rdfAbout.startsWith("/")) {
				result = host + rdfAbout;
			} else if (!host.endsWith("/") && rdfAbout.startsWith("/")) {
				result = host + rdfAbout;
			} else if (!host.endsWith("/") && !rdfAbout.startsWith("/")) {
				result = host + "/" + rdfAbout;
			}
		}
		return result;
	}

	public String getHostFromRdfAbout(String rdfAbout) throws URISyntaxException, ConfigurationException {
		URI uri = new URI(rdfAbout);
		String result = "";
		if (uri.isAbsolute()) {
			result = uri.getHost();
		} else {
			result = defaultDomain;
		}
		return result;
	}

	private Model getModel(byte[] content, String inputFormat, String hostAbout, String link) {
		AcceptList input = AcceptList.create(inputFormat.split(","));
		MediaType matchItemInput = AcceptList.match(BygleSystemUtils.offeringRDF, input);
		Lang lang = RDFLanguages.contentTypeToLang(matchItemInput.getContentType());
		Model modelTpl = ModelFactory.createDefaultModel();
		modelTpl.read(new ByteArrayInputStream(content), hostAbout, lang.getName());
		addDefaultNamespaces(modelTpl);
		if (modelTpl.getNsPrefixMap().get(LDPVoc.PREFIX) == null) {
			modelTpl.setNsPrefix(LDPVoc.PREFIX, LDPVoc.NAMESPACE);
		}
		Resource containerModel = modelTpl.getResource(hostAbout);
		if (!containerModel.hasProperty(modelTpl.createProperty(LDPVoc.Resource.stringValue()))) {
			modelTpl.add(modelTpl.createResource(hostAbout), RDF.type, modelTpl.createResource(LDPVoc.Resource.stringValue()));
		}
		if (link != null) {
			String[] links = link.split(",");
			for (int i = 0; i < links.length; i++) {
				if (links[i].indexOf("<") != -1 && links[i].indexOf(">") != -1) {
					String resource = StringUtils.substringBetween(links[i], "<", ">").trim();
					if (!resource.isEmpty()) {
						try {
							modelTpl.add(modelTpl.createResource(hostAbout), RDF.type, modelTpl.createResource(resource));
						} catch (Exception e) {
						}
					}

				}
			}
		}
		return modelTpl;
	}

	public Content createContent(byte[] content, String inputFormat, String outputFormat, String host, String about, String link, String slug, boolean isImport) throws ConfigurationException, UnsupportedEncodingException, URISyntaxException {
		String hostAbout = joinUrl(host, about);
		AcceptList input = AcceptList.create(inputFormat.split(","));
		MediaType matchItemInput = AcceptList.match(BygleSystemUtils.offeringRDF, input);
		Lang lang = RDFLanguages.contentTypeToLang(matchItemInput.getContentType());
		Model modelTpl = ModelFactory.createDefaultModel();
		// testModel(content, inputFormat, lang);
		RdfClasses rdfClasses = findRdfClasses(content, inputFormat, lang);
		String rdfAbout = about;
		if (isImport == false && slug == null && link == null) {
			List<?> recordsList = bygleService.getList(getCriteria(Records.class, Restrictions.eq("rdfAbout", about), Restrictions.eq("host", host)));
			if (recordsList.size() > 0) {
				String generatedId = URLDecoder.decode(getGeneratedId(rdfClasses), defaultEncoding).replaceAll(":", "");
				rdfAbout = joinUrl(rdfAbout, generatedId);
			} else {
				rdfAbout = URLDecoder.decode(about, defaultEncoding);
			}
			hostAbout = joinUrl(host, rdfAbout);
		}
		if (slug != null && isAlreadyUsedSlug(hostAbout)) {
			String millis = "" + System.currentTimeMillis();
			rdfAbout += millis;
			hostAbout += millis;
		}
		modelTpl.read(new ByteArrayInputStream(content), hostAbout, lang.getName());
		addDefaultNamespaces(modelTpl);
		if (modelTpl.getNsPrefixMap().get(LDPVoc.PREFIX) == null) {
			modelTpl.setNsPrefix(LDPVoc.PREFIX, LDPVoc.NAMESPACE);
		}
		if (link != null) {
			String[] links = link.split(",");
			for (int i = 0; i < links.length; i++) {
				if (links[i].indexOf("<") != -1 && links[i].indexOf(">") != -1) {
					String resource = StringUtils.substringBetween(links[i], "<", ">").trim();
					if (!resource.isEmpty()) {
						try {
							modelTpl.add(modelTpl.createResource(hostAbout), RDF.type, modelTpl.createResource(resource));
						} catch (Exception e) {
						}
					}

				}
			}
		}
		modelTpl.add(modelTpl.createResource(hostAbout), RDF.type, modelTpl.createResource(LDPVoc.RDFSource.stringValue()));
		modelTpl.add(modelTpl.createResource(hostAbout), RDF.type, modelTpl.createResource(LDPVoc.Resource.stringValue()));
		Content result = readContent(modelTpl, hostAbout, rdfAbout, inputFormat, outputFormat, isImport);
		result.setRdfClasses(rdfClasses);
		result.setRdfAbout(rdfAbout);
		return result;
	}

	public void addDefaultNamespaces(Model modelTpl) {
		for (String key : defaultNamespaces.keySet()) {
			if (modelTpl.getNsPrefixMap().get(key) == null) {
				modelTpl.setNsPrefix(key, defaultNamespaces.get(key));
			}
		}
	}

	private Content readContent(Model modelTpl, String hostAbout, String about, String inputFormat, String outputFormat, boolean isImport) throws UnsupportedEncodingException, ConfigurationException, URISyntaxException {
		Iterator<Statement> iterator = modelTpl.getResource(hostAbout).listProperties(RDF.type);
		Content result = null;
		while (iterator.hasNext()) {
			Resource resourceType = iterator.next().getResource();
			if (resourceType.toString().equalsIgnoreCase(LDPVoc.Container.stringValue())) {
				result = new Content(about, BygleSystemUtils.RESOURCE_TYPE_RDF_CONTAINER, resourceType, modelTpl, inputFormat, outputFormat != null ? outputFormat : BygleSystemUtils.defaultOutputFormat);
				break;
			} else if (resourceType.toString().equalsIgnoreCase(LDPVoc.BasicContainer.stringValue())) {
				result = new Content(about, BygleSystemUtils.RESOURCE_TYPE_RDF_BASIC_CONTAINER, resourceType, modelTpl, inputFormat, outputFormat != null ? outputFormat : BygleSystemUtils.defaultOutputFormat);
				break;
			} else if (resourceType.toString().equalsIgnoreCase(LDPVoc.DirectContainer.stringValue())) {
				result = new Content(about, BygleSystemUtils.RESOURCE_TYPE_RDF_DIRECT_CONTAINER, resourceType, modelTpl, inputFormat, outputFormat != null ? outputFormat : BygleSystemUtils.defaultOutputFormat);
				break;
			} else if (resourceType.toString().equalsIgnoreCase(LDPVoc.IndirectContainer.stringValue())) {
				result = new Content(about, BygleSystemUtils.RESOURCE_TYPE_RDF_INDIRECT_CONTAINER, resourceType, modelTpl, inputFormat, outputFormat != null ? outputFormat : BygleSystemUtils.defaultOutputFormat);
				break;
			} else if (resourceType.toString().equalsIgnoreCase(LDPVoc.RDFSource.stringValue())) {
				result = new Content(about, BygleSystemUtils.RESOURCE_TYPE_RDF_RESOURCE, resourceType, modelTpl, inputFormat, outputFormat != null ? outputFormat : BygleSystemUtils.defaultOutputFormat);
			} else if (isImport) {
				result = new Content(about, BygleSystemUtils.RESOURCE_TYPE_RDF_RESOURCE, resourceType, modelTpl, inputFormat, outputFormat != null ? outputFormat : BygleSystemUtils.defaultOutputFormat);
			}
		}
		return result;
	}

	// private void testModel(byte[] content,String inputFormat,Lang lang){
	// Model modelTpl=ModelFactory.createDefaultModel();
	// modelTpl.read(new ByteArrayInputStream(content),null,lang.getName());
	// modelTpl.write(System.out, lang.getName());
	// System.out.println("---------------------------------------------------------------------------------------");
	// }

	public RdfClasses findRdfClasses(byte[] content, String inputFormat, Lang lang) {
		Model modelTpl = ModelFactory.createDefaultModel();
		modelTpl.read(new ByteArrayInputStream(content), null, lang.getName());
		RdfClasses rdfClasses = null;
		StmtIterator iterator = modelTpl.listStatements();
		while (iterator.hasNext()) {
			Statement statement = iterator.next();
			if (statement.getPredicate().toString().equalsIgnoreCase(RDF.type.toString())) {
				String className = "defaultResource";
				try {
					className = new URI(URLEncoder.encode(statement.getObject().asResource().getLocalName(), defaultEncoding)).toString();
				} catch (UnsupportedEncodingException e) {
				} catch (URISyntaxException e) {
				}
				List<?> list = bygleService.getList(getCriteria(RdfClasses.class, Restrictions.eq("className", className)));
				if (list.size() == 0) {
					rdfClasses = new RdfClasses(className.toString(), statement.getObject().asResource().toString(), 1);
					bygleService.add(rdfClasses);
					return rdfClasses;
				} else {
					RdfClasses rdfC = (RdfClasses) list.get(0);
					if (rdfClasses != null && rdfClasses.getIdRdfClass().intValue() != rdfC.getIdRdfClass().intValue()) {
						rdfClasses.setCount(rdfClasses.getCount() + 1);
						bygleService.update(rdfClasses);
						return rdfClasses;
					}
				}
			}
		}
		if (rdfClasses == null) {
			List<?> list = bygleService.getList(getCriteria(RdfClasses.class, Restrictions.eq("className", LDPVoc.RDFSource.getLocalName())));
			if (list.size() == 0) {
				rdfClasses = new RdfClasses(LDPVoc.RDFSource.getLocalName(), LDPVoc.RDFSource.stringValue(), 1);
				bygleService.add(rdfClasses);
			} else {
				rdfClasses = (RdfClasses) list.get(0);
				rdfClasses.setCount(rdfClasses.getCount() + 1);
				bygleService.update(rdfClasses);
			}
		}
		return rdfClasses;
	}

	private void createContainer(String about, String description, org.openrdf.model.URI containerType) throws ConfigurationException, UnsupportedEncodingException, URISyntaxException {
		String turtleContainer = "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.\n";
		turtleContainer += "@prefix dcterms: <http://purl.org/dc/terms/>.\n";
		turtleContainer += "@prefix ldp: <http://www.w3.org/ns/ldp#>.\n";
		turtleContainer += "	\n";
		turtleContainer += "<> a ldp:Container, " + LDPVoc.PREFIX + ":" + containerType.getLocalName() + ", ldp:Resource, ldp:RDFSource;\n";
		turtleContainer += "  dcterms:title \"" + description + "\" .\n";
		List<?> recordsList = bygleService.getList(getCriteria(Records.class, Restrictions.eq("rdfAbout", about)));
		if (recordsList.size() == 0) {
			Content contentToInsert = getContent(turtleContainer.getBytes(), about, BygleSystemUtils.INPUTFORMAT_TEXT_TURTLE, BygleSystemUtils.INPUTFORMAT_APPLICATION_RDF_XML, defaultDomain, null, null);
			String rdf = new String(contentToInsert.getContent());
			String md5ETag = DigestUtils.md5Hex(rdf);
			RecordTypes recordTypes = (RecordTypes) bygleService.getObject(RecordTypes.class, contentToInsert.getResourceType());
			Records records = new Records(recordTypes, contentToInsert.getRdfClasses(), rdf.getBytes(), new Date(), null, about, BygleSystemUtils.INPUTFORMAT_APPLICATION_RDF_XML, md5ETag, "", defaultDomain);
			bygleService.add(records);
			Slugs newSlug = new Slugs(about);
			bygleService.add(newSlug);
		}
	}

	public DetachedCriteria getCriteria(Class<?> c, Criterion... expressions) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(c);
		for (int i = 0; i < expressions.length; i++) {
			detachedCriteria.add(expressions[i]);
		}
		return detachedCriteria;
	}

	@PostConstruct
	private void configure() {
		try {
			defaultDomain = BygleSystemUtils.getStringProperty("endpoint.defaultDomain");
			defaultEncoding = BygleSystemUtils.getStringProperty("default.encoding");

			// createContainer(
			// BygleSystemUtils.getStringProperty("endpoint.defaultDomain")+"/basic",
			// "Basic Container", LDPVoc.BasicContainer);
			// createContainer("/direct", "Direct Container",
			// LDPVoc.DirectContainer);
			// createContainer("/indirect", "Indirect Container",
			// LDPVoc.IndirectContainer);

			createContainer(BygleSystemUtils.getStringProperty("endpoint.defaultBasicContainerAbout"), BygleSystemUtils.getStringProperty("endpoint.defaultBasicContainerDescription"), LDPVoc.BasicContainer);
			createContainer(BygleSystemUtils.getStringProperty("endpoint.defaultDirectContainerAbout"), BygleSystemUtils.getStringProperty("endpoint.defaultDirectContainerDescription"), LDPVoc.DirectContainer);
			createContainer(BygleSystemUtils.getStringProperty("endpoint.defaultIndirectContainerAbout"), BygleSystemUtils.getStringProperty("endpoint.defaultIndirectContainerDescription"), LDPVoc.IndirectContainer);

		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}

}
