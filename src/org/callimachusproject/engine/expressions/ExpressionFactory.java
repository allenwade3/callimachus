package org.callimachusproject.engine.expressions;

import java.util.HashMap;
import java.util.Map;

import org.callimachusproject.engine.RDFParseException;
import org.callimachusproject.engine.model.AbsoluteTermFactory;

public class ExpressionFactory {
	private AbsoluteTermFactory tf = AbsoluteTermFactory.newInstance();
	private Map<String, String> namespaces = new HashMap<String, String>();

	public AbsoluteTermFactory getTermFactory() {
		return tf;
	}

	public void setTermFactory(AbsoluteTermFactory tf) {
		this.tf = tf;
	}

	public Map<String, String> getNamespaces() {
		return namespaces;
	}

	public void setNamespaces(Map<String, String> namespaces) {
		this.namespaces.clear();
		this.namespaces.putAll(namespaces);
	}

	public Expression parse(String text) throws RDFParseException {
		return new MarkupExpression(text, namespaces, tf);
	}

	public void setNamespace(String prefix, String value) {
		namespaces.put(prefix, value);
	}

}
