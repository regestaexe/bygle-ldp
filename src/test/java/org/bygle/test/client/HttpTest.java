package org.bygle.test.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.bygle.utils.BygleSystemUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class HttpTest {
	public String url = "";

	public HttpTest(String url) {
		this.url = url;
	}

	public void testGet(String resource, String host) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpMethod = new HttpGet(url + resource);
		httpMethod.setHeader("Accept", "application/rdf+xml");
		if (host != null && !host.trim().isEmpty())
			httpMethod.setHeader("Host", host);
		HttpResponse response = httpclient.execute(httpMethod);

		System.out.println(response.toString());

		String resultBody = EntityUtils.toString(response.getEntity());
		EntityUtils.consume(response.getEntity());
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		System.out.println(resultBody);
		httpclient.close();
	}

	public void testPostContainer(String resource, String slug, byte[] content, String host) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpMethod = new HttpPost(url + resource);
		httpMethod.setHeader("Accept", "text/turtle");
		httpMethod.setHeader("Content-Type", "text/turtle");
		if (host != null && !host.trim().isEmpty())
			httpMethod.setHeader("Host", host);
		httpMethod.setEntity(new ByteArrayEntity(content));
		HttpResponse response = httpclient.execute(httpMethod);
		System.out.println(response.toString());
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		String resultBody = EntityUtils.toString(response.getEntity());
		EntityUtils.consume(response.getEntity());
		System.out.println(resultBody);
		httpclient.close();
	}

	public void testPostResource(String resource, String slug, byte[] content, String host) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpMethod = new HttpPost(url + resource);
		httpMethod.setHeader("Accept", "text/turtle");
		httpMethod.setHeader("Slug", slug);
		httpMethod.setHeader("Content-Type", "text/turtle");
		if (host != null && !host.trim().isEmpty())
			httpMethod.setHeader("Host", host);
		httpMethod.setEntity(new ByteArrayEntity(content));

		HttpResponse response = httpclient.execute(httpMethod);
		System.out.println(response.toString());
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		String resultBody = EntityUtils.toString(response.getEntity());
		EntityUtils.consume(response.getEntity());
		System.out.println(resultBody);
		httpclient.close();
	}

	public void testPostImg(String resource, String slug, byte[] content, String host) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpMethod = new HttpPost(url + resource);
		httpMethod.setHeader("Slug", slug);
		httpMethod.setHeader("Content-Type", "image/png");
		if (host != null && !host.trim().isEmpty())
			httpMethod.setHeader("Host", host);
		httpMethod.setEntity(new ByteArrayEntity(content));
		HttpResponse response = httpclient.execute(httpMethod);
		System.out.println(response.toString());
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		String resultBody = EntityUtils.toString(response.getEntity());
		EntityUtils.consume(response.getEntity());
		System.out.println(resultBody);
		httpclient.close();
	}

	public void testDelete(String resource, String host) throws ClientProtocolException, IOException {
		System.out.println("deleting "+resource+" on "+host );
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpDelete httpMethod = new HttpDelete(url + resource);
		if (host != null && !host.trim().isEmpty())
			httpMethod.setHeader("Host", host);
		HttpResponse response = httpclient.execute(httpMethod);

		System.out.println(response.toString());
		EntityUtils.consume(response.getEntity());
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		httpclient.close();
	}

	public void testHead(String resource, String host) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpHead httpMethod = new HttpHead(url + resource);
		if (host != null && !host.trim().isEmpty())
			httpMethod.setHeader("Host", host);
		HttpResponse response = httpclient.execute(httpMethod);
		System.out.println(httpMethod);
		System.out.println(response.toString());
		EntityUtils.consume(response.getEntity());
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		httpclient.close();
	}

	public void testOptions(String resource, String host) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpOptions httpMethod = new HttpOptions(url + resource);
		if (host != null && !host.trim().isEmpty())
			httpMethod.setHeader("Host", host);
		System.out.println(httpMethod);
		HttpResponse response = httpclient.execute(httpMethod);

		System.out.println(response.toString());
		EntityUtils.consume(response.getEntity());
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		httpclient.close();

	}

	public void testPatch(String resource) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPatch httpMethod = new HttpPatch(url + resource);
		HttpResponse response = httpclient.execute(httpMethod);

		System.out.println(response.toString());

		String resultBody = EntityUtils.toString(response.getEntity());
		EntityUtils.consume(response.getEntity());
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		System.out.println(resultBody);
		httpclient.close();

	}

	public void testPut(String resource, String eTag, byte[] content, String host) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPut httpMethod = new HttpPut(url + resource);
		httpMethod.setHeader("If-Match", eTag);
		httpMethod.setHeader("Content-Type", "text/turtle");
		if (host != null && !host.trim().isEmpty())
			httpMethod.setHeader("Host", host);
		httpMethod.setEntity(new ByteArrayEntity(content));
		HttpResponse response = httpclient.execute(httpMethod);

		System.out.println(response.toString());
		EntityUtils.consume(response.getEntity());
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		httpclient.close();
	}

	public void testTrace(String resource) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpTrace httpMethod = new HttpTrace(url + resource);
		HttpResponse response = httpclient.execute(httpMethod);

		System.out.println(response.toString());

		String resultBody = EntityUtils.toString(response.getEntity());
		EntityUtils.consume(response.getEntity());
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		System.out.println(resultBody);
		httpclient.close();
	}

	public void testContains(String resource, String host, String location) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpMethod = new HttpGet(url + resource);
		httpMethod.setHeader("Accept", "text/turtle");
		if (host != null && !host.trim().isEmpty())
			httpMethod.setHeader("Host", host);
		HttpResponse response = httpclient.execute(httpMethod);

		System.out.println(response.toString());

		String resultBody = EntityUtils.toString(response.getEntity());
		EntityUtils.consume(response.getEntity());
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		System.out.println(resultBody);
		httpclient.close();
		String inputFormat = "text/turtle";
		AcceptList input = AcceptList.create(inputFormat.split(","));
		MediaType matchItemInput = AcceptList.match(BygleSystemUtils.offeringRDF, input);
		Lang lang = RDFLanguages.contentTypeToLang(matchItemInput.getContentType());
		Model containerModel = ModelFactory.createDefaultModel();
		containerModel.read(new ByteArrayInputStream(resultBody.getBytes()), url + resource, lang.getName());

		StmtIterator ite = containerModel.listStatements();
		while (ite.hasNext()) {
			Statement statement = ite.next();
			System.out.println(statement);
		}
		Resource container = containerModel.getResource(url + resource);
		System.out.println(container);
		Property contains = containerModel.createProperty("http://www.w3.org/ns/ldp#contains");
		System.out.println(contains);
		Resource res = containerModel.getResource(location);
		System.out.println(location);
		System.out.println(container.hasProperty(contains, res));

	}

	// http://www.w3.org/ns/ldp:Resource.

	public static void main(String[] args) {
		String url = "http://127.0.0.1:8080/bygle/";
		String host = "127.0.0.1:8080";
		String resource = "test/";
		try {
			HttpTest httpTest = new HttpTest(url);

			// httpTest.testContains(resource, null,
			// "http://http://127.0.0.1:8080/test/ldpRDFSource5/");

			// System.out.println("-------------------------------------------TEST ADD CONTAINER------------------------------------------------------------------");
			// String turtleContainer
			// ="@prefix dcterms: <http://purl.org/dc/terms/>.\n";
			// turtleContainer+="@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.\n";
			// turtleContainer+="@prefix ldp: <http://www.w3.org/ns/ldp#>.\n";
			// turtleContainer+="\n";
			// turtleContainer+="<> a ldp:BasicContainer, ldp:Container;\n";
			// turtleContainer+=" dcterms:title \"A very simple container by diego\" .\n";
			// httpTest.testPostContainer(resource,"",turtleContainer.getBytes(),null);
			// httpTest.testPostContainer(resource,"",turtleContainer.getBytes(),"example.org");
			//
			// System.out.println("-------------------------------------------TEST GET CONTAINER------------------------------------------------------------------");
			 httpTest.testGet(resource, host );

			// System.out.println("-------------------------------------------TEST GET CONTAINER------------------------------------------------------------------");
			// httpTest.testGet(resource,"example.org");

			// //
			//
			//
			// System.out.println("----------------------------------------------TEST ADD RESOURCE----------------------------------------------------------------");
			// String turtle ="@prefix dc: <http://purl.org/dc/terms/> .\n";
			// turtle+="@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n";
			// turtle+="\n";
			// turtle+="<> a foaf:PersonalProfileDocument;\n";
			// turtle+="    foaf:primaryTopic <#me> ;\n";
			// turtle+="    dc:title 'Sandro FOAF file' .\n";
			// turtle+="\n";
			// turtle+="<#me> a foaf:Person;\n";
			// turtle+="    foaf:name 'Sandro De Leo'  .";
			// httpTest.testPostResource(resource,"foaf",turtle.getBytes());
			//
			//
//			 for (int i = 0; i <100; i++) {
//			 System.out.println("----------------------------------------------TEST ADD GENERIC RESOURCE----------------------------------------------------------------");
//			 String turtleGeneric
//			 ="@prefix dc: <http://purl.org/dc/terms/> .\n";
//			 turtleGeneric+="@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n";
//			 turtleGeneric+="@prefix ods: <http://www.regesta.com/ods/> .\n";
//			 turtleGeneric+="\n";
//			 turtleGeneric+="<> a foaf:PersonalProfileDocument;\n";
//			 turtleGeneric+="    foaf:primaryTopic <#me> ;\n";
//			 turtleGeneric+=" 	ods:relations  <http://localhost:8080/bygle/alice/foaf/> ;\n";
//			 turtleGeneric+="    dc:title 'Sandro"+(i+1)+" FOAF"+(i+1)+" file"+(i+1)+"' .\n";
//			 turtleGeneric+="\n";
//			 turtleGeneric+="<#me> a foaf:Person;\n";
//			 turtleGeneric+="    foaf:name 'Sandro"+(i+1)+" De Leo"+(i+1)+"'  .";
//			 httpTest.testPostResource(resource,null,turtleGeneric.getBytes(),null);
//			 }

			 for (int i = 0; i <100; i++) {
			 System.out.println("----------------------------------------------TEST ADD GENERIC RESOURCE----------------------------------------------------------------");
			 String turtleGeneric
			 ="@prefix dc: <http://purl.org/dc/terms/> .\n";
			 turtleGeneric+="@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n";
			 turtleGeneric+="@prefix ods: <http://www.regesta.com/ods/> .\n";
			 turtleGeneric+="\n";
			 turtleGeneric+="<pippo"+i+"> a foaf:PersonalProfileDocument;\n";
			 turtleGeneric+="    foaf:primaryTopic <#me> ;\n";
			 turtleGeneric+=" 	ods:relations  <http://localhost:8080/bygle/alice/foaf/> ;\n";
			 turtleGeneric+="    dc:title 'Sandro"+(i+1)+" subFOAF"+(i+1)+" file"+(i+1)+"' .\n";
			 turtleGeneric+="\n";
			 turtleGeneric+="<#me> a foaf:Person;\n";
			 turtleGeneric+="    foaf:name 'Sandro"+(i+1)+" De Leo"+(i+1)+"'  .";
			// httpTest.testPostResource(resource+"foaf",null,turtleGeneric.getBytes(), "example.org");
			 }

			// System.out.println("-------------------------------------------TEST DELETE RESOURCE------------------------------------------------------------------");
			httpTest.testDelete("test/foaf", "example.org");
			//
			//
			//

			//

			// System.out.println("-------------------------------------------TEST DELETE GENERIC RESOURCE------------------------------------------------------------------");
			//
			// httpTest.testDelete("/test/46359e6f-3f3f-4624-b42b-267985a401b7/",null);
			//
			//
			// System.out.println("-------------------------------------------TEST GET CONTAINER------------------------------------------------------------------");
			// httpTest.testGet(resource);

			//
			// System.out.println("-------------------------------------------TEST DELETE IMG------------------------------------------------------------------");
			// httpTest.testDelete(resource+"foaf/avatar2");
			//
			// System.out.println("----------------------------------------------TEST ADD IMG----------------------------------------------------------------");
			// File file = new File("totti.jpg");
			// FileUtils.copyInputStreamToFile(HttpTest.class.getClassLoader().getResourceAsStream("totti.jpg"),
			// file);
			// httpTest.testPostImg(resource,"foaf/avatar2",FileUtils.readFileToByteArray(file));
			// httpTest.testGet(resource+"foaf/avatar2");
			//
			// System.out.println("-------------------------------------------TEST GET  RESOURCE------------------------------------------------------------------");
			// httpTest.testGet(resource+"foaf/");
			//

			// System.out.println("-------------------------------------------TEST HEAD CONTAINER------------------------------------------------------------------");
			// httpTest.testHead(resource);
			//
			// System.out.println("-------------------------------------------TEST OPTIONS CONTAINER------------------------------------------------------------------");
			// httpTest.testOptions(resource);
			//

			//
			// System.out.println("-------------------------------------------TEST UPDATE Resource------------------------------------------------------------------");
			// String turtleUpResource
			// ="@prefix dc: <http://purl.org/dc/terms/> .\n";
			// turtleUpResource+="@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n";
			// turtleUpResource+="\n";
			// turtleUpResource+="<> a foaf:PersonalProfileDocument;\n";
			// turtleUpResource+="    foaf:primaryTopic <#me> ;\n";
			// turtleUpResource+="    dc:title 'Alice FOAF file2' .\n";
			// turtleUpResource+="\n";
			// turtleUpResource+="<#me> a foaf:Person;\n";
			// turtleUpResource+="    foaf:name 'Alice Smith2'  .";
			// httpTest.testPut(resource+"foaf","b1b2eca6d9cf7df09900f1b03f7aafb3",turtleUpResource.getBytes());
			//
			// httpTest.testGet(resource+"foaf");

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
