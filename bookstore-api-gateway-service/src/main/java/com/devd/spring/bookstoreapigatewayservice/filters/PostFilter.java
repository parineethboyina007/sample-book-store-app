package com.devd.spring.bookstoreapigatewayservice.filters;

import java.util.List;
import java.util.Locale;

import com.netflix.util.Pair;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * Removes downstream CORS headers so the API gateway remains the
 * single authority for browser-facing CORS responses.
 */
public class PostFilter extends ZuulFilter {

    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        List<Pair<String, String>> headers = context.getZuulResponseHeaders();

        if (headers != null) {
            headers.removeIf(header ->
                header.first() != null
                    && header.first().toLowerCase(Locale.ROOT)
                        .startsWith("access-control-")
            );
        }

        return null;
    }
}
