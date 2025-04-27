package com.kcxutilities.integration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.utils.authtoken.service.CPPTokenService;
//import com.utils.model.CPPAuthTokenModel;
import de.hybris.platform.integrationservices.enums.IntegrationRequestStatus;
import de.hybris.platform.integrationservices.service.MediaPersistenceService;
import de.hybris.platform.outboundservices.model.OutboundRequestMediaModel;
import de.hybris.platform.outboundservices.model.OutboundRequestModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.*;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;

public class SCPIRestClientHelper {

    private static final String MONITORING_ENABLED_KEY = "outboundservices.monitoring.enabled";
    private static final String PAYLOAD_RETENTION_SUCCESS_KEY = "outboundservices.monitoring.success.payload.retention";
    private static final String PAYLOAD_RETENTION_ERROR_KEY = "outboundservices.monitoring.error.payload.retention";

    private static final Logger LOG = Logger.getLogger(SCPIRestClientHelper.class);

    private static final String TOKEN_API_URL = "scpi.ouath.token.url";
    private static final String TOKEN_API_USERNAME = "scpi.ouath.username";
    private static final String TOKEN_API_PASSWORD = "scpi.ouath.password";
    private static final String ACCESS_TOKEN_KEY = "access_token";

    private static final String MAX_TOTAL = "connection.mgr.maxTotal";
    private static final String MAX_PER_ROUTE = "connection.mgr.maxPerRoute";
    private static final String VALIDATE_AFTER_ACTIVITY = "connection.mgr.validateAfterActivity";
    private static final String CONNECTION_REQUEST_TIMEOUT = "requestConfig.connectionRequestTimeout";
    private static final String SOCKET_TIMEOUT = "requestConfig.socketTimeout";
    private static final String CONNECT_TIMEOUT = "requestConfig.connectionTimeout";

    private final ObjectMapper mapper = new ObjectMapper();

    @Resource(name = "scpiRestTemplate")
    private RestTemplate restTemplate;

    @Resource(name = "connectionManager")
    private PoolingHttpClientConnectionManager connectionManager;

    @Resource(name = "modelService")
    private ModelService modelService;

    @Resource(name = "integrationServicesMediaPersistenceService")
    private MediaPersistenceService mediaPersistenceService;

//    @Resource(name = "cppTokenService")
//    private CPPTokenService cppTokenService;

    @Resource(name = "configurationService")
    private ConfigurationService configurationService;

//    public String getSCPITokenFromTable() {
//        CPPAuthTokenModel tokenModel = cppTokenService.getAuthToken("CPI");
//        if (tokenModel != null && StringUtils.isNotEmpty(tokenModel.getToekn())) {
//            return tokenModel.getToekn();
//        }
//        return getSCPIToken();
//    }

    public String getSCPIToken() {
        HttpHeaders requestHeader = new HttpHeaders();
        requestHeader.setBasicAuth(getSCPIUserName(), getSCPIPassword());
        requestHeader.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestHeader);

