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
import org.apache.unomi.api.actions.Action;
import org.apache.unomi.api.actions.ActionExecutor;
import org.apache.unomi.api.conditions.Condition;
import org.apache.unomi.api.services.DefinitionsService;
import org.apache.unomi.api.services.EventService;
import org.apache.unomi.persistence.spi.PersistenceService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

public class SetEventOccurenceCountAction implements ActionExecutor {
    private DefinitionsService definitionsService;

    private PersistenceService persistenceService;

    public void setDefinitionsService(DefinitionsService definitionsService) {
        this.definitionsService = definitionsService;
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Override
    public int execute(Action action, Event event) {
        final Condition pastEventCondition = (Condition) action.getParameterValues().get("pastEventCondition");

        Condition andCondition = new Condition(definitionsService.getConditionType("booleanCondition"));
        andCondition.setParameter("operator", "and");
        ArrayList<Condition> conditions = new ArrayList<Condition>();

        conditions.add(pastEventCondition);

        Condition c = new Condition(definitionsService.getConditionType("eventPropertyCondition"));
        c.setParameter("propertyName", "profileId");
        c.setParameter("comparisonOperator", "equals");
        c.setParameter("propertyValue", event.getProfileId());
        conditions.add(c);

        int numberOfDays = 0;
        if (pastEventCondition.getParameter("numberOfDays") != null) {
            numberOfDays = (Integer) pastEventCondition.getParameter("numberOfDays");

            Condition timeCondition = new Condition(definitionsService.getConditionType("eventPropertyCondition"));
            timeCondition.setParameter("propertyName", "timeStamp");
            timeCondition.setParameter("comparisonOperator", "greaterThan");
            timeCondition.setParameter("propertyValueDateExpr", "now-" + numberOfDays + "d");

            conditions.add(timeCondition);
        }

        andCondition.setParameter("subConditions", conditions);

        long count = persistenceService.queryCount(andCondition, Event.ITEM_TYPE);

        Map<String, Object> pastEvents = (Map<String, Object>) event.getProfile().getSystemProperties().get("pastEvents");
        if (pastEvents == null) {
            pastEvents = new LinkedHashMap<>();
            event.getProfile().getSystemProperties().put("pastEvents", pastEvents);
        }

        //Only increase the counter by 1 if the current event is in the now-numberOfDays range
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        LocalDateTime eventTime = LocalDateTime.ofInstant(event.getTimeStamp().toInstant(),ZoneId.of("UTC"));
        Duration durationDiff = Duration.between(eventTime,now);
        if (!durationDiff.isNegative() && durationDiff.toDays() <= numberOfDays) {
            count++;
        }

        pastEvents.put((String) pastEventCondition.getParameter("generatedPropertyKey"), count);

        return EventService.PROFILE_UPDATED;
    }
}
