package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

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
                    Element dependency = (Element) node;

                    // Get dependency details
                    String groupId = getTagValue("groupId", dependency);
                    String artifactId = getTagValue("artifactId", dependency);
                    String version = getTagValue("version", dependency);

                    // Log dependency details
                    String gav = groupId + ":" + artifactId + ":" + version;
                    System.out.printf("Group ID: %s, Artifact ID: %s, Version: %s%n", groupId, artifactId, version);

                    System.out.println("Dependency: " + gav);
                    String cveApiUrl = "http://35.211.68.143:8086/api/cve/list/" + gav;
                    System.out.println("Fetching CVE-IDs from API: " + cveApiUrl);

                    // Fetch CVE-IDs from the API
                    JSONArray cveIDs = fetchCveIDs(cveApiUrl);
                    if (cveIDs != null && cveIDs.length() > 0) {
                        System.out.println("CVE-IDs for " + gav + ": " + cveIDs.toString());
                    } else {
                        System.out.println("No CVE-IDs found for " + gav);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to get the text value of an XML tag
    private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "N/A"; // Return "N/A" if the tag is missing
    }

    // Fetch CVE IDs from the provided API URL
    private static JSONArray fetchCveIDs(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) { // Success
                Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8);
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                // Parse the JSON response and extract "CVE-IDs"
                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("CVE-IDs")) {
                    return jsonResponse.getJSONArray("CVE-IDs");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if there's an error or no "CVE-IDs" found
    }
}