        try {
            ResponseEntity<String> responseEntity = getRestTemplateWithCustomRequestFactory().exchange(
                    getSCPITokenUrl(), HttpMethod.POST, requestEntity, String.class);
            if (HttpStatus.OK == responseEntity.getStatusCode() && StringUtils.isNotEmpty(responseEntity.getBody())) {
                return getTokenFromJsonStr(responseEntity.getBody());
            }
        } catch (Exception e) {
            String errorMessage = String.format("Remote System with the url [{%s}] is not reachable", getSCPITokenUrl());
            LOG.error(errorMessage, e);
            throw new RuntimeException(e);
        }
        return StringUtils.EMPTY;
    }

    public <T> T requestCPIWithPayload(Object request, String url, HttpMethod method, MediaType mediaType, Class<T> response) {
        try {
            ResponseEntity<String> responseEntity = sendRequest(request, url, method, mediaType);
            Set<HttpStatus> desiredStatusCodes = Set.of(HttpStatus.OK, HttpStatus.CREATED, HttpStatus.ACCEPTED);
            if (desiredStatusCodes.contains(responseEntity.getStatusCode()) && StringUtils.isNotBlank(responseEntity.getBody())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("JSON Response Body = %s", responseEntity.getBody()));
                }
                return mapper.readValue(responseEntity.getBody(), response);
            }
        } catch (Exception e) {
            LOG.error("Error while sending request to CPI or converting response", e);
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean requestEntityResponseCPIWithPayload(Object request, String url, HttpMethod method, MediaType mediaType) {
        try {
            ResponseEntity<String> responseEntity = sendRequest(request, url, method, mediaType);
            LOG.info("Response = " + responseEntity.getBody());
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            String errorMessage = String.format("Remote System with the url [{%s}] is not reachable", url);
            LOG.error(errorMessage, e);
            throw new RuntimeException(e);
        }
    }

    protected ResponseEntity<String> sendRequest(Object request, String url, HttpMethod method, MediaType mediaType) {
        HttpHeaders requestHeader = new HttpHeaders();
//        requestHeader.setBearerAuth(getSCPITokenFromTable());
        requestHeader.setContentType(mediaType);

        String requestBody;
        try {
            requestBody = getMapper().writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, requestHeader);
        ResponseEntity<String> responseEntity = null;
        String errorMessage = null;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("JSON URL = %s , JSON Request Body = %s", url, requestBody));
        }

        try {
            responseEntity = getRestTemplateWithCustomRequestFactory().exchange(url, method, requestEntity, String.class);
            return responseEntity;
        } catch (Exception e) {
            errorMessage = String.format("Remote System with the url [{%s}] is not reachable", url);
            LOG.error(errorMessage, e);
            throw new RuntimeException(e);
        } finally {
            boolean monitoringEnabled = configurationService.getConfiguration().getBoolean(MONITORING_ENABLED_KEY, false);
            if (monitoringEnabled && (method == HttpMethod.POST || method == HttpMethod.PATCH || method == HttpMethod.PUT)) {
                HttpStatus status = (responseEntity != null) ? responseEntity.getStatusCode() : HttpStatus.BAD_REQUEST;
                OutboundRequestModel outboundRequestModel = saveOutboundRequest(url, "REST_REQUEST", status, "REST_REQUEST", errorMessage);

                boolean isSuccess = responseEntity != null && status.is2xxSuccessful();
                boolean retainPayload = configurationService.getConfiguration()
                        .getBoolean(isSuccess ? PAYLOAD_RETENTION_SUCCESS_KEY : PAYLOAD_RETENTION_ERROR_KEY, false);

                if (retainPayload) {
                    updateOutboundRequestWithPayload(
                            outboundRequestModel,
                            StringUtils.defaultIfEmpty(requestBody, "").getBytes(),
                            (responseEntity != null && StringUtils.isNotEmpty(responseEntity.getBody())) ?
                                    responseEntity.getBody().getBytes() : StringUtils.EMPTY.getBytes());
                }
            }
        }
    }

    public OutboundRequestModel saveOutboundRequest(String endpointUrl, String integrationKey,
                                                    HttpStatus status, String outboundIntegrationObject, String errorMessage) {
        String messageId = UUID.randomUUID().toString();
        OutboundRequestModel outboundRequestModel = modelService.create(OutboundRequestModel.class);
        outboundRequestModel.setMessageId(messageId);
        outboundRequestModel.setDestination(endpointUrl);
        outboundRequestModel.setIntegrationKey(integrationKey);
        outboundRequestModel.setError(errorMessage);
        outboundRequestModel.setStatus(status.is2xxSuccessful() ? IntegrationRequestStatus.SUCCESS : IntegrationRequestStatus.ERROR);
        outboundRequestModel.setType(outboundIntegrationObject);
        modelService.save(outboundRequestModel);
        return outboundRequestModel;
    }

    public void updateOutboundRequestWithPayload(OutboundRequestModel outboundRequestModel, byte[] requestBody, byte[] responseBody) {
        outboundRequestModel.setPayload(getPayload(requestBody));
//        outboundRequestModel.setResponsePayload(getPayload(responseBody));
        modelService.save(outboundRequestModel);
    }

    protected OutboundRequestMediaModel getPayload(byte[] payload) {
        List<OutboundRequestMediaModel> list = mediaPersistenceService
                .persistMedias(Collections.singletonList(new ByteArrayInputStream(payload)), OutboundRequestMediaModel.class);
        if (list == null || list.isEmpty()) {
            LOG.warn("No payload was returned. The monitoring response cannot be updated without this item.");
            return null;
        }
        return list.get(0);
    }

    protected String getTokenFromJsonStr(String jsonStr) throws IOException {
        Map<String, String> map = mapper.readValue(jsonStr, new TypeReference<>() {});
        return map.get(ACCESS_TOKEN_KEY);
    }

    protected RestTemplate getRestTemplateWithCustomRequestFactory() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(configurationService.getConfiguration().getInt(CONNECTION_REQUEST_TIMEOUT, 5000))
                .setSocketTimeout(configurationService.getConfiguration().getInt(SOCKET_TIMEOUT, 5000))
                .setConnectTimeout(configurationService.getConfiguration().getInt(CONNECT_TIMEOUT, 5000))
                .build();

        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        RestTemplate template = getRestTemplate();
        HttpComponentsClientHttpRequestWithBodyFactory reqFactory = new HttpComponentsClientHttpRequestWithBodyFactory();
        reqFactory.setHttpClient(httpClient);
        template.setRequestFactory(reqFactory);
        return template;
    }

    private static final class HttpComponentsClientHttpRequestWithBodyFactory extends HttpComponentsClientHttpRequestFactory {
        @Override
        protected HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
            if (httpMethod == HttpMethod.GET) {
                return new HttpGetRequestWithEntity(uri);
            }
            return super.createHttpUriRequest(httpMethod, uri);
        }
    }

    private static final class HttpGetRequestWithEntity extends HttpEntityEnclosingRequestBase {
        public HttpGetRequestWithEntity(final URI uri) {
            super.setURI(uri);
        }

        @Override
        public String getMethod() {
            return HttpMethod.GET.name();
        }
    }

    protected RestTemplate getRestTemplate() {
        return restTemplate;
    }

    protected URI getSCPITokenUrl() {
        return URI.create(Config.getString(TOKEN_API_URL, ""));
    }

    protected String getSCPIUserName() {
        return Config.getString(TOKEN_API_USERNAME, "");
    }

    protected String getSCPIPassword() {
        return Config.getString(TOKEN_API_PASSWORD, "");
    }

    protected ObjectMapper getMapper() {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
}
