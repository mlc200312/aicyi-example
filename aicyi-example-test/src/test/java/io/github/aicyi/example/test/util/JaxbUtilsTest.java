package io.github.aicyi.example.test.util;

import io.github.aicyi.commons.util.JaxbUtils;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link JaxbUtils} 测试类
 */
@DisplayName("JaxbUtils XML工具类测试")
public class JaxbUtilsTest {

    @Getter
    @Setter
    @XmlRootElement(name = "user")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class User {
        @XmlElement
        private Long id;
        @XmlElement
        private String name;

        public User() {
        }

        public User(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Test
    @DisplayName("bean2Xml/xml2Bean 往返一致")
    public void testRoundTrip() throws JAXBException {
        User user = new User(1001L, "Tom");

        String xml = JaxbUtils.bean2Xml(user);
        assertNotNull(xml);
        assertTrue(xml.contains("<user>"));
        assertTrue(xml.contains("<name>Tom</name>"));

        User parsed = JaxbUtils.xml2Bean(xml, User.class);
        assertNotNull(parsed);
        assertEquals(1001L, parsed.getId());
        assertEquals("Tom", parsed.getName());
    }

    @Test
    @DisplayName("xml2Bean InputStream方式")
    public void testXml2BeanFromStream() throws JAXBException {
        String xml = JaxbUtils.bean2Xml(new User(2L, "Jack"));

        User parsed = JaxbUtils.xml2Bean(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), User.class);

        assertEquals("Jack", parsed.getName());
    }

    @Test
    @DisplayName("xmlPath2Bean 从文件路径读取")
    public void testXmlPath2Bean(@TempDir Path tempDir) throws Exception {
        String xml = JaxbUtils.bean2Xml(new User(3L, "File"));
        Path file = tempDir.resolve("user.xml");
        Files.write(file, xml.getBytes(StandardCharsets.UTF_8));

        User parsed = JaxbUtils.xmlPath2Bean(file.toString(), User.class);

        assertEquals(3L, parsed.getId());
        assertEquals("File", parsed.getName());
    }

    @Test
    @DisplayName("null/空白参数抛IllegalArgumentException")
    public void testNullArgument() {
        assertThrows(IllegalArgumentException.class, () -> JaxbUtils.bean2Xml(null));
        assertThrows(IllegalArgumentException.class, () -> JaxbUtils.xml2Bean((String) null, User.class));
        assertThrows(IllegalArgumentException.class, () -> JaxbUtils.xml2Bean("  ", User.class));
        assertThrows(IllegalArgumentException.class, () -> JaxbUtils.xmlPath2Bean(null, User.class));
        assertThrows(IllegalArgumentException.class, () -> JaxbUtils.xmlPath2Bean(" ", User.class));
        assertThrows(IllegalArgumentException.class, () -> JaxbUtils.xml2Bean((java.io.InputStream) null, User.class));
    }

    @Test
    @DisplayName("非法XML抛JAXBException")
    public void testInvalidXml() {
        assertThrows(JAXBException.class, () -> JaxbUtils.xml2Bean("not-xml", User.class));
    }

    @Test
    @DisplayName("重复转换走JAXBContext缓存")
    public void testContextCache() throws JAXBException {
        for (int i = 0; i < 3; i++) {
            String xml = JaxbUtils.bean2Xml(new User((long) i, "u" + i));
            User parsed = JaxbUtils.xml2Bean(xml, User.class);
            assertEquals("u" + i, parsed.getName());
        }
    }
}
