package org.bygle.endpoint.managing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.bygle.bean.RdfClasses;
import org.bygle.bean.RecordTypes;
import org.bygle.bean.Records;
import org.bygle.db.services.BygleService;
import org.bygle.endpoint.managing.utils.RelationsContainer;
import org.bygle.service.LDPService;
import org.bygle.service.RelationsService;
import org.bygle.service.bean.Content;
import org.bygle.utils.BygleSystemUtils;
import org.bygle.xml.XMLReader;
import org.bygle.xslt.TrasformXslt;
import org.dom4j.Namespace;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.util.FileManager;

public class JenaEndPointManager extends EndPointManager {
	@Autowired
	@Qualifier("jenaDataSource")
	private DataSource jenaDataSource;

	@Autowired
	RelationsService relationsService;

	@Autowired
	LDPService ldpService;

	@Autowired
	BygleService bygleService;

	private String databaseType;

	// private static final Logger logger =
	// LoggerFactory.getLogger(JenaEndPointManager.class);

	public JenaEndPointManager() {
	}

	@Override
	public void publishRecord(byte[] rdf, String rdfAbout, String host) throws Exception {
		super.publishRecord(rdf, rdfAbout, host);
		SDBConnection conn = new SDBConnection(jenaDataSource);
		StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash, BygleSystemUtils.getDBType(databaseType));
		Store store = SDBFactory.connectStore(conn, storeDesc);
		if (!StoreUtils.isFormatted(store))
			store.getTableFormatter().create();
		Dataset dataset = SDBFactory.connectDataset(store);
		Model modelTpl = ModelFactory.createDefaultModel();
		modelTpl.read(new ByteArrayInputStream(rdf), "");
		dataset.getDefaultModel().add(modelTpl);
		store.getConnection().close();
		store.close();
	}

	@Override
	public void dePublishRecord(byte[] rdf, String rdfAbout, String host) throws Exception {
		super.dePublishRecord(rdf, rdfAbout, host);
		SDBConnection conn = new SDBConnection(jenaDataSource);
		StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash, BygleSystemUtils.getDBType(databaseType));
		Store store = SDBFactory.connectStore(conn, storeDesc);
		Dataset dataset = SDBFactory.connectDataset(store);
		Model modelTpl = ModelFactory.createDefaultModel();
		modelTpl.read(new ByteArrayInputStream(rdf), "");

		StringBuilder query = new StringBuilder();
		query.append("DELETE  {?bn ?a ?b}   WHERE {");
		query.append("{<" + host + "/" + rdfAbout + "> ?p ?o");
		query.append(". FILTER(isBlank(?o))");
		query.append(". ?o ?c ?s");
		query.append(". FILTER(isBlank(?s))");
		query.append(". ?s ?d ?bn");
		query.append(". FILTER(isBlank(?bn))}");
		query.append("UNION{");
		query.append("<" + host + "/" + rdfAbout + "> ?p ?o");
		query.append(". FILTER(isBlank(?o))");
		query.append(". ?o ?c ?bn");
		query.append(". FILTER(isBlank(?bn))}");
		query.append("UNION{");
		query.append(" <" + host + "/" + rdfAbout + "> ?p ?bn");
		query.append(". FILTER(isBlank(?bn))");
		query.append("} ?bn ?a ?b}");

		UpdateAction.parseExecute(query.toString(), modelTpl);
		modelTpl.removeAll(modelTpl.createResource(host + "/" + rdfAbout), null, null);
		dataset.getDefaultModel().remove(modelTpl);
		store.getConnection().close();
		store.close();

	}

	@Override
	public ResponseEntity<?> query(String defaultGraphUri, String sparqlQuery, int outputFormat) throws Exception {
		super.query(defaultGraphUri, sparqlQuery, outputFormat);
		SDBConnection conn = new SDBConnection(jenaDataSource);
		StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash, BygleSystemUtils.getDBType(databaseType));
		Store store = SDBFactory.connectStore(conn, storeDesc);
		Query query = null;
		if (defaultGraphUri != null && !defaultGraphUri.trim().isEmpty())
			query = QueryFactory.create(sparqlQuery, defaultGraphUri);
		else
			query = QueryFactory.create(sparqlQuery);
		Dataset ds = SDBFactory.connectDataset(store);
		ResponseEntity<?> result;
		try (QueryExecution qe = QueryExecutionFactory.create(query, ds)) {
			if (query.isAskType()) {
				result = formatAskOutput(qe.execAsk(), outputFormat);
			} else if (query.isDescribeType()) {
				if (outputFormat == BygleSystemUtils.OUTPUTFORMAT_TSV || outputFormat == BygleSystemUtils.OUTPUTFORMAT_CSV)
					result = formatDescribeConstructOutput(qe.execDescribeTriples(), outputFormat);
				else
					result = formatDescribeConstructOutput(qe.execDescribe(), outputFormat);
			} else if (query.isConstructType()) {
				if (outputFormat == BygleSystemUtils.OUTPUTFORMAT_TSV || outputFormat == BygleSystemUtils.OUTPUTFORMAT_CSV)
					result = formatDescribeConstructOutput(qe.execConstructTriples(), outputFormat);
				else
					result = formatDescribeConstructOutput(qe.execConstruct(), outputFormat);
			} else if (query.isSelectType()) {
				ResultSet resultSet = qe.execSelect();
				result = formatSelectOutput(resultSet, outputFormat);
			} else {
				throw new Exception("unknown query format");
			}

			store.getConnection().close();
			store.close();
		} catch (Exception e) {
			store.getConnection().close();
			store.close();
			throw e;
		}
		return result;
	}

	private ResponseEntity<?> formatSelectOutput(ResultSet resultSet, int outputFormat) throws Exception {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		HttpHeaders headers = new HttpHeaders();
		switch (outputFormat) {
		case BygleSystemUtils.OUTPUTFORMAT_BIO:
			ResultSetFormatter.outputAsBIO(byteArrayOutputStream, resultSet);
			headers.add("Content-Type", "text/bio" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.bio");
			headers.add("Content-Length", Integer.toString(byteArrayOutputStream.toByteArray().length));
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_CSV:
			ResultSetFormatter.outputAsCSV(byteArrayOutputStream, resultSet);
			headers.add("Content-Type", "text/csv" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.csv");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_JSON:
			ResultSetFormatter.outputAsJSON(byteArrayOutputStream, resultSet);
			headers.add("Content-Type", "application/json" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.json");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_RDF:
			ResultSetFormatter.outputAsRDF(byteArrayOutputStream, "RDF/XML", resultSet);
			headers.add("Content-Type", "application/rdf+xml" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.rdf");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_RDF_ABBR:
			ResultSetFormatter.outputAsRDF(byteArrayOutputStream, "RDF/XML-ABBREV", resultSet);
			headers.add("Content-Type", "application/rdf+xml" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.rdf");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_TSV:
			ResultSetFormatter.outputAsTSV(byteArrayOutputStream, resultSet);
			headers.add("Content-Type", "text/tab-separated-values" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.tsv");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_XML:
			headers.add("Content-Type", "application/xml" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.xml");
			return new ResponseEntity<byte[]>(ResultSetFormatter.asXMLString(resultSet).getBytes(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_N_TRIPLE:
			ResultSetFormatter.outputAsRDF(byteArrayOutputStream, "N-TRIPLE", resultSet);
			headers.add("Content-Type", "application/n-triples" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.n3");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_TURTLE:
			ResultSetFormatter.outputAsRDF(byteArrayOutputStream, "TURTLE", resultSet);
			headers.add("Content-Type", "text/turtle" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.ttl");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_HTML:
			headers.add("Content-Type", "text/html" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			return new ResponseEntity<byte[]>(TrasformXslt.xslt(ResultSetFormatter.asXMLString(resultSet), BygleSystemUtils.getXSLHTMLTController()).getBytes(), headers, HttpStatus.OK);
		default:
			headers.add("Content-Type", "text/html" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			return new ResponseEntity<byte[]>(TrasformXslt.xslt(ResultSetFormatter.asXMLString(resultSet), BygleSystemUtils.getXSLHTMLTController()).getBytes(), headers, HttpStatus.OK);
		}
	}

	private ResponseEntity<?> formatDescribeConstructOutput(Iterator<Triple> iterator, int outputFormat) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		String content = "";
		String separator = ",";
		if (outputFormat == BygleSystemUtils.OUTPUTFORMAT_TSV) {
			separator = "\t";
			headers.add("Content-Type", "text/tab-separated-values" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.tsv");
		} else {
			headers.add("Content-Type", "text/csv" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.csv");
		}
		while (iterator.hasNext()) {
			Triple triple = (Triple) iterator.next();
			content += triple.getSubject().toString() + separator + triple.getPredicate().toString() + separator + triple.getObject().toString() + "\n";
		}
		return new ResponseEntity<byte[]>(content.getBytes(), headers, HttpStatus.OK);
	}

	private ResponseEntity<?> formatDescribeConstructOutput(Model model, int outputFormat) throws Exception {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		HttpHeaders headers = new HttpHeaders();
		switch (outputFormat) {
		case BygleSystemUtils.OUTPUTFORMAT_JSON:
			model.write(byteArrayOutputStream, BygleSystemUtils.getWriter("application/rdf+json"));
			headers.add("Content-Type", "application/json" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.json");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_RDF:
			model.write(byteArrayOutputStream, BygleSystemUtils.getWriter("application/rdf+xml"));
			headers.add("Content-Type", "application/rdf+xml" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.rdf");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_RDF_ABBR:
			model.write(byteArrayOutputStream, BygleSystemUtils.getWriter("application/rdf+xml-abbr"));
			headers.add("Content-Type", "application/rdf+xml" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.rdf");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_XML:
			model.write(byteArrayOutputStream, BygleSystemUtils.getWriter("application/rdf+xml"));
			headers.add("Content-Type", "application/xml" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.xml");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_N_TRIPLE:
			model.write(byteArrayOutputStream, BygleSystemUtils.getWriter("application/n-triples"));
			headers.add("Content-Type", "application/n-triples" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.n3");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_TURTLE:
			model.write(byteArrayOutputStream, BygleSystemUtils.getWriter("application/x-turtle"));
			headers.add("Content-Type", "text/turtle" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.ttl");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_HTML:
			model.write(byteArrayOutputStream, BygleSystemUtils.getWriter("application/rdf+xml"));
			headers.add("Content-Type", "text/html" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			return new ResponseEntity<byte[]>(TrasformXslt.xslt(new String(byteArrayOutputStream.toByteArray()), BygleSystemUtils.getXSLHTMLTController()).getBytes(), headers, HttpStatus.OK);
		default:
			model.write(byteArrayOutputStream, BygleSystemUtils.getWriter("application/rdf+xml"));
			headers.add("Content-Type", "text/html" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			return new ResponseEntity<byte[]>(TrasformXslt.xslt(new String(byteArrayOutputStream.toByteArray()), BygleSystemUtils.getXSLHTMLTController()).getBytes(), headers, HttpStatus.OK);
		}
	}

	private ResponseEntity<?> formatAskOutput(boolean ask, int outputFormat) throws Exception {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		HttpHeaders headers = new HttpHeaders();
		switch (outputFormat) {
		case BygleSystemUtils.OUTPUTFORMAT_CSV:
			ResultSetFormatter.outputAsCSV(byteArrayOutputStream, ask);
			headers.add("Content-Type", "text/csv" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.csv");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_JSON:
			ResultSetFormatter.outputAsJSON(byteArrayOutputStream, ask);
			headers.add("Content-Type", "application/json" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.json");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_RDF:
			ResultSetFormatter.outputAsRDF(byteArrayOutputStream, "RDF/XML", ask);
			headers.add("Content-Type", "application/rdf+xml" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.rdf");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_RDF_ABBR:
			ResultSetFormatter.outputAsRDF(byteArrayOutputStream, "RDF/XML-ABBREV", ask);
			headers.add("Content-Type", "application/rdf+xml" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.rdf");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_TSV:
			ResultSetFormatter.outputAsTSV(byteArrayOutputStream, ask);
			headers.add("Content-Type", "text/tab-separated-values" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.tsv");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_XML:
			headers.add("Content-Type", "application/xml" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.xml");
			return new ResponseEntity<byte[]>(ResultSetFormatter.asXMLString(ask).getBytes(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_N_TRIPLE:
			ResultSetFormatter.outputAsRDF(byteArrayOutputStream, "N-TRIPLE", ask);
			headers.add("Content-Type", "application/n-triples" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.n3");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_TURTLE:
			ResultSetFormatter.outputAsRDF(byteArrayOutputStream, "TURTLE", ask);
			headers.add("Content-Type", "text/turtle" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			headers.add("Content-Disposition", "attachment; filename=query.ttl");
			return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
		case BygleSystemUtils.OUTPUTFORMAT_HTML:
			headers.add("Content-Type", "text/html" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			return new ResponseEntity<byte[]>(TrasformXslt.xslt(ResultSetFormatter.asXMLString(ask), BygleSystemUtils.getXSLHTMLTController()).getBytes(), headers, HttpStatus.OK);
		default:
			headers.add("Content-Type", "text/html" + "; charset=" + BygleSystemUtils.getStringProperty("default.encoding"));
			return new ResponseEntity<byte[]>(TrasformXslt.xslt(ResultSetFormatter.asXMLString(ask), BygleSystemUtils.getXSLHTMLTController()).getBytes(), headers, HttpStatus.OK);
		}
	}

	@Override
	public void rePublishRecord(byte[] rdf, String rdfAbout, String host) throws Exception {
		super.rePublishRecord(rdf, rdfAbout, host);
		dePublishRecord(rdf, rdfAbout, host);
		publishRecord(rdf, rdfAbout, host);
	}

	private void addAnon(Model modelBase, Model modelResource, List<Statement> statementList) {
		for (int i = 0; i < statementList.size(); i++) {
			Statement statement = statementList.get(i);
			if (statement.getObject().isAnon()) {
				List<Statement> newStatementList = modelBase.listStatements(new SimpleSelector(statement.getObject().asResource(), null, null, null)).toList();
				modelResource.add(newStatementList);
				addAnon(modelBase, modelResource, newStatementList);
			}
		}
	}

	@Override
	public void executeImport() throws Exception {
		super.executeImport();
		// WebApplicationContext springContext =
		// WebApplicationContextUtils.getWebApplicationContext(servletConext);
		// relationsService = (RelationsService)
		// springContext.getBean("relationsService");
		// ldpService = (LDPService) springContext.getBean("ldpService");
		// bygleService = (BygleService) springContext.getBean("bygleService");

		File importDir = new File(importDirectory);
		if (importDir.list().length > 0) {
			List<RelationsContainer> addRelationsContainerList = new ArrayList<RelationsContainer>();
			List<RelationsContainer> updateRelationsContainerList = new ArrayList<RelationsContainer>();
			File[] importFiles = importDir.listFiles();
			Model modelBase = ModelFactory.createDefaultModel();
			// ldpService.addDefaultNamespaces(modelBase);
			for (int i = 0; i < importFiles.length; i++) {
				if (importFiles[i].isFile()) {
					try {
						System.out.println("loading RDF " + importFiles[i].getAbsolutePath());
						FileManager.get().readModel(modelBase, importFiles[i].getAbsolutePath());
					} catch (Exception e) {
						// e.printStackTrace();
						System.err.println("[bygle - error] importing " + e.getMessage());
						FileUtils.moveFile(importFiles[i], new File(importFiles[i].getAbsolutePath().replaceAll("(.+)\\.(\\w+)$", "$1_error.$2")));
						FileUtils.writeStringToFile(new File(importFiles[i].getAbsolutePath().replaceAll("(.+)\\.(\\w+)$", "$1_error.$2.log")), e.getMessage());
					}
				}
			}
			ResIterator resources = modelBase.listSubjects();
			while (resources.hasNext()) {
				Resource resource = (Resource) resources.next();
				if (!resource.isAnon()) {
					try {
						List<Statement> statementList = modelBase.listStatements(new SimpleSelector(resource, null, null, null)).toList();
						Model modelResource = ModelFactory.createDefaultModel();
						modelResource.setNsPrefixes(modelBase.getNsPrefixMap());
						modelResource.add(statementList);
						addAnon(modelBase, modelResource, statementList);
						ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
						modelResource.write(byteArrayOutputStream, BygleSystemUtils.getWriter("application/rdf+xml-abbr"));
						XMLReader xmlReader = new XMLReader(byteArrayOutputStream.toByteArray());
						String rdfAbout = xmlReader.getNodeValue("/rdf:RDF/*/@rdf:about");
						List<?> nodeList = xmlReader.getNodeList("/rdf:RDF/*/*/@rdf:resource[not(ancestor::rdf:type)]");
						Content content = ldpService.createContent(byteArrayOutputStream.toByteArray(), "application/rdf+xml", "application/rdf+xml", BygleSystemUtils.getStringProperty("endpoint.defaultDomain"), rdfAbout, null, null, true);
						RecordTypes recordTypes = (RecordTypes) bygleService.getObject(RecordTypes.class, content.getResourceType());
						RdfClasses rdfClasses = getRdfClasses(content.getENTITY_TYPE(), rdfAbout);
						String md5ETag = DigestUtils.md5Hex(new String(byteArrayOutputStream.toByteArray()));
						DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Records.class);
						detachedCriteria.add(Restrictions.eq("rdfAbout", rdfAbout));
						detachedCriteria.add(Restrictions.not(Restrictions.eq("etag", md5ETag)));
						detachedCriteria.add(Restrictions.eq("host", ldpService.getHostFromRdfAbout(rdfAbout)));
						List<?> recordsList = bygleService.getList(detachedCriteria);
						if (recordsList.size() > 0) {
							Records records = (Records) recordsList.get(0);
							byte[] oldRdf = records.getRdf();
							records.setModifyDate(new Date());
							records.setEtag(md5ETag);
							records.setRdf(byteArrayOutputStream.toByteArray());
							bygleService.update(records);
							updateRelationsContainerList.add(new RelationsContainer(records.getIdRecord(), oldRdf));
							relationsService.updateRelations(records, oldRdf);
						} else {
							detachedCriteria = DetachedCriteria.forClass(Records.class);
							detachedCriteria.add(Restrictions.eq("rdfAbout", rdfAbout));
							detachedCriteria.add(Restrictions.eq("etag", md5ETag));
							detachedCriteria.add(Restrictions.eq("host", ldpService.getHostFromRdfAbout(rdfAbout)));
							recordsList = bygleService.getList(detachedCriteria);
							if (recordsList.size() == 0) {
								Records records = new Records(recordTypes, rdfClasses, byteArrayOutputStream.toByteArray(), new Date(), null, rdfAbout, "application/rdf+xml", md5ETag, md5ETag, ldpService.getHostFromRdfAbout(rdfAbout));
								bygleService.add(records);
								addRelationsContainerList.add(new RelationsContainer(records.getIdRecord(), nodeList));
							}
						}

					} catch (Exception e) {
					}
				}
			}
			FileUtils.cleanDirectory(importDir);
		}

	}

	public synchronized RdfClasses getRdfClasses(Resource ENTITY_TYPE, String about) throws UnsupportedEncodingException, URISyntaxException {
		RdfClasses rdfClasses = null;
		if (ENTITY_TYPE != null) {
			DetachedCriteria detachedCriteria = DetachedCriteria.forClass(RdfClasses.class);
			detachedCriteria.add(Restrictions.eq("rdfType", ENTITY_TYPE.toString()));
			List<?> list = bygleService.getList(detachedCriteria);
			if (list.size() == 0) {
				try {
					java.net.URI className = new java.net.URI(URLEncoder.encode(ENTITY_TYPE.getModel().qnameFor(ENTITY_TYPE.getURI()), BygleSystemUtils.getStringProperty("default.encoding")));
					rdfClasses = new RdfClasses(className.toString(), ENTITY_TYPE.getURI(), 1);
				} catch (Exception e) {
					rdfClasses = new RdfClasses("defaultResource", ENTITY_TYPE.getURI(), 1);
				}
				bygleService.add(rdfClasses);
				return rdfClasses;
			} else {

				rdfClasses = (RdfClasses) list.get(0);
				if (about.indexOf(ENTITY_TYPE.toString()) == -1) {
					rdfClasses.setCount(rdfClasses.getCount() + 1);
					bygleService.update(rdfClasses);
				}
			}
		}
		return rdfClasses;
	}

	public String buildRDF(String xmlBase, List<Namespace> list, String rdf) {
		String result = "<rdf:RDF ";
		for (int i = 0; i < list.size(); i++) {
			Namespace namespace = list.get(i);
			result += namespace.getPrefix().isEmpty() ? "xmlns=\"" + namespace.getURI() + "\" " : "xmlns:" + namespace.getPrefix() + "=\"" + namespace.getURI() + "\" ";
		}
		result += !xmlBase.isEmpty() ? "xml:base=\"" + xmlBase + "\"" : "";
		result += ">\n" + rdf;
		result += "\n</rdf:RDF>";
		return result;
	}

	@Override
	public void executePublishing() throws Exception {
		super.executeImport();
		SDBConnection conn = new SDBConnection(jenaDataSource);
		StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash, BygleSystemUtils.getDBType(databaseType));
		Store store = SDBFactory.connectStore(conn, storeDesc);
		if (!StoreUtils.isFormatted(store))
			store.getTableFormatter().create();
		File importDir = new File(importDirectory);
		FileFilter fileFilter = new WildcardFileFilter("*.nt");
		File[] importFiles = importDir.listFiles(fileFilter);
		if (importFiles.length > 0) {
			OntModel ontModel = ModelFactory.createOntologyModel();
			FileFilter ontologyFileFilter = new WildcardFileFilter("*.owl");
			File[] ontologyfiles = importDir.listFiles(ontologyFileFilter);
			for (int x = 0; x < ontologyfiles.length; x++) {
				FileManager.get().readModel(ontModel, ontologyfiles[x].getAbsolutePath());
			}
			System.out.println("##############################STARTING PUBLISHING#############################");
			for (int i = 0; i < importFiles.length; i++) {
				Model modelTpl = ModelFactory.createDefaultModel();
				FileManager.get().readModel(modelTpl, importFiles[i].getAbsolutePath());
				System.out.println("PUBLISHING  FILE " + importFiles[i].getName());
				System.out.println("##############################START SAVING DATA###############################");
				ontModel.add(modelTpl);
			}
			Dataset dataset = SDBFactory.connectDataset(store);
			dataset.getDefaultModel().add(ontModel);
			store.getConnection().close();
			store.close();
			System.out.println("##############################END PUBLISHING##################################");
			FileUtils.cleanDirectory(importDir);
			System.out.println("##############################PUBLISHING SUCCESS##############################");
		} else {
			System.out.println("##############################NO FILES TO PUBLISH##############################");
		}
	}

	public DataSource getJenaDataSource() {
		return jenaDataSource;
	}

	public void setJenaDataSource(DataSource jenaDataSource) {
		this.jenaDataSource = jenaDataSource;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	@Override
	public void resetEndpoint() throws Exception {
		Connection connection = null;
		java.sql.Statement statement = null;
		try {
			connection = jenaDataSource.getConnection();
			statement = connection.createStatement();
			statement.executeUpdate("TRUNCATE nodes;");
			statement.executeUpdate("TRUNCATE prefixes;");
			statement.executeUpdate("TRUNCATE quads;");
			statement.executeUpdate("TRUNCATE triples;");
			statement.close();
		} catch (SQLException se) {
		} catch (Exception e) {
		} finally {
			try {
				if (connection != null && !connection.isClosed())
					connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}

	@Override
	public void dropEndpoint() throws Exception {
		resetEndpoint();
	}

}
