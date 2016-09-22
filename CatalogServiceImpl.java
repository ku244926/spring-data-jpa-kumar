

package com.sei.service.catalog.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.sei.service.catalog.model.ServiceCatalog;
import com.sei.service.catalog.repository.CatalogRepository;

@Transactional
public class CatalogServiceImpl implements CatalogService{

	private  CatalogRepository serviceCatalogRepository;
	
	public CatalogRepository getServiceCatalogRepository() {
		
		return serviceCatalogRepository;
	}

	@Autowired
	public CatalogServiceImpl(CatalogRepository serviceCatalogRepository) {
		this.serviceCatalogRepository = serviceCatalogRepository;
	}
	
	@Override
	public List<ServiceCatalog> findCatalogs(CatalogSearchCriteria criteria) {
	
		Assert.notNull(criteria, "Criteria must not be null");
		
		String swpDomainName = criteria.getSwpDomainName();
		
		if (!StringUtils.hasLength(swpDomainName)) {
			System.out.println("swpdomain name passed is "+swpDomainName);
			return  this.serviceCatalogRepository.findServiceCatalogBySwpDomainNameIgnoringCase(swpDomainName);
		}
		return this.serviceCatalogRepository.findServiceCatalogBySwpDomainNameIgnoringCase(swpDomainName);
	}

	@Override
	public ServiceCatalog getServiceCatalog(String swpDomainName) {
		
		System.out.println("inside getServiceCatalog CatalogServiceImpl");
		return null;
	}

	@Override
	public List<ServiceCatalog>  updateServiceCatalog(CatalogSearchCriteria criteria) {
		
		int result = 0;
		
		Assert.notNull(criteria, "Criteria must not be null");
		String swpDomainName = criteria.getSwpDomainName();
		String swpBusinessComponentName = criteria.getSwpBusinessComponentName();
		System.out.println("swpdomain name passed is "+swpDomainName);
		result = this.serviceCatalogRepository.setFixedSwpBusinessComponentNameFor(swpBusinessComponentName, swpDomainName);
			return this.serviceCatalogRepository.findServiceCatalogBySwpDomainNameIgnoringCase(swpDomainName);
	}

	@Override
	public Long deleteServiceCatalog(CatalogSearchCriteria criteria) {
		
		System.out.println("inside insertServiceCatalog CatalogServiceImpl");
		Assert.notNull(criteria, "Criteria must not be null");
		String swpDomainName = criteria.getSwpDomainName();
		System.out.println("swpdomain name passed is "+swpDomainName);
			return this.serviceCatalogRepository.deleteBySwpDomainName(swpDomainName);
	}

	@Override
	public List<ServiceCatalog> insertServiceCatalog(CatalogSearchCriteria criteria) {
			
		int result = 0;
		
		System.out.println("inside insertServiceCatalog CatalogServiceImpl");
		Assert.notNull(criteria, "Criteria must not be null");
		String swpDomainName = criteria.getSwpDomainName();
		String swpBusinessComponentName = criteria.getSwpBusinessComponentName();
		String swpBusinessServiceName = criteria.getSwpBusinessServiceName();
		String swpSubServiceName = criteria.getSwpSubServiceName();
		System.out.println("swpdomain name passed is "+swpDomainName);
		result= this.serviceCatalogRepository.insertServiceCatalog(swpDomainName, swpBusinessComponentName, swpBusinessServiceName, swpSubServiceName);
			return this.serviceCatalogRepository.findServiceCatalogBySwpDomainNameIgnoringCase(swpDomainName);
	}
}
