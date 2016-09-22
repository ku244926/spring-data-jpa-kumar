

package com.sei.service.catalog.service;

import java.util.List;

import com.sei.service.catalog.model.ServiceCatalog;

public interface CatalogService {

    public List<ServiceCatalog> findCatalogs(CatalogSearchCriteria criteria);
	
    public ServiceCatalog getServiceCatalog(String swpDomainName);
	
    public List<ServiceCatalog>  updateServiceCatalog(CatalogSearchCriteria criteria);
	
    public List<ServiceCatalog>  insertServiceCatalog(CatalogSearchCriteria criteria);
	
    public Long  deleteServiceCatalog(CatalogSearchCriteria criteria);
}
