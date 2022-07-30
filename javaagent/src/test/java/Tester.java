import org.devopology.common.classloader.JarClassLoader;
import org.devopology.common.jar.Jar;
import org.devopology.common.logger.Logger;
import org.devopology.common.logger.LoggerFactory;
import org.junit.jupiter.api.Test;

import java.io.File;

public class Tester {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tester.class);

    @Test
    public void test() throws Exception {
        LOGGER.info("test()");

        Jar jar = new Jar();
        jar.load(new File("/home/dhoard/Development/github/devopology/metrics-exporter/temp/exporter.pkg"));

        ClassLoader parent = Thread.currentThread().getContextClassLoader();

        JarClassLoader jarClassLoader = new JarClassLoader(jar, parent);

        Class clazz = jarClassLoader.loadClass("io.undertow.Undertow");
        LOGGER.info(String.format("clazz = [%s]", clazz.getName()));
        LOGGER.info(String.format("classLoader = [%s]", clazz.getClass().getClassLoader()));

        clazz = jarClassLoader.loadClass("org.xnio._private.Messages_$logger_en_US");
        LOGGER.info(String.format("clazz = [%s]", clazz.getName()));
        LOGGER.info(String.format("classLoader = [%s]", clazz.getClass().getClassLoader()));
    }
}
