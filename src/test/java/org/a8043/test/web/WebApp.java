package org.a8043.test.web;

import org.a8043.simpleFramework.annotations.ConfigFile;
import org.a8043.simpleFramework.annotations.RequestMapping;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequestMapping("/")
@ConfigFile("webApp")
public class WebApp extends HttpServlet {
    private static String hello;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().println(hello);
    }
}
