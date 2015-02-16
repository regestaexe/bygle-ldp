<%@page contentType="text/html; charset=UTF-8" language="java"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="org.bygle.utils.BygleSystemUtils"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>



<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>Bygle SPARQL Query Editor</title>
    <link rel="stylesheet" type="text/css" href="<c:url value="resources/css/screen.css"/>"></link>
    <script src="<c:url value="resources/js/jquery/jquery-2.1.1.min.js"/>" type="text/javascript"></script>
    <script src="<c:url value="resources/js/jquery/jquery.elastic.source.js"/>" type="text/javascript"></script>
	<script>
	    $(function(){
	    	var sparql ='PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#>\n';
		    	sparql +='PREFIX  dc:     <http://purl.org/dc/elements/1.1/>\n';
		    	sparql +='PREFIX  bygle:     <http://www.bygle.com/bygle/>\n';
		    	sparql +='PREFIX  :       <.>\n';
		    	sparql +='\n';
		    	sparql +='SELECT *\n';
		    	sparql +='\n';
		    	sparql +='    { ?s ?p ?o }\n';
		    	sparql +='\n';
		    	sparql +='LIMIT 100\n';
	    	$("#sparqlQuery").elastic();
	    	$("#sparqlQuery").val(sparql);
	    });
	    </script>
  </head>

  <body>
  		<form id="sparqlform" action="<c:url value="/sparql/query"/>" method="get">
		        <div style="float:left;width: 100%;padding:15px;">
					    <div>
					      <h1>Bygle SPARQL Query Editor</h1>
					    </div>
					    <div>
					      <h2>Default Data Set Name (Graph IRI)</h2>
					       <input type="text" size="100" value="<%=BygleSystemUtils.getStringProperty("endpoint.defaultDomain")%>" id="defaultGraphUri" name="defaultGraphUri"></input>
					    </div>
					    <div>
						      <h2>SPARQL:</h2>
						      <div>
							        <textarea name="sparqlQuery" id="sparqlQuery" style="height: 200px;"></textarea>
							        Results: <div id="time"></div>
							        <select id="outpuFormat" name="outpuFormat">
							        	  <option value="<%=BygleSystemUtils.OUTPUTFORMAT_HTML%>">HTML</option>
								          <!-- option value="<%=BygleSystemUtils.OUTPUTFORMAT_BIO%>">BIO</option-->
								          <option value="<%=BygleSystemUtils.OUTPUTFORMAT_CSV%>">CSV</option>
								          <option value="<%=BygleSystemUtils.OUTPUTFORMAT_JSON%>">JSON</option>
								          <option value="<%=BygleSystemUtils.OUTPUTFORMAT_RDF%>">RDF</option>
								          <option value="<%=BygleSystemUtils.OUTPUTFORMAT_RDF_ABBR%>">RDF-ABBR</option>
								          <option value="<%=BygleSystemUtils.OUTPUTFORMAT_TSV%>">TSV</option>
								          <option value="<%=BygleSystemUtils.OUTPUTFORMAT_XML%>">XML</option>
								          <option value="<%=BygleSystemUtils.OUTPUTFORMAT_N_TRIPLE%>">N-TRIPLES</option>
								          <option value="<%=BygleSystemUtils.OUTPUTFORMAT_TURTLE%>">TURTLE</option>
							        </select>
							        <input id="run" type="submit" value="Run query"></input>
							        <input id="reset" type="reset" value="Reset"></input>
						      </div>
					    </div>
			    </div>
	     </form>
  </body>
</html>