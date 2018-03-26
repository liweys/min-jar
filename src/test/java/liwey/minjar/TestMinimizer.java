package liwey.minjar;

import junit.framework.TestCase;

import java.io.IOException;

public class TestMinimizer extends TestCase {

    public void testRestClient() throws IOException {
        Minimizer minimizer = new Minimizer();
        minimizer.shrink("d:/projects/github/RestClient/build/libs/v.out",
              "d:/projects/github/RestClient/build/libs/rest-min.jar");
    }

    public void test() throws IOException {
        Minimizer minimizer = new Minimizer();
        minimizer.shrink("d:/temp/m3.out","d:/temp/m3.jar");
    }
}
