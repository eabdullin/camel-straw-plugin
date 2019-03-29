package com.camelstraw;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

import java.awt.Desktop;

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class CamelStrawUtil {
    public static String toJson(Object obj) {
        JsonBuilder builder = new JsonBuilder();
        builder.call(obj);
        return builder.toPrettyString();
    }

    private static final Logger log = LoggerFactory.getLogger(CamelStrawUtil.class);

    public static Object sendHttpFile(String urlString, String filename){
        try {
            URL url = new URL(urlString);
            String charset = "UTF-8";
            File textFile = new File(filename);
            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
            String CRLF = "\r\n"; // Line separator required by multipart/form-data.

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (
                    OutputStream output = connection.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
            ) {
                // Send text file.
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"formTestFile\"; filename=\"" + textFile.getName() + "\"").append(CRLF);
                writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
                writer.append(CRLF).flush();
                java.nio.file.Files.copy(textFile.toPath(), output);

                output.flush(); // Important before continuing with writer!
                writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

                // End of multipart/form-data.
                writer.append("--" + boundary + "--").append(CRLF).flush();
            }
            int status = connection.getResponseCode();
            if (status < 300) {
                JsonSlurper slurper = new JsonSlurper();
                InputStream inputStream = connection.getInputStream();
                Object response = slurper.parse(inputStream);
                inputStream.close();
                return response;
            }
            return null;
        }catch (Exception e){
            return null;
        }
    }
    public static Object sendHttpRequest(String urlString, String method, List<Map<String, Object>> data) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            JsonBuilder builder = new JsonBuilder();
            builder.call(data);
            con.setRequestProperty("Content-Type", "application/json");
            String jsonValue = builder.toPrettyString();
            log.info("sending metric: " + jsonValue);
            byte[] encodedData = jsonValue.getBytes(StandardCharsets.UTF_8);
            con.setDoOutput(true);
            OutputStream out = con.getOutputStream();
            out.write(encodedData);
            out.flush();
            out.close();
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);

            int status = con.getResponseCode();
            if (status < 300) {
                JsonSlurper slurper = new JsonSlurper();
                InputStream inputStream = con.getInputStream();
                Object response = slurper.parse(inputStream);
                inputStream.close();
                return response;
            }
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void openBrowser(String url) {
        String myOS = System.getProperty("os.name").toLowerCase();
        try {
            if (Desktop.isDesktopSupported()) { // Probably Windows
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(new URI(url));
            } else { // Definitely Non-windows
                Runtime runtime = Runtime.getRuntime();
                if (myOS.contains("mac")) { // Apples
                    log.debug(" -- Going on Apple with 'open'...");
                    runtime.exec("open " + url);
                } else if (myOS.contains("nix") || myOS.contains("nux")) { // Linux flavours
                    log.debug(" -- Going on Linux with 'xdg-open'...");
                    runtime.exec("xdg-open " + url);
                } else {
                    log.debug("unable/unwilling to launch a browser in your OS :( #SadFace");
                }

            }
        } catch (IOException | URISyntaxException eek) {
            log.debug("**Stuff wrongly: " + eek.getMessage());
        }
    }
}
