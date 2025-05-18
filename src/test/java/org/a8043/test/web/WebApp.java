package org.a8043.test.web;

import org.a8043.simpleFramework.annotations.Autowired;
import org.a8043.simpleFramework.annotations.ConfigFile;
import org.a8043.simpleFramework.annotations.EnableAutowired;
import org.a8043.simpleFramework.annotations.RequestMapping;
import org.a8043.test.TestBean;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequestMapping("/")
@ConfigFile("webApp")
@EnableAutowired
public class WebApp extends HttpServlet {
    private static String hello;
    @Autowired
    private static TestBean testBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().println(hello);
    }
}
