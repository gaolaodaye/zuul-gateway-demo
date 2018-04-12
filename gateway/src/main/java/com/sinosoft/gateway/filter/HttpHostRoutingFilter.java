package com.sinosoft.gateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.sinosoft.gateway.config.Constants;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by Gavin on 2018/2/11.
 */
@Service
public class HttpHostRoutingFilter extends ZuulFilter {
    private static final Logger log = LoggerFactory.getLogger(HttpHostRoutingFilter.class);

    private final Timer connectionManagerTimer = new Timer(
            "HttpHostRoutingFilter.connectionManagerTimer", true);

    private boolean sslHostnameValidationEnabled = false;
    private boolean forceOriginalQueryStringEncoding;

    private ProxyRequestHelper helper = new ProxyRequestHelper();
    private HttpClientConnectionManager connectionManager;
    private CloseableHttpClient httpClient = HttpClientBuilder.create().setConnectionManager(new PoolingHttpClientConnectionManager()).build();
    private boolean customHttpClient = true;



    @PreDestroy
    public void stop() {
        this.connectionManagerTimer.cancel();
    }

    @Override
    public String filterType() {
        return Constants.ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 500;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        return request.getRequestURI().toString().indexOf(Constants.TRANSMIT_URI_KEY) == 0;
    }

    @Override
    public Object run() {
        log.info("func=run");

        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();


        MultiValueMap<String, String> headers = this.helper
                .buildZuulRequestHeaders(request);
        MultiValueMap<String, String> params = this.helper
                .buildZuulRequestQueryParams(request);
        String verb = "get";
        InputStream requestEntity = getRequestBody(request);
        if (request.getContentLength() < 0) {
            //context.setChunkedRequestBody();
        }

        String uri = this.helper.buildZuulRequestURI(request);

        try {
            CloseableHttpResponse response = forward(this.httpClient, verb, uri, request,
                    headers, params, requestEntity);
            setResponse(response);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    protected HttpClientConnectionManager getConnectionManager() {
        return connectionManager;
    }


    private CloseableHttpResponse forward(CloseableHttpClient httpclient, String verb,
                                          String uri, HttpServletRequest request, MultiValueMap<String, String> headers,
                                          MultiValueMap<String, String> params, InputStream requestEntity)
            throws Exception {



        URL host = new URL("http://e.meishi.st.sankuai.com");
        HttpHost httpHost = getHttpHost(host);
        int contentLength = request.getContentLength();

        ContentType contentType = null;

        if (request.getContentType() != null) {
            contentType = ContentType.parse(request.getContentType());
        }

        InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength,
                contentType);

        HttpRequest httpRequest = buildHttpRequest(verb, uri, entity, headers, params,
                request);
        try {
            log.debug(httpHost.getHostName() + " " + httpHost.getPort() + " "
                    + httpHost.getSchemeName());
            CloseableHttpResponse zuulResponse = forwardRequest(httpclient, httpHost,
                    httpRequest);
            return zuulResponse;
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            // httpclient.getConnectionManager().shutdown();
        }
    }

    protected HttpRequest buildHttpRequest(String verb, String uri,
                                           InputStreamEntity entity, MultiValueMap<String, String> headers,
                                           MultiValueMap<String, String> params, HttpServletRequest request) {
        HttpRequest httpRequest;
        String uriWithQueryString = uri + (this.forceOriginalQueryStringEncoding
                ? getEncodedQueryString(request) : this.helper.getQueryString(params));

        switch (verb.toUpperCase()) {
            case "POST":
                HttpPost httpPost = new HttpPost(uriWithQueryString);
                httpRequest = httpPost;
                httpPost.setEntity(entity);
                break;
            case "PUT":
                HttpPut httpPut = new HttpPut(uriWithQueryString);
                httpRequest = httpPut;
                httpPut.setEntity(entity);
                break;
            case "PATCH":
                HttpPatch httpPatch = new HttpPatch(uriWithQueryString);
                httpRequest = httpPatch;
                httpPatch.setEntity(entity);
                break;
            case "DELETE":
                BasicHttpEntityEnclosingRequest entityRequest = new BasicHttpEntityEnclosingRequest(
                        verb, uriWithQueryString);
                httpRequest = entityRequest;
                entityRequest.setEntity(entity);
                break;
            default:
                httpRequest = new BasicHttpRequest(verb, uriWithQueryString);
                log.debug(uriWithQueryString);
        }

        httpRequest.setHeaders(convertHeaders(headers));
        return httpRequest;
    }

    private String getEncodedQueryString(HttpServletRequest request) {
        String query = request.getQueryString();
        return (query != null) ? "?" + query : "";
    }

    private MultiValueMap<String, String> revertHeaders(Header[] headers) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        for (Header header : headers) {
            String name = header.getName();
            if (!map.containsKey(name)) {
                map.put(name, new ArrayList<String>());
            }
            map.get(name).add(header.getValue());
        }
        return map;
    }

    private Header[] convertHeaders(MultiValueMap<String, String> headers) {
        List<Header> list = new ArrayList<>();
        for (String name : headers.keySet()) {
            for (String value : headers.get(name)) {
                list.add(new BasicHeader(name, value));
            }
        }
        return list.toArray(new BasicHeader[0]);
    }

    private CloseableHttpResponse forwardRequest(CloseableHttpClient httpclient,
                                                 HttpHost httpHost, HttpRequest httpRequest) throws IOException {
        return httpclient.execute(httpHost, httpRequest);
    }

    private HttpHost getHttpHost(URL host) {
        HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(), host.getProtocol());
        log.trace("create httpHost host={},port={},protocol={}", host.getHost(), host.getPort(), host.getProtocol());
        return httpHost;
    }

    private InputStream getRequestBody(HttpServletRequest request) {
        InputStream requestEntity = null;
        try {
            requestEntity = request.getInputStream();
        } catch (IOException ex) {
            // no requestBody is ok.
        }
        return requestEntity;
    }


    private void setResponse(HttpResponse response) throws IOException {
        RequestContext context = RequestContext.getCurrentContext();
        context.setResponse((HttpServletResponse) response);
        this.helper.setResponse(response.getStatusLine().getStatusCode(),
                response.getEntity() == null ? null : response.getEntity().getContent(),
                revertHeaders(response.getAllHeaders()));
    }


}
