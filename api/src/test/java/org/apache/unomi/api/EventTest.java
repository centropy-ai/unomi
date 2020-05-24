package org.apache.unomi.api;


import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class EventTest extends TestCase {
    public EventTest(String name) {
        super(name);
    }

    @Test
    public void testSendAtInFuture() {
        Event e = new Event();
        Calendar nextOneHour = Calendar.getInstance();
        nextOneHour.add(Calendar.HOUR, 1);
        e.setTimeStamp(nextOneHour.getTime());
        Calendar nextTwoHour = Calendar.getInstance();
        nextTwoHour.add(Calendar.HOUR, 2);
        e.setSendAt(nextTwoHour.getTime());
        Date now = Calendar.getInstance().getTime();
        Assert.assertTrue("sendAt must be allow less than current system time", e.getSendAt().getTime() <= now.getTime());
        Assert.assertTrue("timeStamp must be less then or equal to sendAt", e.getTimeStamp().getTime() <= e.getSendAt().getTime());
    }
}