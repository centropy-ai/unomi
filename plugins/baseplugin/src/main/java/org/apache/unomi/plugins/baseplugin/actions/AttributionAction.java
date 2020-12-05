/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.unomi.plugins.baseplugin.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.unomi.api.Event;
import org.apache.unomi.api.actions.Action;
import org.apache.unomi.api.actions.ActionExecutor;
import org.apache.unomi.api.services.EventService;
import org.apache.unomi.persistence.spi.PropertyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class AttributionAction implements ActionExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AttributionAction.class.getName());

    final String UTM_CHANNEL = "utm_channel_tactic=";
    final String UTM_CAMPAIGN = "utm_campaign=";
    final String UTM_MEDIUM = "utm_medium=";

    final String TOUCH_DIRECT = "direct";
    final String TOUCH_PAID = "paid";
    final String TOUCH_ORGANIC = "organic";

    public int execute(Action action, Event event) {
        if (event.getSession() == null) {
            return EventService.NO_CHANGE;
        }

        String touchType = "";

        String eventType = (String) action.getParameterValues().get("getEventTypeProperty");
        String referringValue = (String) action.getParameterValues().get("getReferringURLProperty");
        String destinationValue = (String) action.getParameterValues().get("getDestinationURLProperty");
        if (referringValue == null) {
            referringValue = "";
        }
        if (destinationValue == null) {
            destinationValue = "";
        }

        if (!destinationValue.contains(UTM_MEDIUM) && referringValue.length() == 0) {
            touchType = TOUCH_DIRECT;
        }
        if (destinationValue.contains(UTM_MEDIUM) && destinationValue.contains(UTM_CAMPAIGN) && destinationValue.contains(UTM_CHANNEL)) {
            touchType = TOUCH_PAID;
        }
        if (referringValue.length() > 0 && !destinationValue.contains(UTM_CHANNEL) && !destinationValue.contains(UTM_MEDIUM)) {
            touchType = TOUCH_ORGANIC;
        }
        String[] urlElement = StringUtils.split(destinationValue, "?");
        String campaign = "null";
        if (urlElement.length > 1) {
            campaign = Stream.of(urlElement[1].split("&"))
                    .map(kv -> kv.split("="))
                    .filter(kv -> "utm_campaign".equalsIgnoreCase(kv[0]))
                    .map(kv -> kv[1])
                    .findFirst()
                    .orElse("null");
            try {
                campaign = URLDecoder.decode(campaign, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException ignored) {
            }
        }
        if (touchType.length() > 0) {
            PropertyHelper.setProperty(event.getSession(), "properties.touch_type", touchType, "setIfMissing");
            PropertyHelper.setProperty(event.getSession(), "properties.touch_channel", referringValue, "setIfMissing");
            PropertyHelper.setProperty(event.getSession(), "properties.touch_campaign", campaign, "setIfMissing");
            PropertyHelper.setProperty(event.getSession(), "properties.touch_event", eventType, "setIfMissing");
            return EventService.SESSION_UPDATED;
        }

        return EventService.NO_CHANGE;
    }
}
