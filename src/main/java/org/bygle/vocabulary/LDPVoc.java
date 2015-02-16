package org.bygle.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;


public class LDPVoc {
	public static final String NAMESPACE = "http://www.w3.org/ns/ldp#";
	public static final String PREFIX = "ldp";
	public static final URI BasicContainer;
	public static final URI Container;
	public static final URI contains;
	public static final URI DirectContainer;
	public static final URI hasMemberRelation;
	public static final URI IndirectContainer;
	public static final URI insertedContentRelation;
	public static final URI isMemberOfRelation;
	public static final URI member;
	public static final URI membershipResource;
	public static final URI MemberSubject;
	public static final URI NonRDFSource;
	public static final URI PreferContainment;
	public static final URI PreferEmptyContainer;
	public static final URI PreferMembership;
	public static final URI RDFSource;
	public static final URI Resource;
	public static final URI constrainedBy;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		BasicContainer = factory.createURI(LDPVoc.NAMESPACE, "BasicContainer");
		Container = factory.createURI(LDPVoc.NAMESPACE, "Container");
		contains = factory.createURI(LDPVoc.NAMESPACE, "contains");
		DirectContainer = factory.createURI(LDPVoc.NAMESPACE, "DirectContainer");
		hasMemberRelation = factory.createURI(LDPVoc.NAMESPACE, "hasMemberRelation");
		IndirectContainer = factory.createURI(LDPVoc.NAMESPACE, "IndirectContainer");
		insertedContentRelation = factory.createURI(LDPVoc.NAMESPACE, "insertedContentRelation");
		isMemberOfRelation = factory.createURI(LDPVoc.NAMESPACE, "isMemberOfRelation");
		member = factory.createURI(LDPVoc.NAMESPACE, "member");
		membershipResource = factory.createURI(LDPVoc.NAMESPACE, "membershipResource");
		MemberSubject = factory.createURI(LDPVoc.NAMESPACE, "MemberSubject");
		NonRDFSource = factory.createURI(LDPVoc.NAMESPACE, "NonRDFSource");
		PreferContainment = factory.createURI(LDPVoc.NAMESPACE, "PreferContainment");
		PreferEmptyContainer = factory.createURI(LDPVoc.NAMESPACE, "PreferEmptyContainer");
		PreferMembership = factory.createURI(LDPVoc.NAMESPACE, "PreferMembership");
		RDFSource = factory.createURI(LDPVoc.NAMESPACE, "RDFSource");
		Resource = factory.createURI(LDPVoc.NAMESPACE, "Resource");
		constrainedBy = factory.createURI(LDPVoc.NAMESPACE, "constrainedBy");
	}

	private LDPVoc() {
	}

}
