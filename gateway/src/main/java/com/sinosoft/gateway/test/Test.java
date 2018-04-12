package com.sinosoft.gateway.test;

import org.apache.http.HttpHost;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by gaofeng22 on 2018/4/12
 */
public class Test {


    public static void main(String[] args) throws URISyntaxException, MalformedURLException {
        URL uri = new URL("https://www.qiandu.com:8080/goods/index.html?username=dgh&passwd=123#j2se");


        HttpHost httpHost = new HttpHost(uri.getHost(), uri.getPort(),uri.getProtocol());
    }
}
