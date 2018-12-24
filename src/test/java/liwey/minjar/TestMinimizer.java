package liwey.minjar;

import junit.framework.TestCase;

import java.io.IOException;

public class TestMinimizer extends TestCase {

    public void testRestClient() throws IOException {
        Minimizer minimizer = new Minimizer();
        minimizer.shrink("d:/projects/github/RestClient/build/libs/v.out",
              "d:/projects/github/RestClient/build/libs/rest-min.jar");
    }

    public void testM3() throws IOException {
        Minimizer minimizer = new Minimizer();
        minimizer.shrink("d:/temp/m3.out","d:/temp/m3.jar");
    }

    public void testFeign() throws IOException {
        Minimizer minimizer = new Minimizer();
        minimizer.shrink("D:\\scratch\\llian\\workspaces\\spring\\spring-cloud\\feign\\build\\libs\\v.out",
              "D:\\scratch\\llian\\workspaces\\spring\\spring-cloud\\feign\\build\\libs\\feign-min.jar");
    }
}
