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
    public void testSendAt_CorrectValue() {
        Event e = new Event();
        Calendar timeStampHour = Calendar.getInstance();
        timeStampHour.add(Calendar.HOUR, -2);
        e.setTimeStamp(timeStampHour.getTime());
        Calendar sendAtHour = Calendar.getInstance();
        sendAtHour.add(Calendar.HOUR, -1);
        e.setSendAt(sendAtHour.getTime());
        Assert.assertEquals("sendAt must be allow less than current system time", e.getSendAt(), sendAtHour.getTime());
        Assert.assertEquals("timeStamp must be less then or equal to sendAt", e.getTimeStamp(), timeStampHour.getTime());
    }

    @Test
    public void testSendAt_InFuture() {
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
