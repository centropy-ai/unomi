package org.apache.unomi.operation.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.unomi.api.Event;
import org.apache.unomi.api.Profile;
import org.apache.unomi.api.Session;
import org.apache.unomi.api.services.EventService;
import org.apache.unomi.api.services.ProfileService;
import org.apache.unomi.operation.ConsumeEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaEventInjectorListener implements ConsumeEventProcessor, Processor {
    private static Logger logger = LoggerFactory.getLogger(KafkaEventInjectorListener.class);

    private EventService eventService;
    private ProfileService profileService;

    @Override
    public void process(Event e) {
        if (e != null) {
            Profile profile = profileService.load(e.getProfileId());
            Session session = profileService.loadSession(e.getSessionId(), e.getTimeStamp());
            e.setProfile(profile);
            e.setSession(session);
            e.setPersistent(false);
            int changes = this.eventService.send(e);
            if ((changes & EventService.PROFILE_UPDATED) == EventService.PROFILE_UPDATED) {
                profileService.save(profile);
                Event profileUpdated = new Event("profileUpdated", session, profile, session.getScope(), e, profile, e.getTimeStamp());
                profileUpdated.setPersistent(false);
                eventService.send(profileUpdated);
            }
            if ((changes & EventService.SESSION_UPDATED) == EventService.SESSION_UPDATED) {
                if (session != null) {
                    profileService.saveSession(session);
                    eventService.send(new Event("sessionUpdated", session, profile, session.getScope(), e, session, e.getTimeStamp()));
                }
            }
        }
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Event ev = exchange.getIn().getBody(Event.class);

        this.process(ev);
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void setProfileService(ProfileService profileService) {
        this.profileService = profileService;
    }
}
