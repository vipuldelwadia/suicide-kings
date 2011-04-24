package org.sfnelson.sk.client.activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sfnelson.sk.client.EventManager;
import org.sfnelson.sk.client.Factory;
import org.sfnelson.sk.client.event.SuicideKingsEvent;
import org.sfnelson.sk.client.event.SuicideKingsEvent.EventHandler;
import org.sfnelson.sk.client.place.Group;
import org.sfnelson.sk.client.place.Realm;
import org.sfnelson.sk.client.request.CharacterProxy;
import org.sfnelson.sk.client.request.CharacterRequest;
import org.sfnelson.sk.client.request.EventRequest;
import org.sfnelson.sk.client.view.LadderView;
import org.sfnelson.sk.client.view.LadderView.LadderPresenter;
import org.sfnelson.sk.shared.Random;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ShowLadder extends AbstractActivity implements LadderPresenter, EventHandler {

	private final Factory factory;

	private final Realm realm;
	private final Group group;

	private final EventManager manager;

	private LadderView view;

	private boolean showAll = false;

	public ShowLadder(Factory factory, Group group) {
		this.factory = factory;
		this.realm = group.getRealm();
		this.group = group;

		manager = new EventManager(factory.getRequestFactory(), factory.getPlaceController(), group);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		view = factory.getLadderView();
		view.setPresenter(this);

		SuicideKingsEvent.register(eventBus, this);
		manager.start(null, eventBus);

		panel.setWidget(view);
	}

	@Override
	public void onStop() {
		view = null;
		manager.onStop();
	}

	@Override
	public void setShowAll(boolean showAll) {
		this.showAll = showAll;

		update(false);
	}

	@Override
	public void registerCharacter(String name) {
		CharacterRequest rq = factory.getRequestFactory().characterRequest();
		rq.registerCharacter(realm.getRegion().getRegion(), realm.getServer(), group.getName(), name);
		rq.fire();
	}

	@Override
	public void joinParty(CharacterProxy character) {
		EventRequest rq = factory.getRequestFactory().eventRequest();
		rq.joinParty(manager.getGroup(), character);
		rq.fire();
	}

	@Override
	public void leaveParty(CharacterProxy character) {
		EventRequest rq = factory.getRequestFactory().eventRequest();
		rq.leaveParty(manager.getGroup(), character);
		rq.fire();
	}

	private void update(boolean listChanged) {
		if (view == null) return;

		if (listChanged) {
			view.setData(ladder);
		}

		if (showAll) {
			view.showData(ladder);
		}
		else {
			view.showData(new ArrayList<CharacterProxy>(active));
		}
	}

	private final List<CharacterProxy> ladder = new ArrayList<CharacterProxy>();
	private final Set<CharacterProxy> active = new HashSet<CharacterProxy>();

	@Override
	public void onCharacterAdded(SuicideKingsEvent event) {
		CharacterProxy character = event.getCharacter();

		if (ladder.contains(character)) {
			throw new IllegalArgumentException("Cannot add character to the ladder. Character is already present.");
		}

		int position = Random.getRandom(character.getSeed(), 0, ladder.size());
		ladder.add(position, character);

		update(true);
	}

	@Override
	public void onCharacterJoined(SuicideKingsEvent event) {
		CharacterProxy character = event.getCharacter();

		active.add(character);

		update(false);
	}

	@Override
	public void onCharacterLeft(SuicideKingsEvent event) {
		CharacterProxy character = event.getCharacter();

		active.remove(character);

		update(false);
	}

	@Override
	public void onReceivedLoot(SuicideKingsEvent event) {
		CharacterProxy character = event.getCharacter();

		int position = 0;
		while (!ladder.get(position).equals(character)) {
			if (++position >= ladder.size()) return;
		}

		while (true) { // move character down
			int next = position;
			do { // find next active character
				if (++next >= ladder.size()) {
					update(true);
					return;
				}
			} while (!active.contains(character));

			swap(position, next);

			position = next;
		}
	}

	private void swap(int a, int b) {
		CharacterProxy ca = ladder.get(a);
		CharacterProxy cb = ladder.get(b);

		ladder.set(a, cb);
		ladder.set(b, ca);
	}
}
