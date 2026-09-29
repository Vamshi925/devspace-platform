package com.devspace.environment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.devspace.environment.dto.response.TemplateResponse;
import com.devspace.environment.exception.TemplateNotFoundException;
import com.devspace.environment.exception.TemplateServiceUnavailableException;

@Component
public class TemplateServiceClient {

    private final RestClient restClient;

    public TemplateServiceClient(
            @Value("${devspace.services.template.url}") String templateServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(templateServiceUrl)
                .build();
    }

    public TemplateResponse getTemplateById(String templateId) {

    try {

        return restClient.get()
                .uri("/api/templates/{templateId}", templateId)
                .retrieve()
                .body(TemplateResponse.class);

    } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {

        throw new TemplateNotFoundException(
                "Template not found with id: " + templateId
        );

    } catch (org.springframework.web.client.HttpServerErrorException ex) {

        throw new TemplateServiceUnavailableException(
                "Template Service is currently unavailable"
        );

    } catch (org.springframework.web.client.ResourceAccessException ex) {

        throw new TemplateServiceUnavailableException(
                "Unable to communicate with Template Service"
        );
    }
}

}