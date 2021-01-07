package lv.llu.science.bees.webapi.web.helpers

import org.springframework.mock.web.MockHttpServletRequest

import javax.servlet.*

class TestHeaderFilter implements Filter {

    @Override
    void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        maybeAddHeader(request)
        chain.doFilter(request, response)
    }

    private void maybeAddHeader(ServletRequest request) {
        def req = (MockHttpServletRequest) request

        if (req.getRequestURI() == '/token') {
            return
        }

        req.addHeader('Authorization', 'Bearer eyJ...dMQ')

        if (!(req.getRequestURI() ==~ '/(workspaces.*|data|logs|configs/device)')) {
            req.addHeader('ws', 'ws1')
        }
    }

    @Override
    void destroy() {}
}
