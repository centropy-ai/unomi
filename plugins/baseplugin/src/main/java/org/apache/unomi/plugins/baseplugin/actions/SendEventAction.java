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

import org.apache.unomi.api.Event;
import org.apache.unomi.api.Item;
import org.apache.unomi.api.Profile;
import org.apache.unomi.api.Session;
import org.apache.unomi.api.actions.Action;
import org.apache.unomi.api.actions.ActionExecutor;
import org.apache.unomi.api.services.EventService;
import org.apache.unomi.persistence.spi.PropertyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class SendEventAction implements ActionExecutor {

    private EventService eventService;
    private static final Logger logger = LoggerFactory.getLogger(SendEventAction.class.getName());

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public int execute(Action action, Event event) {
        String eventType = (String) action.getParameterValues().get("eventType");
        Object persistence = action.getParameterValues().get("persistence");
        boolean persistenceValue = PropertyHelper.getBooleanValue(persistence);

        @SuppressWarnings("unchecked")
        Map<String, Object> eventProperties = (Map<String, Object>) action.getParameterValues().get("eventProperties");
        Item target = (Item) action.getParameterValues().get("eventTarget");
        Event subEvent = new Event(eventType, event.getSession(), event.getProfile(), event.getScope(), event, target, eventProperties, event.getTimeStamp(), persistenceValue);
        subEvent.setProfileId(event.getProfileId());
        subEvent.getAttributes().putAll(event.getAttributes());

        int changes = eventService.send(subEvent);
        if ((changes & EventService.PROFILE_UPDATED) == EventService.PROFILE_UPDATED) {
            logger.info("Change by event {} on subEvent {} with profile {}", event.getEventType(), subEvent.getEventType(), String.join(", ", subEvent.getProfile().getSegments()));
            event.setProfile(subEvent.getProfile());
        }
        return changes;
    }
}
