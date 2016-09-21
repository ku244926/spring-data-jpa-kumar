

package com.sei.service.catalog.service;

import java.io.Serializable;

public class CatalogSearchCriteria implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String swpDomainName;
	
	private String swpBusinessComponentName;
	
	private String swpBusinessServiceName;
	
	private String swpSubServiceName;
	
	public CatalogSearchCriteria(String swpDomainName) {
		this.swpDomainName = swpDomainName;
	}
	
	public CatalogSearchCriteria(String swpDomainName,
			String swpBusinessComponentName, String swpBusinessServiceName,
			String swpSubServiceName) {
		this.swpDomainName = swpDomainName;
		this.swpBusinessComponentName = swpBusinessComponentName;
		this.swpBusinessServiceName = swpBusinessServiceName;
		this.swpSubServiceName = swpSubServiceName;
	}
	public String getSwpBusinessComponentName() {
		return swpBusinessComponentName;
	}

	public void setSwpBusinessComponentName(String swpBusinessComponentName) {
		this.swpBusinessComponentName = swpBusinessComponentName;
	}

	public String getSwpBusinessServiceName() {
		return swpBusinessServiceName;
	}

	public void setSwpBusinessServiceName(String swpBusinessServiceName) {
		this.swpBusinessServiceName = swpBusinessServiceName;
	}

	public String getSwpSubServiceName() {
		return swpSubServiceName;
	}

	public void setSwpSubServiceName(String swpSubServiceName) {
		this.swpSubServiceName = swpSubServiceName;
	}

	public String getSwpDomainName() {
		return swpDomainName;
	}
	public void setSwpDomainName(String swpDomainName) {
		this.swpDomainName = swpDomainName;
	}	
}
