

package com.sei.service.catalog.utils;

import javax.xml.bind.annotation.XmlAttribute;

import org.springframework.hateoas.Link;

public class CustomLink extends Link {

	private static final long serialVersionUID = 1L;

	@XmlAttribute
	private String method;

	public CustomLink(Link link, String method) {
		super(link.getHref(), link.getRel());
	    this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
}
