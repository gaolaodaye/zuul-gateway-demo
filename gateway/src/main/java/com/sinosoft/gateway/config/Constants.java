package com.sinosoft.gateway.config;

import com.netflix.zuul.ZuulFilter;

/**
 * 常量
 * Created by Gavin on 2018/2/7.
 */
public interface Constants {

    //初始化 api-->功能链关系
    //初始化 api-->后端API关系
    //初始化 api-->参数映射关系，验证关系
    //初始化 api-->结果关系

    //缓存KEY
    String CACHE_ROUTE_HTTP_APPKEY = "cache_route_http_%s";
    String CACHE_ROUTE_HTTP_ID = "cache_route_http_id_%s";
    String CACHE_API_APPKEY = "cache_API_%s";
    String CACHE_API_FUNCTIONS_APPKEY = "cache_api_functions_%s_%s";
    String CACHE_API_TARGETAPI_APPKEY = "cache_api_targetapi_%s_%s";
    String CACHE_API_PARAMMAPPING_APPKEY = "cache_api_parammapping_%s_%s";
    String CACHE_API_RESULT_APPKEY = "cache_api_result_%s_%s";
    String CACHE_CLIENT_MTHRIFT_INTERFACE = "cache_client_%s";
    String CACHE_API_ID = "cache_api_id_";
    String CACHE_APPLICATION_APPKEY = "cache_application_%s";
    String CACHE_FUNCTION_ID = "cache_function_%s";

    //标识服务
    String PROTOCOL = "protocol";
    String REQUEST = "request";
    String RESPONSE = "response";
    String API_INFO_ID = "api_info_id";
    String TARGET_PARAM_VALUE = "target_param_value";

    // Zuul Filter TYPE constants -----------------------------------

    /**
     * CACHE_APPLICATION_APPKEY
     * {@link ZuulFilter#filterType()} error type.
     */
    String ERROR_TYPE = "error";

    /**
     * {@link ZuulFilter#filterType()} post type.
     */
    String POST_TYPE = "post";

    /**
     * {@link ZuulFilter#filterType()} pre type.
     */
    String PRE_TYPE = "pre";

    /**
     * {@link ZuulFilter#filterType()} route type.
     */
    String ROUTE_TYPE = "route";

    //字段
    String DEFAULT_ERROR = "{\n" +
            "    \"error\" : {\n" +
            "        \"code\" : %s, \n" +
            "        \"type\" : \"error\",  \n" +
            "        \"message\" : %s\n" +
            "    }\n" +
            "}";
    String TRANSMIT_URI_KEY = "/route/transmit/";


    String SYS_PARA_X_MGW_APPKEY = "sys_para_x_mgw_appkey";
    String SYS_PARA_X_MGW_REQID = "sys_para_x_mgw_reqid";
    String SYS_PARA_X_MGW_SRCIP = "sys_para_x_mgw_srcip";
    String SYS_PARA_DATE = "sys_para_date";
    String SYS_PARA_AUTHORIZATION = "sys_para_authorization";
    String SYS_PARA_X_MGW_TOKEN = "sys_para_x_mgw_token";
    String SYS_PARA_X_MGW_TARGET_URL = "sys_para_x_mgw_target_url";
    String SYS_PARA_X_MGW_TARGET_METHOD = "sys_para_x_mgw_target_method";

}
