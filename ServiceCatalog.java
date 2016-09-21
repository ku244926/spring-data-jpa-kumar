

package com.sei.service.catalog.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.rest.core.annotation.Description;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity
@Embeddable
@Table(name = "SERVICE_CATALOG")
@JsonPropertyOrder({ "SWP_DOMAIN_NAME", "SWP_BUSINESS_COMPONENT_NAME",
		"SWP_BUSINESS_SERVICE_NAME", "SWP_SUB_SERVICE_NAME" })
public class ServiceCatalog extends ResourceSupport implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "SWP_DOMAIN_NAME")
	private String swpDomainName; // SWP_DOMAIN_NAME

	@Column(name = "SWP_BUSINESS_COMPONENT_NAME")
	private String swpBusinessComponentName; // SWP_BUSINESS_COMPONENT_NAME

	@Column(name = "SWP_BUSINESS_SERVICE_NAME")
	private String swpBusinessServiceName; // SWP_BUSINESS_SERVICE_NAME

	@Column(name = "SWP_SUB_SERVICE_NAME")
	private String swpSubServiceName; // SWP_SUB_SERVICE_NAME

	@Description("Title for the TODO item")
	private String title;

	@Description("Details about the TODO item")
	private String description;

	@Description("Is it completed?")
	private boolean completed;

	public ServiceCatalog() {

	}

	public ServiceCatalog(String SWP_DOMAIN_NAME,
			String SWP_BUSINESS_COMPONENT_NAME,
			String SWP_BUSINESS_SERVICE_NAME, String SWP_SUB_SERVICE_NAME) {
		this.swpDomainName = SWP_DOMAIN_NAME;
		this.swpBusinessComponentName = SWP_BUSINESS_COMPONENT_NAME;
		this.swpBusinessServiceName = SWP_BUSINESS_SERVICE_NAME;
		this.swpSubServiceName = SWP_SUB_SERVICE_NAME;
	}

	public String getSwpDomainName() {
		return swpDomainName;
	}

	public void setSwpDomainName(String swpDomainName) {
		this.swpDomainName = swpDomainName;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
}
