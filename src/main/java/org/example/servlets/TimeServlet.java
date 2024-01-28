package org.example.servlets;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private static final String COOKIE_LAST_TIMEZONE = "lastTimezone";
    private static final int COOKIE_AGE = 30;
    private static final String REAL_PATH_TO_TEMPLATES = "/WEB-INF/templates/";
    private static final String DATA_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String UTC = "UTC";
    private TemplateEngine engine;

    @Override
    public void init() throws ServletException {
        super.init();
        engine = new TemplateEngine();
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(getServletContext().getRealPath(REAL_PATH_TO_TEMPLATES));
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=utf-8");
            Cookie cookie = new Cookie(COOKIE_LAST_TIMEZONE, getQuery(req));
            cookie.setMaxAge(COOKIE_AGE);
            resp.addCookie(cookie);
            String currentTime = getResponse(req);
            Context context = new Context();
            context.setVariable("current_time", currentTime);
            context.setVariable("time_format", getQuery(req));
            engine.process("time", context, resp.getWriter());
            resp.addCookie(new Cookie(COOKIE_LAST_TIMEZONE, getQuery(req)));
            resp.getWriter().close();
    }
    private String getResponse(HttpServletRequest req) {
        return  LocalDateTime.now(ZoneId.of(getQuery(req)))
                .format(DateTimeFormatter
                .ofPattern(DATA_TIME_FORMAT));
    }

    private String getQuery(HttpServletRequest req) {
        Optional<String> optionalQuery = Optional.ofNullable(req.getParameter("timezone"));
        return optionalQuery.orElseGet(() -> getCookies(req).getOrDefault(COOKIE_LAST_TIMEZONE,UTC));
    }
    private Map<String, String> getCookies(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (Cookie cookie : cookies) {
            result.put(cookie.getName(), cookie.getValue());
        }
        return result;
    }

    @Override
    public void destroy() {
        engine = null;
        super.destroy();
    }
}
