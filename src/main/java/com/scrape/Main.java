package com.scrape;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.scrape.helpers.Helper;

public class Main {
    public static void main(String[] args) {
        String url = "https://en.ephoto360.com/create-cute-girl-gamer-mascot-logo-online-687.html";

        String[] texts = new String[2];
        texts[0] = "Dzaky";
        try {
            String res = Ephoto360Scraper(url, texts);
            System.out.println(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String Ephoto360Scraper(String url, String[] texts) throws Exception {
        if (texts[1] == null) {
            texts[1] = "SeeDev";
        }

        String ua = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        try {
            // Membuat koneksi ke URL
            HttpGet get = new HttpGet(url);

            // set header user agent
            get.setHeader("User-Agent", ua);

            // Membuat cookie store untuk menyimpan cookie
            CookieStore cookieStore = new BasicCookieStore();

            // Membuat klien HTTP dengan cookie store
            HttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

            HttpResponse responseGet = client.execute(get);

            Document doc = Jsoup.parse(EntityUtils.toString(responseGet.getEntity()));

            // Memilih elemen HTML yang ingin diambil
            Element findToken = doc.select("input[name=token]").first();
            String tokenValue = findToken.attr("value");
            // System.out.println(tokenValue);

            Element findBuildServer = doc.select("input[name=build_server]").first();
            String buildServerValue = findBuildServer.attr("value");

            Element findBuildServerId = doc.select("input[name=build_server_id]").first();
            String buildServerIdValue = findBuildServerId.attr("value");

            // String[] radioSelectValues = new String[15];
            ArrayList<String> radioSelectValues = new ArrayList<>();
            Elements findRadioSelect = doc.select("input[name=radio0[radio]]");
            if (findRadioSelect != null && !findRadioSelect.isEmpty()) {
                for (Element radioSelect : findRadioSelect) {
                    String value = radioSelect.attr("value");
                    radioSelectValues.add(value);
                }
            }

            HttpEntity formBody;

            MultipartEntityBuilder formBodyBuilder = MultipartEntityBuilder
                    .create()
                    .addTextBody("submit", "GO")
                    .addTextBody("token", tokenValue)
                    .addTextBody("build_server", buildServerValue)
                    .addTextBody("build_server_id", buildServerIdValue);

            if (texts.length > 0) {
                for (int i = 0; i < texts.length; i++) {
                    formBodyBuilder.addTextBody("text[" + i + "]", texts[i]);
                }
            }
            String randomRadio = "";
            if (!radioSelectValues.isEmpty()) {
                randomRadio = Helper.randomString(radioSelectValues);
                formBodyBuilder.addTextBody("radio0[radio]", randomRadio);

            }

            // build formBodyBuilder
            formBody = formBodyBuilder.build();

            HttpPost post = new HttpPost(url);

            // set headers
            post.setHeader("User-Agent",
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            post.setHeader("Referer", url);
            post.setHeader("Origin", "https://en.ephoto360.com");

            // set body data
            post.setEntity(formBody);

            // execute request post data
            HttpResponse responsePost = client.execute(post);

            String responseBody = EntityUtils.toString(responsePost.getEntity());

            Document document = Jsoup.parse(responseBody);

            Element findFormValueInput = document.select("input[id=form_value_input]").first();

            if (findFormValueInput == null) {
                throw new Exception("Cannot create image ephoto360");
            }

            String formValueInputValue = findFormValueInput.attr("value");

            // System.out.println(formValueInputValue);

            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode jsonNode = objectMapper.readTree(formValueInputValue);

            if (jsonNode.has("radio0")) {
                randomRadio = jsonNode.get("radio0").get("radio").asText();
            }

            String id = jsonNode.get("id").asText();
            String token = jsonNode.get("token").asText();
            String buildServer = jsonNode.get("build_server").asText();
            String buildServerId = jsonNode.get("build_server_id").asText();

            HttpEntity bodyCreateImage;

            MultipartEntityBuilder bodyCreateImageBuilder = MultipartEntityBuilder
                    .create()
                    .addTextBody("id", id)
                    .addTextBody("token", token)
                    .addTextBody("radio0[radio]", randomRadio)
                    .addTextBody("build_server", buildServer)
                    .addTextBody("build_server_id", buildServerId);

            if (texts.length > 0) {
                for (int i = 0; i < texts.length; i++) {
                    bodyCreateImageBuilder.addTextBody("text[" + i + "]", texts[i]);
                }
            }

            // build bodyCreateImageBuilder
            bodyCreateImage = bodyCreateImageBuilder.build();

            HttpPost postCreateImage = new HttpPost("https://en.ephoto360.com/effect/create-image");

            // set headers
            postCreateImage.setHeader("User-Agent",
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            postCreateImage.setHeader("Referer", url);
            postCreateImage.setHeader("Origin", "https://en.ephoto360.com");

            // set body data
            postCreateImage.setEntity(bodyCreateImage);

            // execute request post data
            HttpResponse responsePostCreateImage = client.execute(postCreateImage);

            String responseBodyCreateImage = EntityUtils.toString(responsePostCreateImage.getEntity());

            // System.out.println(responseBodyCreateImage);
            JsonNode resJson = objectMapper.readTree(responseBodyCreateImage);

            // System.out.println(resJson);
            if (!resJson.get("success").asBoolean()) {
                throw new Exception("Cannot create image");
            }

            return responseBodyCreateImage;

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}