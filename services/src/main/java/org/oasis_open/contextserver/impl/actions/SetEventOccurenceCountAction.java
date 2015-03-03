package org.oasis_open.contextserver.impl.actions;

import org.oasis_open.contextserver.api.Event;
import org.oasis_open.contextserver.api.actions.Action;
import org.oasis_open.contextserver.api.actions.ActionExecutor;
import org.oasis_open.contextserver.api.conditions.Condition;
import org.oasis_open.contextserver.api.services.DefinitionsService;
import org.oasis_open.contextserver.impl.services.ParserHelper;
import org.oasis_open.contextserver.persistence.spi.PersistenceService;

import java.util.ArrayList;

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
    public boolean execute(Action action, Event event) {
        final Condition pastEventCondition = (Condition) action.getParameterValues().get("pastEventCondition");

        Condition andCondition = new Condition(definitionsService.getConditionType("booleanCondition"));
        andCondition.setParameter("operator", "and");
        ArrayList<Condition> conditions = new ArrayList<Condition>();

        Condition eventCondition = (Condition) pastEventCondition.getParameterValues().get("eventCondition");
        ParserHelper.resolveConditionType(definitionsService, eventCondition);
        conditions.add(eventCondition);

        Condition c = new Condition(definitionsService.getConditionType("eventPropertyCondition"));
        c.setParameter("propertyName","profileId");
        c.setParameter("comparisonOperator", "equals");
        c.setParameter("propertyValue",event.getProfileId());
        conditions.add(c);

        if (pastEventCondition.getParameterValues().get("numberOfDays") != null) {
            int i = (Integer) pastEventCondition.getParameterValues().get("numberOfDays");

            Condition timeCondition = new Condition(definitionsService.getConditionType("eventPropertyCondition"));
            timeCondition.setParameter("propertyName","timeStamp");
            timeCondition.setParameter("comparisonOperator","greaterThan");
            timeCondition.setParameter("propertyValueDateExpr","now-"+i+"d");

            conditions.add(timeCondition);
        }

        andCondition.setParameter("subConditions", conditions);

        long count = persistenceService.queryCount(andCondition, Event.ITEM_TYPE);

        event.getProfile().setProperty((String) pastEventCondition.getParameterValues().get("generatedPropertyKey"), count+1);

        return true;
    }
}
