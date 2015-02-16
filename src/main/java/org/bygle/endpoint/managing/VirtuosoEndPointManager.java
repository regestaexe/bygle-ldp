package org.bygle.endpoint.managing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
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
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class VirtuosoEndPointManager extends EndPointManager {
	@Autowired
	RelationsService relationsService;

	@Autowired
	LDPService ldpService;

	@Autowired
	BygleService bygleService;
	private int port = 1111;
	private String username, password, defaultGraph, server;

	Map<String, VirtGraph> endpointsMap = new HashMap<String, VirtGraph>();

	public VirtuosoEndPointManager() {
	}

	public void createConnection() throws RepositoryException {
	}

	public void closeConnection() {
		for (String chiave : endpointsMap.keySet()) {
			if (endpointsMap.get(chiave) != null && !endpointsMap.get(chiave).isClosed()) {
				try {
					endpointsMap.get(chiave).close();
				} catch (Exception e) {
					System.err.println("connection already closed (" + chiave + ")? " + e.getMessage());
				}
			}
		}
	}

	@Override
	public void publishRecord(byte[] rdf, String rdfAbout) throws Exception {
		System.out.println("VirtuosoEndPointManager.publishRecord()");
		if (!endpointsMap.containsKey(server + "+" + defaultGraph)) {
			System.out.println("connecting to  " + server + " for graph " + defaultGraph);
			endpointsMap.put(server + "+" + defaultGraph, new VirtGraph(defaultGraph, "jdbc:virtuoso://" + server + ":" + port, username, password));
			System.out.println("connected!");
		}
		VirtGraph virtGraph = endpointsMap.get(server + "+" + defaultGraph);
		Model m = ModelFactory.createDefaultModel();
		m.read(new ByteArrayInputStream(rdf), "");

		Map<String, Node> sub = new HashMap<String, Node>();

		for (StmtIterator i = m.listStatements(); i.hasNext();) {
			Statement s = (Statement) i.next();
			Triple a = s.asTriple();
			if (a.getObject().isBlank()) {
				String obs = a.getObject().getBlankNodeLabel();
				if (!sub.containsKey(obs)) {
					sub.put(obs, Node.createURI("nodeID://b" + System.nanoTime()));
				}
			}
			if (a.getSubject().isBlank()) {
				String obs = a.getSubject().getBlankNodeLabel();
				if (!sub.containsKey(obs)) {
					sub.put(obs, Node.createURI("nodeID://b" + System.nanoTime()));
				}
			}
		}

		for (StmtIterator i = m.listStatements(); i.hasNext();) {
			Statement s = (Statement) i.next();
			Triple a = s.asTriple();
			if (a.getObject().isBlank()) {
				String obs = a.getObject().getBlankNodeLabel();
				a = Triple.create(a.getSubject(), a.getPredicate(), sub.get(obs));
			}
			if (a.getSubject().isBlank()) {
				String obs = a.getSubject().getBlankNodeLabel();
				a = Triple.create(sub.get(obs), a.getPredicate(), a.getObject());
			}
			Node o = a.getObject();
			if (o.isLiteral() && o.getLiteralDatatypeURI() != null) {
				String value = o.getLiteralLexicalForm();
				value = value.replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n");
				String str = "INSERT INTO GRAPH <" + virtGraph.getGraphName() + "> { <" + a.getSubject() + "> <" + a.getPredicate() + "> \"" + value + "\"^^<" + o.getLiteralDatatypeURI() + ">. }";
				VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(str, virtGraph);
				vur.exec();
			} else {
				virtGraph.add(a);
			}
		}

	}

	@Override
	public void dePublishRecord(byte[] rdf, String rdfAbout) {
		System.out.println("VirtuosoEndPointManager.dePublishRecord()");
		System.out.println("removing " + rdfAbout);
		if (!endpointsMap.containsKey(server + "+" + defaultGraph)) {
			System.out.println("connecting to  " + server + " for graph " + defaultGraph);
			endpointsMap.put(server + "+" + defaultGraph, new VirtGraph(defaultGraph, "jdbc:virtuoso://" + server + ":" + port, username, password));
			System.out.println("connected!");
		}
		VirtGraph virtGraph = endpointsMap.get(server + "+" + defaultDomain);

		System.out.println("dePublishRecord from " + virtGraph.getGraphName() + " -- URI -- " + rdfAbout);
		StringBuilder query = new StringBuilder();
		query.append(" DELETE FROM GRAPH <" + virtGraph.getGraphName() + "> {?bn ?a ?b}  WHERE {");
		query.append("{<" + defaultDomain + rdfAbout + "> ?p ?o");
		query.append(". FILTER(isBlank(?o))");
		query.append(". ?o ?c ?s");
		query.append(". FILTER(isBlank(?s))");
		query.append(". ?s ?d ?bn");
		query.append(". FILTER(isBlank(?bn))}");
		query.append("UNION{");
		query.append("<" + defaultDomain + rdfAbout + "> ?p ?o");
		query.append(". FILTER(isBlank(?o))");
		query.append(". ?o ?c ?bn");
		query.append(". FILTER(isBlank(?bn))}");
		query.append("UNION{");
		query.append(" <" + defaultDomain + rdfAbout + "> ?p ?bn");
		query.append(". FILTER(isBlank(?bn))");
		query.append("} ?bn ?a ?b}");
		VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(query.toString(), virtGraph);
		vur.exec();
		vur = VirtuosoUpdateFactory.create("delete from graph <" + virtGraph.getGraphName() + "> {?s ?p ?o.} FROM <" + virtGraph.getGraphName() + "> WHERE {?s ?p ?o. FILTER(?s = <" + defaultDomain + rdfAbout + ">)}", virtGraph);
		vur.exec();

	}

	@Override
	public void rePublishRecord(byte[] rdf, String rdfAbout) throws Exception {
		System.out.println("VirtuosoEndPointManager.rePublishRecord()");
		dePublishRecord(rdf, rdfAbout);
		publishRecord(rdf, rdfAbout);
	}

	@Override
	public void resetEndpoint() throws Exception {
		System.out.println("VirtuosoEndPointManager.resetEndpoint()");
		if (!endpointsMap.containsKey(server + "+" + defaultGraph)) {
			System.out.println("connecting to  " + server + " for graph " + defaultGraph);
			endpointsMap.put(server + "+" + defaultGraph, new VirtGraph(defaultGraph, "jdbc:virtuoso://" + server + ":" + port, username, password));
			System.out.println("connected!");
		}
		VirtGraph virtGraph = endpointsMap.get(server + "+" + defaultDomain);
		if (virtGraph != null)
			virtGraph.clear();
	}

	@Override
	public void dropEndpoint() throws Exception {
		System.out.println("VirtuosoEndPointManager.dropEndpoint()");
		resetEndpoint();
	}

	@Override
	public void executeImport() throws Exception {
		super.executeImport();
		File importDir = new File(importDirectory);
		if (importDir.list().length > 0) {
			List<RelationsContainer> addRelationsContainerList = new ArrayList<RelationsContainer>();
			List<RelationsContainer> updateRelationsContainerList = new ArrayList<RelationsContainer>();
			File[] importFiles = importDir.listFiles();
			Model modelBase = ModelFactory.createDefaultModel();
			// ldpService.addDefaultNamespaces(modelBase);
			System.out.println("VirtuosoEndPointManager.executeImport() reading files");
			for (int i = 0; i < importFiles.length; i++) {
				if (importFiles[i].isFile()) {
					try {
						System.out.println("loading RDF " + importFiles[i].getAbsolutePath());
						FileManager.get().readModel(modelBase, importFiles[i].getAbsolutePath());
					} catch (Exception e) {
						e.printStackTrace();
						FileUtils.moveFile(importFiles[i], new File(importFiles[i].getAbsolutePath().replaceAll("(.+)\\.(\\w+)$", "$1_error.$2")));
						FileUtils.writeStringToFile(new File(importFiles[i].getAbsolutePath().replaceAll("(.+)\\.(\\w+)$", "$1_error.$2.log")), e.getMessage());
					}
				}
			}
			ResIterator resources = modelBase.listSubjects();
			System.out.println("VirtuosoEndPointManager.executeImport() starting import");
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
						modelResource.write(byteArrayOutputStream, BygleSystemUtils.getWriter("application/rdf+xml-abbr")); //diego: RDF/XML-ABBREV
						XMLReader xmlReader = new XMLReader(byteArrayOutputStream.toByteArray());
						String rdfAbout = xmlReader.getNodeValue("/rdf:RDF/*/@rdf:about");
						System.out.println("VirtuosoEndPointManager.executeImport() importing " + rdfAbout);
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

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getDefaultGraph() {
		return defaultGraph;
	}

	public void setDefaultGraph(String defaultGraph) {
		this.defaultGraph = defaultGraph;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
