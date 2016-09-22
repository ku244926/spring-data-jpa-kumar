

package com.sei.service.catalog.test;

import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.sei.service.catalog.model.ServiceCatalog;

public class ServiceCatalogTest {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String booksUrl = "http://localhost:8080/service-catalogue-poc-impl/servicecatalog/TSkumar";

    @Test
    public void testCreateBookWithAuthor() throws Exception {
     
        final URI bookUri = new URI(booksUrl);
     
        Resource<ServiceCatalog> book = getBook(bookUri);
        assertNotNull(book);
    }

    private Resource<ServiceCatalog> getBook(URI uri) {
    	
        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(acceptableMediaTypes);
        HttpEntity<String> entity = new HttpEntity<String>(headers); 
        return restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<Resource<ServiceCatalog>>() {	
        }).getBody();   
    }
}
