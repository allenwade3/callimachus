PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX skos:<http://www.w3.org/2004/02/skos/core#>
PREFIX sd:<http://www.w3.org/ns/sparql-service-description#>
PREFIX void:<http://rdfs.org/ns/void#>
PREFIX foaf:<http://xmlns.com/foaf/0.1/>
PREFIX msg:<http://www.openrdf.org/rdf/2011/messaging#>
PREFIX calli:<http://callimachusproject.org/rdf/2009/framework#>
PREFIX prov:<http://www.w3.org/ns/prov#>
PREFIX audit:<http://www.openrdf.org/rdf/2012/auditing#>

INSERT {
</auth/> calli:hasComponent </auth/secrets/>.
</auth/secrets/> a <types/Folder>, calli:Folder;
    rdfs:label "secrets".
} WHERE {
	FILTER NOT EXISTS { </auth/secrets/> a calli:Folder }
};

INSERT {
</auth/groups/> calli:hasComponent </auth/groups/super>.
</auth/groups/super> a calli:Party, calli:Group, <types/Group>;
    rdfs:label "super";
    rdfs:comment "The user accounts in this group have heightened privileges to change or patch the system itself".
} WHERE {
	FILTER NOT EXISTS { </auth/groups/super> a calli:Group }
};

INSERT {
</auth/groups/> calli:hasComponent </auth/groups/power>.
</auth/groups/power> a calli:Party, calli:Group, <types/Group>;
    rdfs:label "power";
    rdfs:comment "Members of this group can access all data in the underlying data store";
    calli:subscriber </auth/groups/staff>;
    calli:administrator </auth/groups/admin>;
    calli:membersFrom ".".
} WHERE {
	FILTER NOT EXISTS { </auth/groups/power> a calli:Group }
};

INSERT {
    ?user a <types/DigestUser>
} WHERE {
    ?user a <types/User>
};

INSERT {
</auth/> calli:hasComponent </auth/invited-users/>.
</auth/invited-users/> a <types/Folder>, calli:Folder;
    rdfs:label "invited users";
    calli:subscriber </auth/groups/power>;
    calli:administrator </auth/groups/admin>.
} WHERE {
	FILTER NOT EXISTS { </auth/invited-users/> a calli:Folder }
};

INSERT {
    </sparql> calli:reader </auth/groups/power>;
} WHERE {
    </sparql> a <types/SparqlService>, sd:Service .
    FILTER NOT EXISTS { </sparql> calli:reader </auth/groups/power> }
};

INSERT {
	?file calli:reader </auth/groups/admin>
} WHERE {
	{
		<types/> calli:hasComponent? ?file . ?file calli:reader </auth/groups/system>
	} UNION {
		<pipelines/> calli:hasComponent? ?file . ?file calli:reader </auth/groups/system>
	} UNION {
		<queries/> calli:hasComponent? ?file . ?file calli:reader </auth/groups/system>
	} UNION {
		<schemas/> calli:hasComponent? ?file . ?file calli:reader </auth/groups/system>
	} UNION {
		<transforms/> calli:hasComponent? ?file . ?file calli:reader </auth/groups/system>
	}
	FILTER NOT EXISTS { ?file calli:reader </auth/groups/admin> }
};

INSERT {
    ?digest calli:authName ?authNameLit
} WHERE {
    ?digest a calli:DigestManager; calli:authName ?authName
    FILTER NOT EXISTS { ?digest calli:authName ?lit FILTER isLiteral(?lit) }
    FILTER isIRI(?authName)
    BIND (str(?authName) as ?authNameLit)
};

DELETE {
    ?digest calli:authName ?authName
} WHERE {
    ?digest a calli:DigestManager; calli:authName ?authName
    FILTER isIRI(?authName)
};

DELETE {
    GRAPH ?g1 { ?realm calli:authentication ?auth }
} WHERE {
    GRAPH ?g1 { ?realm calli:authentication ?auth }
    GRAPH ?g2 { ?realm calli:authentication ?auth }
    FILTER (str(?g1) < str(?g2))
};

INSERT {
<../> calli:hasComponent <../error.xpl> .
<../error.xpl> a <types/PURL>, calli:PURL ;
	rdfs:label "error.xpl";
	calli:alternate ?alternate;
	calli:administrator </auth/groups/super>;
	calli:reader </auth/groups/public> .
} WHERE {
    BIND (str(<pipelines/error.xpl>) as ?alternate)
	FILTER NOT EXISTS { <../error.xpl> a calli:PURL }
};

INSERT {
	?origin calli:error <../error.xpl>.
} WHERE {
    ?origin a <types/Origin>
	FILTER NOT EXISTS { ?origin calli:error ?error }
};

INSERT {
	?realm calli:error <../error.xpl>.
} WHERE {
    ?realm a <types/Realm>
	FILTER NOT EXISTS { ?realm calli:error ?error }
};

DELETE {
    </auth/groups/everyone> a calli:Group, <types/Group>.
    </auth/groups/system> a calli:Group, <types/Group>.
    </auth/groups/public> a calli:Group, <types/Group>.
} INSERT {
    </auth/groups/everyone> a calli:Domain, <types/Domain>.
    </auth/groups/system> a calli:Domain, <types/Domain>.
    </auth/groups/public> a calli:Domain, <types/Domain>.
} WHERE {
    </auth/groups/everyone> a calli:Group, <types/Group>.
    </auth/groups/system> a calli:Group, <types/Group>.
    </auth/groups/public> a calli:Group, <types/Group>.
};

