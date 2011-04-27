package org.sfnelson.sk.client.request;

import java.util.Date;
import java.util.List;

import org.sfnelson.sk.server.ServiceLocator;
import org.sfnelson.sk.server.request.EventService;

import com.google.gwt.requestfactory.shared.Request;
import com.google.gwt.requestfactory.shared.RequestContext;
import com.google.gwt.requestfactory.shared.Service;

@Service(value=EventService.class, locator=ServiceLocator.class)
public interface EventRequest extends RequestContext {

	Request<Void> assign(String region, String server, String group, Long characterId, Long lootId);
	Request<Void> joinParty(GroupProxy group, CharacterProxy character);
	Request<Void> leaveParty(GroupProxy group, CharacterProxy character);
	Request<List<EventProxy>> getEvents(GroupProxy group, Date lastUpdate);

}
