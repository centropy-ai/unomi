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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.unomi.api.*;
import org.apache.unomi.api.actions.Action;
import org.apache.unomi.api.actions.ActionExecutor;
import org.apache.unomi.api.actions.ActionPostExecutor;
import org.apache.unomi.api.conditions.Condition;
import org.apache.unomi.api.services.*;
import org.apache.unomi.persistence.spi.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class DetermineProfileOnPropertiesAction implements ActionExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MergeProfilesOnPropertyAction.class.getName());

    private PersistenceService persistenceService;
    private EventService eventService;
    private DefinitionsService definitionsService;
    private ConfigSharingService configSharingService;
    String[] profileMergeFields = {"email", "phone", "cellphone", "device_id", "id"};

    public int execute(Action action, Event event) {
        String profileIdCookieName = (String) configSharingService.getProperty("profileIdCookieName");
        String profileIdCookieDomain = (String) configSharingService.getProperty("profileIdCookieDomain");
        Integer profileIdCookieMaxAgeInSeconds = (Integer) configSharingService.getProperty("profileIdCookieMaxAgeInSeconds");

        CustomItem target = (CustomItem) event.getTarget();
        Profile profile = event.getProfile();
        if (profile instanceof Persona || profile.isAnonymousProfile()) {
            return EventService.NO_CHANGE;
        }

        final Session currentSession = event.getSession();

        // store the profile id in case the merge change it to a previous one
        String profileId = profile.getItemId();
        Profile previousProfile = event.getProfile();
        List<Condition> subConditions = new ArrayList<>();
        for (Map.Entry<String, Object> entry : target.getProperties().entrySet()) {
            if (!ArrayUtils.contains(profileMergeFields, entry.getKey())) {
                continue;
            }
            Condition propertyCondition = new Condition(definitionsService.getConditionType("profilePropertyCondition"));
            propertyCondition.setParameter("comparisonOperator", "equals");
            propertyCondition.setParameter("propertyName", "properties." + entry.getKey());
            propertyCondition.setParameter("propertyValue", entry.getValue());
            subConditions.add(propertyCondition);
        }


        Condition c = new Condition(definitionsService.getConditionType("booleanCondition"));
        c.setParameter("operator", "or");
        c.setParameter("subConditions", subConditions);

        final List<Profile> profiles = persistenceService.query(c, "properties.firstVisit", Profile.class);
        if (profiles.size() > 0) {
            // Take existing profile
            profile = profiles.get(0);
        } else {
            return EventService.NO_CHANGE;
        }

        HttpServletResponse httpServletResponse = (HttpServletResponse) event.getAttributes().get(Event.HTTP_RESPONSE_ATTRIBUTE);
        sendProfileCookie(profile, httpServletResponse, profileIdCookieName, profileIdCookieDomain, profileIdCookieMaxAgeInSeconds);

        // At the end of the merge, we must set the merged profile as profile event to process other Actions
        event.setProfileId(profile.getItemId());
        event.setProfile(profile);

        currentSession.setProfile(profile);

        eventService.send(new Event("mergedProfile", currentSession, profile, event.getScope(), profile, previousProfile, event.getTimeStamp()));

        return EventService.PROFILE_UPDATED + EventService.SESSION_UPDATED;
    }

    private static void sendProfileCookie(Profile profile, ServletResponse response, String profileIdCookieName, String profileIdCookieDomain, int cookieAgeInSeconds) {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            if (!(profile instanceof Persona)) {
                Cookie profileIdCookie = new Cookie(profileIdCookieName, profile.getItemId());
                profileIdCookie.setPath("/");
                if (profileIdCookieDomain != null && !profileIdCookieDomain.equals("")) {
                    profileIdCookie.setDomain(profileIdCookieDomain);
                }
                profileIdCookie.setMaxAge(cookieAgeInSeconds);
                httpServletResponse.addCookie(profileIdCookie);
            }
        }
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void setDefinitionsService(DefinitionsService definitionsService) {
        this.definitionsService = definitionsService;
    }

    public void setConfigSharingService(ConfigSharingService configSharingService) {
        this.configSharingService = configSharingService;
    }

}