DELETE {
    ?group calli:membersFrom ?from
} WHERE {
    ?group a <types/Group>; calli:membersFrom ?from
};

INSERT {
    ?realm calli:allowOrigin ?allowed
} WHERE {
    ?origin a <types/Origin>
    {
        ?realm a <types/Origin>
    } UNION {
        ?realm a <types/Realm>
    }
	BIND (replace(str(?origin), "/$", "") AS ?allowed)
	FILTER NOT EXISTS { ?realm calli:allowOrigin ?allowed }
};

INSERT {
</> calli:hasComponent </describe>.
</describe> a <types/DescribeService>, sd:Service;
    rdfs:label "describe";
    calli:reader </auth/groups/power>;
    calli:administrator </auth/groups/admin>.
} WHERE {
	FILTER NOT EXISTS { </> calli:hasComponent </describe> }
};

INSERT {
    ?dataset void:uriLookupEndpoint </describe?uri=>
} WHERE {
    $origin a <types/Origin>;
        calli:hasComponent/calli:hasComponent ?void.

    ?void a void:DatasetDescription;
        foaf:primaryTopic ?dataset.

    ?dataset a void:Dataset;
        void:sparqlEndpoint </sparql>.
    FILTER NOT EXISTS { ?dataset void:uriLookupEndpoint </describe?uri=> }
};

INSERT {
    <../> calli:hasComponent <../getting-started-with-callimachus> .
    <../getting-started-with-callimachus> a <types/PURL>, calli:PURL ;
	rdfs:label "getting-started-with-callimachus";
	calli:alternate <http://callimachusproject.org/docs/1.1/getting-started-with-callimachus.docbook?view>;
	calli:administrator </auth/groups/super>;
	calli:reader </auth/groups/public> .
} WHERE {
	FILTER NOT EXISTS { <../getting-started-with-callimachus> a calli:PURL }
};

INSERT {
    <../> calli:hasComponent <../callimachus-for-web-developers> .
    <../callimachus-for-web-developers> a <types/PURL>, calli:PURL ;
	rdfs:label "callimachus-for-web-developers";
	calli:alternate <http://callimachusproject.org/docs/1.1/callimachus-for-web-developers.docbook?view>;
	calli:administrator </auth/groups/super>;
	calli:reader </auth/groups/public> .
} WHERE {
	FILTER NOT EXISTS { <../callimachus-for-web-developers> a calli:PURL }
};

DELETE {
    <../getting-started-with-callimachus> calli:alternate <http://callimachusproject.org/docs/1.0/getting-started-with-callimachus.docbook?view>.
} INSERT {
    <../getting-started-with-callimachus> calli:alternate <http://callimachusproject.org/docs/1.1/getting-started-with-callimachus.docbook?view>.
} WHERE {
    <../getting-started-with-callimachus> calli:alternate <http://callimachusproject.org/docs/1.0/getting-started-with-callimachus.docbook?view>.
};

DELETE {
    <../callimachus-for-web-developers> calli:alternate <http://callimachusproject.org/docs/1.0/callimachus-for-web-developers.docbook?view>.
} INSERT {
    <../callimachus-for-web-developers> calli:alternate <http://callimachusproject.org/docs/1.1/callimachus-for-web-developers.docbook?view>.
} WHERE {
    <../callimachus-for-web-developers> calli:alternate <http://callimachusproject.org/docs/1.0/callimachus-for-web-developers.docbook?view>.
};

INSERT {
    ?digest rdfs:comment "Sign in with your email address and a site password"
} WHERE {
    ?digest a <types/DigestManager>
    FILTER NOT EXISTS { ?digest rdfs:comment ?comment }
};

INSERT {
    ?digest calli:authButton <images/digest_64.png>
} WHERE {
    ?digest a <types/DigestManager>
    FILTER NOT EXISTS { ?digest calli:authButton ?button }
};

INSERT {
    ?origin calli:subscriber </auth/groups/everyone>
} WHERE {
    ?origin a <types/Origin>
    FILTER NOT EXISTS { ?origin calli:subscriber </auth/groups/everyone> }
};

INSERT {
    ?auth calli:subscriber </auth/groups/everyone>
} WHERE {
    ?origin a <types/Origin>; calli:hasComponent ?auth .
    ?auth calli:hasComponent ?manager .
    ?manager a calli:AuthenticationManager .
    FILTER NOT EXISTS { ?auth calli:subscriber </auth/groups/everyone> }
};

INSERT {
    ?manager calli:subscriber </auth/groups/everyone>
} WHERE {
    ?origin a <types/Origin>; calli:hasComponent ?auth .
    ?auth calli:hasComponent ?manager .
    ?manager a calli:AuthenticationManager .
    FILTER NOT EXISTS { ?manager calli:subscriber </auth/groups/everyone> }
};

INSERT {
	<queries/folder-create-menu.rq> calli:reader </auth/groups/everyone>
} WHERE {
	FILTER NOT EXISTS { <queries/folder-create-menu.rq> calli:reader </auth/groups/everyone> }
};

DELETE {
	</callimachus/ontology> owl:versionInfo "1.0.1"
} INSERT {
	</callimachus/ontology> owl:versionInfo "1.1"
} WHERE {
	</callimachus/ontology> owl:versionInfo "1.0.1"
};

