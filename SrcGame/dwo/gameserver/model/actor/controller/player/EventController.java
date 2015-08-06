package dwo.gameserver.model.actor.controller.player;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.restriction.IRestrictionChecker;
import dwo.gameserver.model.actor.restriction.RestrictionCheck;
import dwo.gameserver.model.actor.restriction.RestrictionCheckList;
import dwo.gameserver.model.world.npc.L2Event;
import dwo.gameserver.model.world.zone.Location;

import java.util.LinkedList;
import java.util.Map;

/**
 * Player event engine controller.
 * TODO: This shit should be fully refactored. For now it's just moved from L2PcInstance.
 *
 * @author Yorie
 */
@RestrictionCheckList(RestrictionCheck.PARTICIPATING_EVENT)
public class EventController extends PlayerController implements IRestrictionChecker
{
	/**
	 * Event parameters
	 */
	private int reputation;
	private int pvpKills;
	private int pkKills;
	private String title;
	private LinkedList<String> kills = new LinkedList<>();
	private boolean isForceSit;
	private boolean isParticipant;
	// TODO: Waht a f..?
	private byte handysBlockCheckerEventArena = -1;

	public EventController(L2PcInstance player)
	{
		super(player);
	}

	@Override
	public boolean checkRestriction(RestrictionCheck check, Map<RestrictionCheck, Object> params)
	{
		switch(check)
		{
			case PARTICIPATING_EVENT:
				return isParticipant;
		}
		return true;
	}

	public boolean isForceSit()
	{
		return isForceSit;
	}

	public void setForceSit(boolean value)
	{
		isForceSit = value;
	}

	public boolean isParticipant()
	{
		return isParticipant;
	}

	public void setParticipating(boolean value)
	{
		isParticipant = value;
	}

	public void addKill(String name)
	{
		kills.add(name);
	}

	public boolean isInHandysBlockCheckerEventArena()
	{
		return handysBlockCheckerEventArena != -1;
	}

	public byte getHandysBlockCheckerEventArena()
	{
		return handysBlockCheckerEventArena;
	}

	public void setHandysBlockCheckerEventArena(byte arenaId)
	{
		handysBlockCheckerEventArena = arenaId;
	}

	public int getKillsCount()
	{
		return kills.size();
	}

	public LinkedList<String> getKills()
	{
		return kills;
	}

	public void clearKillsList()
	{
		kills.clear();
	}

	public int getReputation()
	{
		return reputation;
	}

	public int getPvPKills()
	{
		return pvpKills;
	}

	public int getPkKills()
	{
		return pkKills;
	}

	public String getTitle()
	{
		return title;
	}

	public void prepare()
	{
		reputation = player.getReputation();
		player.getLocationController().rememberLocation();
		pkKills = player.getPkKills();
		pvpKills = player.getPvpKills();
		title = player.getTitle();
		isParticipant = false;
		clearKillsList();
	}

	public void restore()
	{
		if(!L2Event.active || !L2Event.connectionLossData.containsKey(player.getName()))
		{
			return;
		}

		reputation = L2Event.connectionLossData.get(player.getName()).eventReputation;
		pvpKills = L2Event.connectionLossData.get(player.getName()).eventPvpKills;
		pkKills = L2Event.connectionLossData.get(player.getName()).eventPkKills;
		title = L2Event.connectionLossData.get(player.getName()).eventTitle;
		kills = L2Event.connectionLossData.get(player.getName()).kills;
		isForceSit = L2Event.connectionLossData.get(player.getName()).eventSitForced;
		isParticipant = true;

		if(L2Event.isOnEvent(player))
		{
			Location loc = new Location(L2Event.connectionLossData.get(player.getName()).eventX, L2Event.connectionLossData.get(player.getName()).eventY, L2Event.connectionLossData.get(player.getName()).eventZ);
			player.teleToLocation(loc, true);
		}
	}
}
