package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/time")
public class ServletThymeleaf extends HttpServlet {
    private static final String COOKIE_NAME = "lastTimezone";
    private static final Logger logger = LoggerFactory.getLogger(ServletThymeleaf.class);
    private TemplateEngine engine;

    @Override
    public void init() {
        engine = new TemplateEngine();

        FileTemplateResolver templateResolver = new FileTemplateResolver();


        templateResolver.setPrefix("src/main/webapp/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCacheable(false);

        engine.addTemplateResolver(templateResolver);
    }



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html");

        String timezone = getTimezoneFromRequestOrCookie(req,resp);

        ZoneId zoneId = ZoneId.of(timezone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        ZonedDateTime time = ZonedDateTime.now(zoneId);

        String validTimezone = time.format(formatter).replace("GMT", "UTC");


        Map<String, Object> params = new LinkedHashMap<>();
        params.put("time", validTimezone);

        Context context = new Context(req.getLocale(),Map.of("time",params));


        try {

            engine.process("time", context, resp.getWriter());
        } catch (DateTimeException e) {
            System.err.println(e.getMessage());
        }
    }
    private String getTimezoneFromRequestOrCookie(HttpServletRequest req, HttpServletResponse resp){
        String timezone = req.getParameter("timezone");

        if (timezone != null){
            timezone = timezone.replace(" ","+");
            timezone = timezone.replace("UTC+","Etc/GMT-").replace("UTC-","Etc/GMT+");

            resp.addCookie(new Cookie(COOKIE_NAME,timezone));

            return timezone;
        }

        return getTimezoneFromCookie(req);
    }

    private String getTimezoneFromCookie(HttpServletRequest req) {
        String timezone = "";
        Cookie[] cookies = req.getCookies();

        if (cookies != null){
            for (Cookie c : cookies) {
                if (COOKIE_NAME.equals(c.getName())){
                    timezone = c.getValue();
                    break;
                }
            }
        }

        if (timezone != null){
            return timezone;
        } else {
            return "Etc/GMT";
        }

    }
}
