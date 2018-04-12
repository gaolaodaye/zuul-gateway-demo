package com.sinosoft.gateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.sinosoft.gateway.config.Constants.POST_TYPE;


@Service
public class SendResponseFilter extends ZuulFilter {

    private static final Log log = LogFactory.getLog(SendResponseFilter.class);
    private ThreadLocal<byte[]> buffers = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[8192];
        }
    };
    private String magic_tag_data = "<data>";


    @Override
    public String filterType() {
        return POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return 1000;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        try {
            log.info("func=run");
            writeResponse();
        } catch (Exception ex) {
            ReflectionUtils.rethrowRuntimeException(ex);
        }
        return null;
    }

    private void writeResponse() throws Exception {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletResponse servletResponse = context.getResponse();
        if (servletResponse.getCharacterEncoding() == null) { // only set if not set
            servletResponse.setCharacterEncoding("UTF-8");
        }
        OutputStream outStream = servletResponse.getOutputStream();
        InputStream is = null;
        try {
//			is = context.getResponseDataStream();
            String resultData = context.getResponse().toString();
            is = new ByteArrayInputStream(resultData.getBytes("UTF-8"));
            if (is != null) {
                writeResponse(is, outStream);
            }
        } finally {
            try {

                outStream.flush();
                // The container will close the stream for us
            } catch (IOException ex) {
                log.warn("Error while sending response to client: " + ex.getMessage());
            }
        }
    }

    private void writeResponse(InputStream zin, OutputStream out) throws Exception {
        byte[] bytes = buffers.get();
        int bytesRead = -1;
        while ((bytesRead = zin.read(bytes)) != -1) {
            out.write(bytes, 0, bytesRead);
        }
    }


}
