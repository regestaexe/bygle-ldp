<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:nfo="http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#" xmlns:isbd="http://iflastandards.info/ns/isbd/elements/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:bibo="http://purl.org/ontology/bibo/" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:xsd="http://www.w3.org/2001/XMLSchema#" xmlns:skos="http://www.w3.org/2008/05/skos#" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:bio="http://purl.org/vocab/bio/0.1/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:ocd="http://dati.intra.camera.it/ocd/" xmlns:ods="http://lod.xdams.org/ontologies/ods/" xml:base="http://dati.intra.camera.it/ocd/" xmlns:time="http://www.w3.org/2006/time#" xmlns:org="http://www.w3.org/ns/org#" xmlns="http://dati.intra.camera.it/ocd/">
	<xsl:output indent="yes" encoding="UTF-8" method="xml"></xsl:output>
	<xsl:strip-space elements="*" />
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="@*">
				<xsl:sort select="name()" />
			</xsl:apply-templates>
			<xsl:apply-templates select="node()">
				<xsl:sort select="name()" />
				<xsl:sort select="rdfs:label" data-type="text" />
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>