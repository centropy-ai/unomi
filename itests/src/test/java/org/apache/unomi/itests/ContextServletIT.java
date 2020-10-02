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
 * limitations under the License
 */

package org.apache.unomi.itests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.unomi.api.*;
import org.apache.unomi.api.conditions.Condition;
import org.apache.unomi.api.segments.Segment;
import org.apache.unomi.api.services.DefinitionsService;
import org.apache.unomi.api.services.EventService;
import org.apache.unomi.api.services.ProfileService;
import org.apache.unomi.api.services.SegmentService;
import org.apache.unomi.persistence.spi.PersistenceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.util.Filter;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.*;


/**
 * Created by Ron Barabash on 5/4/2020.
 */

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class ContextServletIT extends BaseIT {
	private final static String CONTEXT_URL = "/context.json";
	private final static String THIRD_PARTY_HEADER_NAME = "X-Unomi-Peer";
	private final static String SEGMENT_EVENT_TYPE = "test-event-type";
	private final static String SEGMENT_ID = "test-segment-id";
	private final static int SEGMENT_NUMBER_OF_DAYS = 30;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Inject
	@Filter(timeout = 600000)
	protected EventService eventService;

	@Inject
	@Filter(timeout = 600000)
	protected PersistenceService persistenceService;

	@Inject
	@Filter(timeout = 600000)
	protected ProfileService profileService;

	@Inject
	@Filter(timeout = 600000)
	protected DefinitionsService definitionsService;

	@Inject
	@Filter(timeout = 600000)
	protected SegmentService segmentService;

	private Profile profile;

	@Before
	public void setUp() throws InterruptedException {
		//Create a past-event segment
		Metadata segmentMetadata = new Metadata(SEGMENT_ID);
		Segment segment = new Segment(segmentMetadata);
		Condition segmentCondition = new Condition(definitionsService.getConditionType("pastEventCondition"));
		segmentCondition.setParameter("minimumEventCount",2);
		segmentCondition.setParameter("numberOfDays",SEGMENT_NUMBER_OF_DAYS);
		Condition pastEventEventCondition = new Condition(definitionsService.getConditionType("eventTypeCondition"));
		pastEventEventCondition.setParameter("eventTypeId",SEGMENT_EVENT_TYPE);
		segmentCondition.setParameter("eventCondition",pastEventEventCondition);
		segment.setCondition(segmentCondition);
		segmentService.setSegmentDefinition(segment);

		String profileId = "test-profile-id";
		profile = new Profile(profileId);
		profileService.save(profile);

		refreshPersistence();
	}

	@After
	public void tearDown() {
		TestUtils.removeAllEvents(definitionsService, persistenceService);
		TestUtils.removeAllSessions(definitionsService, persistenceService);
		TestUtils.removeAllProfiles(definitionsService, persistenceService);
		profileService.delete(profile.getItemId(), false);
		segmentService.removeSegmentDefinition(SEGMENT_ID,false);
	}

	@Test
	public void testUpdateEventFromContextAuthorizedThirdParty_Success() throws IOException, InterruptedException {
		//Arrange
		String eventId = "test-event-id-" + System.currentTimeMillis();
		String profileId = "test-profile-id";
		String sessionId = "test-session-id";
		String scope = "test-scope";
		String eventTypeOriginal = "test-event-type-original";
		String eventTypeUpdated = "test-event-type-updated";
		Profile profile = new Profile(profileId);
		Session session = new Session(sessionId, profile, new Date(), scope);
		Event event = new Event(eventId, eventTypeOriginal, session, profile, scope, null, null, new Date());
		profileService.save(profile);
		this.eventService.send(event);
		refreshPersistence();
		Thread.sleep(2000);
		event.setEventType(eventTypeUpdated); //change the event so we can see the update effect

		//Act
		ContextRequest contextRequest = new ContextRequest();
		contextRequest.setSessionId(session.getItemId());
		contextRequest.setEvents(Arrays.asList(event));
		HttpPost request = new HttpPost(URL + CONTEXT_URL);
		request.addHeader(THIRD_PARTY_HEADER_NAME, UNOMI_KEY);
		request.setEntity(new StringEntity(objectMapper.writeValueAsString(contextRequest), ContentType.create("application/json")));
		TestUtils.executeContextJSONRequest(request, sessionId);
		refreshPersistence();
		Thread.sleep(2000); //Making sure event is updated in DB

		//Assert
		event = this.eventService.getEvent(eventId);
		assertEquals(2, event.getVersion().longValue());
		assertEquals(eventTypeUpdated,event.getEventType());
	}

	@Test
	public void testUpdateEventFromContextUnAuthorizedThirdParty_Fail() throws IOException, InterruptedException {
		//Arrange
		String eventId = "test-event-id-" + System.currentTimeMillis();
		String profileId = "test-profile-id";
		String sessionId = "test-session-id";
		String scope = "test-scope";
		String eventTypeOriginal = "test-event-type-original";
		String eventTypeUpdated = "test-event-type-updated";
		Profile profile = new Profile(profileId);
		Session session = new Session(sessionId, profile, new Date(), scope);
		Event event = new Event(eventId, eventTypeOriginal, session, profile, scope, null, null, new Date());
		profileService.save(profile);
		this.eventService.send(event);
		refreshPersistence();
		Thread.sleep(2000);
		event.setEventType(eventTypeUpdated); //change the event so we can see the update effect

		//Act
		ContextRequest contextRequest = new ContextRequest();
		contextRequest.setSessionId(session.getItemId());
		contextRequest.setEvents(Arrays.asList(event));
		HttpPost request = new HttpPost(URL + CONTEXT_URL);
		request.setEntity(new StringEntity(objectMapper.writeValueAsString(contextRequest), ContentType.create("application/json")));
		TestUtils.executeContextJSONRequest(request, sessionId);
		refreshPersistence();
		Thread.sleep(2000); //Making sure event is updated in DB

		//Assert
		event = this.eventService.getEvent(eventId);
		assertEquals(1, event.getVersion().longValue());
		assertEquals(eventTypeOriginal,event.getEventType());
	}


	@Test
	public void testUpdateEventFromContextAuthorizedThirdPartyNoItemID_Fail() throws IOException, InterruptedException {
		//Arrange
		String eventId = "test-event-id-" + System.currentTimeMillis();
		String sessionId = "test-session-id";
		String scope = "test-scope";
		String eventTypeOriginal = "test-event-type-original";
		String eventTypeUpdated = "test-event-type-updated";
		Session session = new Session(sessionId, profile, new Date(), scope);
		Event event = new Event(eventId, eventTypeOriginal, session, profile, scope, null, null, new Date());
		this.eventService.send(event);
		refreshPersistence();
		Thread.sleep(2000);
		event.setEventType(eventTypeUpdated); //change the event so we can see the update effect

		//Act
		ContextRequest contextRequest = new ContextRequest();
		contextRequest.setSessionId(session.getItemId());
		contextRequest.setEvents(Arrays.asList(event));
		HttpPost request = new HttpPost(URL + CONTEXT_URL);
		request.setEntity(new StringEntity(objectMapper.writeValueAsString(contextRequest), ContentType.create("application/json")));
		TestUtils.executeContextJSONRequest(request, sessionId);
		refreshPersistence();
		Thread.sleep(2000); //Making sure event is updated in DB

		//Assert
		event = this.eventService.getEvent(eventId);
		assertEquals(1, event.getVersion().longValue());
		assertEquals(eventTypeOriginal,event.getEventType());
	}

	@Test
	public void testCreateEventsWithNoTimestampParam_profileAddedToSegment() throws IOException, InterruptedException {
		//Arrange
		String sessionId = "test-session-id";
		String scope = "test-scope";
		Event event = new Event();
		event.setEventType(SEGMENT_EVENT_TYPE);
		event.setScope(scope);

		//Act
		ContextRequest contextRequest = new ContextRequest();
		contextRequest.setSessionId(sessionId);
		contextRequest.setRequireSegments(true);
		contextRequest.setEvents(Arrays.asList(event));
		HttpPost request = new HttpPost(URL + CONTEXT_URL);
		request.setEntity(new StringEntity(objectMapper.writeValueAsString(contextRequest), ContentType.create("application/json")));
		String cookieHeaderValue = TestUtils.executeContextJSONRequest(request, sessionId).getCookieHeaderValue();
		refreshPersistence();
		Thread.sleep(1000); //Making sure DB is updated

		//Add the context-profile-id cookie to the second event
		request.addHeader("Cookie", cookieHeaderValue);
		ContextResponse response = (TestUtils.executeContextJSONRequest(request, sessionId)).getContextResponse(); //second event

		refreshPersistence();

		//Assert
		assertEquals(1, response.getProfileSegments().size());
		assertThat(response.getProfileSegments(),hasItem(SEGMENT_ID));
	}

	@Test
	public void testCreateEventWithTimestampParam_pastEvent_profileIsNotAddedToSegment() throws IOException, InterruptedException {
		//Arrange
		String sessionId = "test-session-id";
		String scope = "test-scope";
		Event event = new Event();
		event.setEventType(SEGMENT_EVENT_TYPE);
		event.setScope(scope);
		String regularURI = URL + CONTEXT_URL;
		long oldTimestamp = LocalDateTime.now(ZoneId.of("UTC")).minusDays(SEGMENT_NUMBER_OF_DAYS + 1).toInstant(ZoneOffset.UTC).toEpochMilli();
		String customTimestampURI = regularURI + "?timestamp=" + oldTimestamp;

		//Act
		ContextRequest contextRequest = new ContextRequest();
		contextRequest.setSessionId(sessionId);
		contextRequest.setRequireSegments(true);
		contextRequest.setEvents(Arrays.asList(event));
		HttpPost request = new HttpPost(regularURI);
		request.setEntity(new StringEntity(objectMapper.writeValueAsString(contextRequest), ContentType.create("application/json")));
		//The first event is with a default timestamp (now)
		String cookieHeaderValue = TestUtils.executeContextJSONRequest(request, sessionId).getCookieHeaderValue();
		refreshPersistence();
		//The second event is with a customized timestamp
		request.setURI(URI.create(customTimestampURI));
		request.addHeader("Cookie", cookieHeaderValue);
		ContextResponse response = (TestUtils.executeContextJSONRequest(request, sessionId)).getContextResponse(); //second event
		refreshPersistence();

		//Assert
		assertEquals(0,response.getProfileSegments().size());
	}

	@Test
	public void testCreateEventWithTimestampParam_futureEvent_profileIsNotAddedToSegment() throws IOException, InterruptedException {
		//Arrange
		String sessionId = "test-session-id";
		String scope = "test-scope";
		Event event = new Event();
		event.setEventType(SEGMENT_EVENT_TYPE);
		event.setScope(scope);
		String regularURI = URL + CONTEXT_URL;
		long futureTimestamp = LocalDateTime.now(ZoneId.of("UTC")).plusDays(1).toInstant(ZoneOffset.UTC).toEpochMilli();
		String customTimestampURI = regularURI + "?timestamp=" + futureTimestamp;

		//Act
		ContextRequest contextRequest = new ContextRequest();
		contextRequest.setSessionId(sessionId);
		contextRequest.setRequireSegments(true);
		contextRequest.setEvents(Arrays.asList(event));
		HttpPost request = new HttpPost(regularURI);
		request.setEntity(new StringEntity(objectMapper.writeValueAsString(contextRequest), ContentType.create("application/json")));
		//The first event is with a default timestamp (now)
		String cookieHeaderValue = TestUtils.executeContextJSONRequest(request, sessionId).getCookieHeaderValue();
		refreshPersistence();
		//The second event is with a customized timestamp
		request.setURI(URI.create(customTimestampURI));
		request.addHeader("Cookie", cookieHeaderValue);
		ContextResponse response = (TestUtils.executeContextJSONRequest(request, sessionId)).getContextResponse(); //second event
		refreshPersistence();

		//Assert
		assertEquals(0,response.getProfileSegments().size());
	}

	@Test
	public void testCreateEventWithProfileId_Success() throws IOException, InterruptedException {
		//Arrange
		String eventId = "test-event-id-" + System.currentTimeMillis();
		String profileId = "test-profile-id";
		String eventType = "test-event-type";
		Event event = new Event();
		event.setEventType(eventType);
		event.setItemId(eventId);

		ContextRequest contextRequest = new ContextRequest();
		contextRequest.setProfileId(profileId);
		contextRequest.setEvents(Arrays.asList(event));

		//Act
		HttpPost request = new HttpPost(URL + CONTEXT_URL);
		request.addHeader(THIRD_PARTY_HEADER_NAME, UNOMI_KEY);
		request.setEntity(new StringEntity(objectMapper.writeValueAsString(contextRequest), ContentType.create("application/json")));
		TestUtils.executeContextJSONRequest(request);
		refreshPersistence();
		Thread.sleep(2000); //Making sure event is updated in DB

		//Assert
		Profile profile =  this.profileService.load(profileId);
		assertEquals(profileId, profile.getItemId());
	}

	@Test
	public void testOGNLVulnerability() throws IOException, InterruptedException {

		File vulnFile = new File("target/vuln-file.txt");
		if (vulnFile.exists()) {
			vulnFile.delete();
		}
		String vulnFileCanonicalPath = vulnFile.getCanonicalPath();
		vulnFileCanonicalPath = vulnFileCanonicalPath.replace("\\", "\\\\"); // this is required for Windows support

		Map<String,String> parameters = new HashMap<>();
		parameters.put("VULN_FILE_PATH", vulnFileCanonicalPath);
		HttpPost request = new HttpPost(URL + CONTEXT_URL);
		request.setEntity(new StringEntity(getValidatedBundleJSON("security/ognl-payload-1.json", parameters), ContentType.create("application/json")));
		TestUtils.executeContextJSONRequest(request);
		refreshPersistence();
		Thread.sleep(2000); //Making sure event is updated in DB

		assertFalse("Vulnerability successfully executed ! File created at " + vulnFileCanonicalPath, vulnFile.exists());

	}

	@Test
	public void testMVELVulnerability() throws IOException, InterruptedException {

		File vulnFile = new File("target/vuln-file.txt");
		if (vulnFile.exists()) {
			vulnFile.delete();
		}
		String vulnFileCanonicalPath = vulnFile.getCanonicalPath();
		vulnFileCanonicalPath = vulnFileCanonicalPath.replace("\\", "\\\\"); // this is required for Windows support

		Map<String,String> parameters = new HashMap<>();
		parameters.put("VULN_FILE_PATH", vulnFileCanonicalPath);
		HttpPost request = new HttpPost(URL + CONTEXT_URL);
		request.setEntity(new StringEntity(getValidatedBundleJSON("security/mvel-payload-1.json", parameters), ContentType.create("application/json")));
		TestUtils.executeContextJSONRequest(request);
		refreshPersistence();
		Thread.sleep(2000); //Making sure event is updated in DB

		assertFalse("Vulnerability successfully executed ! File created at " + vulnFileCanonicalPath, vulnFile.exists());

	}
}
