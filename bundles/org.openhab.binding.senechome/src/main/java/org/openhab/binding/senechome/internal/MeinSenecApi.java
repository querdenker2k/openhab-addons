/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.senechome.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.senechome.internal.json.MeinSenecResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link MeinSenecApi} class configures https client and performs status requests
 *
 * @author Korbinian Probst - Initial contribution
 *
 */
@NonNullByDefault
public class MeinSenecApi {
    private final Logger logger = LoggerFactory.getLogger(MeinSenecApi.class);
    private static final String BASE_URL = "https://app-gateway-prod.senecops.com/v1/senec";
    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private boolean isEnabled = false;
    private String username = "";
    private String password = "";
    private String token = "";
    private String deviceId = "";

    public MeinSenecApi(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Return if the mein-senec.de api can be accessed
     * 
     * @return true = enabled, false = disabled
     */
    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void loginMeinSenec(String username, String password, String deviceId) {
        // if username or password are empty, mein-senec.de stays disabled
        if (username == null || username.isEmpty() || !username.contains("@")) {
            logger.debug("No username given, mein-senec.de stays disabled");
            return;
        }
        if (password == null || password.isEmpty()) {
            logger.debug("No password given, mein-senec.de stays disabled");
            return;
        }

        this.username = username;
        this.password = password;

        getLoginToken();
        verifyDevice(deviceId);

        // if the token could not be received or the device position not clarified, mein-senec.de stays disabled
        if ((token.isEmpty()) || (this.deviceId.isEmpty())) {
            logger.debug("Problems with token or deviceId, mein-senec.de stays disabled");
        } else {
            isEnabled = true;
        }
    }

    /**
     * If no login token is set, login to mein-senec.de to generate and store the token for future api calls
     */
    private void getLoginToken() {
        try {
            String loginUrl = BASE_URL + "/login";
            Request request = httpClient.newRequest(loginUrl);

            // Set the HTTP request method to POST
            request.method(HttpMethod.POST);

            // Create the payload data as a JSON string
            Map<String, String> payloadData = new HashMap<>();
            payloadData.put("username", username);
            payloadData.put("password", password);
            String payloadJson = gson.toJson(payloadData);

            // Set the request content
            request.content(new StringContentProvider(payloadJson), "application/json");

            // Send the request
            ContentResponse response = request.send();

            if (response.getStatus() == 200) {
                // Parse the JSON response to extract the access token
                Map<String, Object> responseMap = gson.fromJson(response.getContentAsString(), Map.class);
                // Code below looks strange because of the build error
                // Null type mismatch: required 'java.lang.@NonNull String' but the provided value is inferred as
                // @org.eclipse.jdt.annotation.Nullable
                String local_token = (responseMap.get("token") instanceof String) ? (String) responseMap.get("token")
                        : "";

                if (local_token != null) {
                    logger.debug("Login successful!");
                    token = local_token;
                } else {
                    logger.error("Login failed. Token not found in response.");
                    token = "";
                }
            } else {
                logger.debug("Login failed with status code: {}", response.getStatus());
            }
        } catch (Exception e) {
            logger.error("An error occurred during login: {}", e.getMessage());
        }
    }

    /**
     * Get all devices from mein-senec.de. Either only one device exists or the device with the given deviceId will be
     * taken for fetching
     * 
     * @param deviceId optional
     */
    private void verifyDevice(String deviceId) {
        List<Map<String, Object>> devicesData = null;

        try {
            String devicesUrl = BASE_URL + "/anlagen";
            Request request = httpClient.newRequest(devicesUrl);

            // Set the HTTP request method to GET
            request.method(HttpMethod.GET);

            // Set the authorization header
            request.header("Authorization", token);

            // Send the request
            ContentResponse response = request.send();

            if (response.getStatus() == 200) {
                // Parse the JSON response to extract the list of devices
                devicesData = gson.fromJson(response.getContentAsString(), List.class);

                logger.debug("Devices Data:");
                logger.debug("{}", gson.toJson(devicesData));

                if (devicesData.size() == 0) {
                    // No device, nothing to do
                }
                if (devicesData.size() == 1) {
                    // With only one device just take it
                    Map<String, Object> device = devicesData.get(0);
                    String id = (String) device.get("id");
                    if (id != null) {
                        this.deviceId = id;
                    }
                } else if (deviceId.isEmpty()) {
                    // if no device id is given but the size of the array is > 1, print all IDs
                    logger.warn("There are {} devices configured in mein-senec.de, but no device id was configured.",
                            devicesData.size());
                    for (int i = 0; i < devicesData.size(); i++) {
                        Map<String, Object> device = devicesData.get(i);
                        String id = (String) device.get("id");
                        String steuereinheitnummer = (String) device.get("steuereinheitnummer");
                        String gehaeusenummer = (String) device.get("gehaeusenummer");
                        String strasse = (String) device.get("strasse");
                        String hausnummer = (String) device.get("hausnummer");
                        String postleitzahl = (String) device.get("postleitzahl");
                        String ort = (String) device.get("ort");
                        String zeitzone = (String) device.get("zeitzone");
                        String systemType = (String) device.get("systemType");
                        logger.warn(
                                "Id: {}, control device id: {}, housing id: {} address: {} {}, {} {}, timezone: {}, system type: {}",
                                id, steuereinheitnummer, gehaeusenummer, strasse, hausnummer, postleitzahl, ort,
                                zeitzone, systemType);
                    }
                } else {
                    // If size > 1 and a device id was given, try to identify the position in the array
                    for (int i = 0; i < devicesData.size(); i++) {
                        Map<String, Object> device = devicesData.get(i);
                        String id = (String) device.get("id");
                        if (deviceId.equals(id)) {
                            // Code below looks strange because of build error
                            // Null type mismatch: required 'java.lang.@NonNull String' but the provided value is
                            // inferred as @org.eclipse.jdt.annotation.Nullable
                            String local_id = (id instanceof String) ? (String) id : "";
                            this.deviceId = local_id;
                            break; // Exit the loop once the device is found
                        }
                    }
                }

                // last verify that the device was found
                if (this.deviceId.length() > 0) {
                    logger.debug("Device ID {} found and verified", this.deviceId);
                } else {
                    logger.debug("Device ID not found in the devicesData array.");
                }
            } else {
                logger.debug("Failed to retrieve devices with status code: {}", response.getStatus());
                logger.debug("Request URL: {}", devicesUrl);

                // Iterate over the headers
                for (HttpField field : request.getHeaders()) {
                    String headerName = field.getName();
                    String headerValue = field.getValue();

                    logger.debug("Header Name: {}", headerName);
                    logger.debug("Header Value: {}", headerValue);
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred while retrieving devices: {}", e.getMessage());
        }
    }

    public MeinSenecResponse getDashboard() {
        try {
            HttpClient httpClient = new HttpClient(new SslContextFactory.Client());
            httpClient.start();

            String dashboardUrl = BASE_URL + "/anlagen/" + deviceId + "/dashboard";
            Request request = httpClient.newRequest(dashboardUrl);

            // Set the HTTP request method to GET
            request.method(HttpMethod.GET);

            // Set the authorization header
            request.header("Authorization", token);

            // Send the request
            ContentResponse response = request.send();

            if (response.getStatus() == 200) {
                // Parse the JSON response to extract dashboard data
                // Map<String, Object> dashboardData = gson.fromJson(response.getContentAsString(), Map.class);

                // logger.debug("Dashboard Data:");
                // logger.debug("{}", gson.toJson(dashboardData, Map.class));

                // // Convert the dashboardData map to a JSON string
                // String dashboardJson = gson.toJson(dashboardData, Map.class);

                // Deserialize the JSON string into a MeinSenecResponse object
                // return Objects.requireNonNull(gson.fromJson(dashboardJson, MeinSenecResponse.class));
                return Objects.requireNonNull(gson.fromJson(response.getContentAsString(), MeinSenecResponse.class));
            } else {
                logger.debug("Failed to retrieve dashboard data with status code: {}", response.getStatus());
                logger.debug("Request URL: {}", dashboardUrl);

                // Iterate over the headers
                for (HttpField field : request.getHeaders()) {
                    String headerName = field.getName();
                    String headerValue = field.getValue();

                    logger.debug("Header Name: {}", headerName);
                    logger.debug("Header Value: {}", headerValue);
                }
            }

        } catch (Exception e) {
            logger.error("An error occurred while retrieving dashboard data: {}", e.getMessage());
        }
        // TODO what to return if everything breaks?
        return new MeinSenecResponse();
    }
}
