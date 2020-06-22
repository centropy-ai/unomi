package org.apache.unomi.operation.segment;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.unomi.api.Event;
import org.apache.unomi.api.Profile;
import org.apache.unomi.api.services.ProfileService;
import org.apache.unomi.operation.ConsumeEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SegmentListener implements ConsumeEventProcessor, Processor {
    private static Logger logger = LoggerFactory.getLogger(SegmentListener.class);

    final String SEGMENT_OPT_IN = "segmentOptIn";
    final String SEGMENT_OPT_OUT = "segmentOptOut";

    private ProfileService profileService;

    @Override
    public void process(Event e) {
        Profile p = this.profileService.load(e.getProfileId());
        String segmentID = (String) e.getProperties().get("segment");
        if (e.getEventType().equals(SEGMENT_OPT_IN)) {
            logger.info("Optin segment", segmentID, p.getItemId());
            p.getProperties().remove(segmentID);
        }
        if (e.getEventType().equals(SEGMENT_OPT_OUT)) {
            logger.info("Optout segment", segmentID, p.getItemId());
            p.getSegments().remove(segmentID);
        }
        this.profileService.save(p);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Event ev = exchange.getIn().getBody(Event.class);

        if (ev.getEventType().equals(SEGMENT_OPT_IN) || ev.getEventType().equals(SEGMENT_OPT_OUT)) {
            this.process(ev);
        }
    }

    public void setProfileService(ProfileService profileService) {
        this.profileService = profileService;
    }
}
