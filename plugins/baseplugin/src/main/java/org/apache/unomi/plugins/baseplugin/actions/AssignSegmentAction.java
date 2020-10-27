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
import org.apache.unomi.api.Profile;
import org.apache.unomi.api.actions.Action;
import org.apache.unomi.api.actions.ActionExecutor;
import org.apache.unomi.api.services.EventService;
import org.apache.unomi.persistence.spi.PropertyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class AssignSegmentAction implements ActionExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AssignSegmentAction.class.getName());

    private EventService eventService;

    private boolean useEventToUpdateProfile = false;

    public void setUseEventToUpdateProfile(boolean useEventToUpdateProfile) {
        this.useEventToUpdateProfile = useEventToUpdateProfile;
    }

    public int execute(Action action, Event event) {
        boolean storeInSession = Boolean.TRUE.equals(action.getParameterValues().get("storeInSession"));
        if (storeInSession && event.getSession() == null) {
            return EventService.NO_CHANGE;
        }

        String segmentID = (String) action.getParameterValues().get("id");
        if (!event.getProfile().getSegments().contains(segmentID)) {
            event.getProfile().getSegments().add(segmentID);
            return EventService.PROFILE_UPDATED;
        }
        return EventService.NO_CHANGE;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

}
