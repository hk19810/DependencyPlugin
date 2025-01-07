package org.example;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class DependencyExtractor {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java DependencyExtractor <pom.xml path>");
            return;
        }

        String pomFilePath = args[0];

        try {
            File file = new File(pomFilePath);
            if (!file.exists()) {
                System.out.println("Error: The specified pom.xml file does not exist!");
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList dependencyList = doc.getElementsByTagName("dependency");

            System.out.println("Dependencies found:");
            for (int i = 0; i < dependencyList.getLength(); i++) {
                Node node = dependencyList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    String groupId = element.getElementsByTagName("groupId").item(0).getTextContent();
                    String artifactId = element.getElementsByTagName("artifactId").item(0).getTextContent();
                    String version = element.getElementsByTagName("version").item(0).getTextContent();

                    System.out.printf("Group ID: %s, Artifact ID: %s, Version: %s%n", groupId, artifactId, version);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
