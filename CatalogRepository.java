

package com.sei.service.catalog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.sei.service.catalog.model.ServiceCatalog;

public interface CatalogRepository extends JpaRepository<ServiceCatalog,Long> {
	
	public List<ServiceCatalog> findAll();
	
	public List<ServiceCatalog> findServiceCatalogBySwpDomainNameIgnoringCase(String swpDomainName);
	
	@Modifying
	@Query("update ServiceCatalog s set s.swpBusinessComponentName = ?1 where s.swpDomainName = ?2")
    public int setFixedSwpBusinessComponentNameFor(String swpBusinessComponentName, String swpDomainName);
	
	@Modifying
	@Query(value = "INSERT INTO SERVICE_CATALOG  VALUES (?1, ?2, ?3 , ?4)", nativeQuery = true)
    public int insertServiceCatalog(String swpDomainName, String swpBusinessComponentName,String swpBusinessServiceName,String swpSubServiceName);
	
	@Modifying
    public Long deleteBySwpDomainName(String swpDomainName);
}
