

package com.sei.service.catalog.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sei.service.catalog.model.ServiceCatalog;
//import com.sei.service.catalog.repository.ServiceCatalogDao;
import com.sei.service.catalog.service.CatalogSearchCriteria;
import com.sei.service.catalog.service.CatalogService;
import com.sei.service.catalog.utils.CustomLink;
import com.sei.service.catalog.utils.ServiceCatalogConstants;

@RestController
@RequestMapping("/servicecatalog")
class ServiceCatalogController {

	@Autowired
    public CatalogService catalogService;

	@ResponseBody
    @RequestMapping(value = "/", method = RequestMethod.POST, headers = {"Accept=application/json"}, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public  ResponseEntity<?> addServiceCatalogs(@RequestBody String inputString) throws JsonParseException, JsonMappingException, IOException {
		
        ServiceCatalog serviceCatalog = null;
        
        ServiceCatalog inputServiceCatalog = null;
        
        List<ServiceCatalog> serviceCatalogList = new ArrayList<ServiceCatalog>();
        
        ObjectMapper mapper = new ObjectMapper();
        
        HttpHeaders httpHeaders = new HttpHeaders();
        
        inputServiceCatalog = mapper.readValue(inputString, ServiceCatalog.class);
        CatalogSearchCriteria catalogSearchCriteria = new CatalogSearchCriteria(inputServiceCatalog.getSwpDomainName(),inputServiceCatalog.getSwpBusinessComponentName(),inputServiceCatalog.getSwpBusinessServiceName(),inputServiceCatalog.getSwpSubServiceName());
        serviceCatalogList = this.catalogService.insertServiceCatalog(catalogSearchCriteria);
        serviceCatalog = serviceCatalogList.get(0);
        System.out.println("Processing for http headers");
        if(serviceCatalog.getLinks().size()< ServiceCatalogConstants.NUMBER_OF_LINKS){
			 System.out.println("size of link is "+serviceCatalog.getLinks().size());
			 CustomLink link = new CustomLink(linkTo(ServiceCatalogController.class).slash(serviceCatalog.getSwpDomainName()).withSelfRel(),"GET");
			 CustomLink linkPostandPut =  new CustomLink(linkTo(ServiceCatalogController.class).withRel("updateServiceCatalog"),"POST and PUT");
			 CustomLink linkDelete = new CustomLink(linkTo(ServiceCatalogController.class).withRel("deleteServiceCatalog"), "DELETE");
			 serviceCatalog.add(linkPostandPut);
			 serviceCatalog.add(link);
			 serviceCatalog.add(linkDelete);
		 }
        return new ResponseEntity<ServiceCatalog>(serviceCatalog, httpHeaders, HttpStatus.CREATED);
    }
    
    @RequestMapping(value = "/", method = RequestMethod.PUT, headers = "Accept=application/json", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateServiceCatalogs(@RequestBody String inputString) throws JsonParseException, JsonMappingException, IOException {
    	
        List<ServiceCatalog> serviceCatalogList = new ArrayList<ServiceCatalog>();
        
        int flag = 0;
        
        ServiceCatalog serviceCatalog = null;
        
        ServiceCatalog inputServiceCatalog = null;
        
        ObjectMapper mapper = new ObjectMapper();
        
        HttpHeaders httpHeaders = new HttpHeaders();
        
        inputServiceCatalog = mapper.readValue(inputString, ServiceCatalog.class);
        CatalogSearchCriteria catalogSearchCriteria = new CatalogSearchCriteria(inputServiceCatalog.getSwpDomainName(),inputServiceCatalog.getSwpBusinessComponentName(),inputServiceCatalog.getSwpBusinessServiceName(),inputServiceCatalog.getSwpSubServiceName());
        System.out.println("fields paased for update are"+catalogSearchCriteria.getSwpSubServiceName());
        serviceCatalogList = this.catalogService.updateServiceCatalog(catalogSearchCriteria);
        if(serviceCatalogList.size()>0){
        	serviceCatalog = serviceCatalogList.get(0);	

			 if(serviceCatalog.getLinks().size()< ServiceCatalogConstants.NUMBER_OF_LINKS){
				 System.out.println("size of link is "+serviceCatalog.getLinks().size());
				 CustomLink link = new CustomLink(linkTo(ServiceCatalogController.class).slash(serviceCatalog.getSwpDomainName()).withSelfRel(),"GET");
				 CustomLink linkPostandPut =  new CustomLink(linkTo(ServiceCatalogController.class).withRel("updateServiceCatalog"),"POST and PUT");
				 CustomLink linkDelete = new CustomLink(linkTo(ServiceCatalogController.class).withRel("deleteServiceCatalog"), "DELETE");
				 serviceCatalog.add(linkPostandPut);
				 serviceCatalog.add(link);
				 serviceCatalog.add(linkDelete);
			 }
		 
             flag = checkStatus(serviceCatalogList, inputServiceCatalog);
        }
        System.out.println("httpHeaders-->" + httpHeaders); 
        if (flag == 1){
        return new ResponseEntity<>(serviceCatalog, httpHeaders, HttpStatus.CREATED);
        }else{
        	return new ResponseEntity<>(serviceCatalog, httpHeaders, HttpStatus.NOT_MODIFIED);
        }  
    }

	public int checkStatus(List<ServiceCatalog> serviceCatalogList,ServiceCatalog inputServiceCatalog) {
		
		int flag = 0;
		
		for(ServiceCatalog sce:serviceCatalogList){
        	if (sce.getSwpBusinessComponentName().equalsIgnoreCase(inputServiceCatalog.getSwpBusinessComponentName())){
        		flag = 1;
        	}else{
        		flag = 0;
        	}
        }
		return flag;
	}
    
    @RequestMapping(value = "/", method = RequestMethod.DELETE, headers = "Accept=application/json", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteServiceCatalogs(@RequestBody String inputString) throws JsonParseException, JsonMappingException, IOException {
       
        Long result = 0L;
        
        ServiceCatalog inputServiceCatalog = null;
        
        ObjectMapper mapper = new ObjectMapper();
        
        HttpHeaders httpHeaders = new HttpHeaders();
        
        inputServiceCatalog = mapper.readValue(inputString, ServiceCatalog.class);
        CatalogSearchCriteria catalogSearchCriteria = new CatalogSearchCriteria(inputServiceCatalog.getSwpDomainName(),inputServiceCatalog.getSwpBusinessComponentName(),inputServiceCatalog.getSwpBusinessServiceName(),inputServiceCatalog.getSwpSubServiceName());
        result = this.catalogService.deleteServiceCatalog(catalogSearchCriteria);
        return new ResponseEntity<>("", httpHeaders, HttpStatus.CREATED); 
    }

    @RequestMapping(value = "/{swpDomainName}", method = RequestMethod.GET, headers = "Accept=application/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?>  readServiceCatalogsBySwpDomainName(@PathVariable String swpDomainName) {
    	
        List<ServiceCatalog> serviceCatalogList = new ArrayList<ServiceCatalog>();
        List<ServiceCatalog> serviceCatalogListtemp = new ArrayList<ServiceCatalog>();
        serviceCatalogListtemp.clear();
        serviceCatalogList.clear();
        CatalogSearchCriteria catalogSearchCriteria = new CatalogSearchCriteria(swpDomainName);
        HttpHeaders httpHeaders = new HttpHeaders();
        
        try {
        	serviceCatalogList =  (ArrayList<ServiceCatalog>) this.catalogService.findCatalogs(catalogSearchCriteria);
        	 
        	System.out.println("size is "+serviceCatalogList.size());
        	
        	 for (ServiceCatalog sc :serviceCatalogList){
        		 if(sc.getSwpDomainName().equalsIgnoreCase(swpDomainName)){
        			 if(sc.getLinks().size()< ServiceCatalogConstants.NUMBER_OF_LINKS){
        				 CustomLink link = new CustomLink(linkTo(ServiceCatalogController.class).slash(sc.getSwpDomainName()).withSelfRel(),"GET");
        				 CustomLink linkPostandPut =  new CustomLink(linkTo(ServiceCatalogController.class).withRel("updateServiceCatalog"),"POST and PUT");
        				 CustomLink linkDelete = new CustomLink(linkTo(ServiceCatalogController.class).withRel("deleteServiceCatalog"), "DELETE");
                		 sc.add(linkPostandPut);
                		 sc.add(link);
                		 sc.add(linkDelete);
        			 }
        		 }
        		 serviceCatalogListtemp.add(sc);
        	 }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
         return new ResponseEntity<>(serviceCatalogListtemp, httpHeaders, HttpStatus.CREATED);
    }
}
