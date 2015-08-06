package dwo.gameserver.model.world.olympiad;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.type.L2OlympiadStadiumZone;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author godson, GodKratos, Pere, DS
 */
public abstract class AbstractOlympiadGame
{
	protected static final Logger _log = LogManager.getLogger(AbstractOlympiadGame.class);

	protected static final String POINTS = "olympiad_points";
	protected static final String COMP_DONE = "competitions_done";
	protected static final String COMP_DONE_WEEKLY = "competitions_done_weekly";
	protected static final String COMP_DONE_WEEKLY_CLASSED = "competitions_done_weekly_c";
	protected static final String COMP_DONE_WEEKLY_NON_CLASSED = "competitions_done_weekly_nc";
	protected static final String COMP_WON = "competitions_won";
	protected static final String COMP_LOST = "competitions_lost";
	protected static final String COMP_DRAWN = "competitions_drawn";
	protected final int _stadiumID;
	protected long _startTime;
	protected boolean _aborted;

	protected AbstractOlympiadGame(int id)
	{
		_stadiumID = id;
	}

	/**
	 * Function return null if player passed all checks
	 * or SystemMessage with reason for broadcast to opponent(s).
	 *
	 * @param player
	 * @return
	 */
	protected static SystemMessage checkDefaulted(L2PcInstance player)
	{
		if(player == null || !player.isOnline())
		{
			return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME);
		}

		if(player.getClient() == null || player.getClient().isDetached())
		{
			return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME);
		}

		// safety precautions
		if(player.getObserverController().isObserving() || EventManager.isPlayerParticipant(player))
		{
			return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		}

		if(player.isDead())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_PARTICIPATE_OLYMPIAD_WHILE_DEAD).addPcName(player));
			return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		}
		if(player.isSubClassActive())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_PARTICIPATE_IN_OLYMPIAD_WHILE_CHANGED_TO_SUB_CLASS).addPcName(player));
			return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		}
		if(player.isCursedWeaponEquipped())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_JOIN_OLYMPIAD_POSSESSING_S2).addPcName(player).addItemName(player.getCursedWeaponEquippedId()));
			return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		}
		if(!player.isInventoryUnder90(true))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_PARTICIPATE_IN_OLYMPIAD_INVENTORY_SLOT_EXCEEDS_80_PERCENT).addPcName(player));
			return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
		}

		return null;
	}

	public static void rewardParticipant(L2PcInstance player, int[][] reward)
	{
		if(player == null || !player.isOnline() || reward == null)
		{
			return;
		}

		try
		{
			L2ItemInstance item;
			InventoryUpdate iu = new InventoryUpdate();
			for(int[] it : reward)
			{
				if(it == null || it.length != 2)
				{
					continue;
				}

				item = player.getInventory().addItem(ProcessType.OLYMPIAD, it[0], it[1], player, null);
				if(item == null)
				{
					continue;
				}

				iu.addModifiedItem(item);
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(it[0]).addNumber(it[1]));
			}
			player.sendPacket(iu);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}
	}

	public boolean isAborted()
	{
		return _aborted;
	}

	public int getStadiumId()
	{
		return _stadiumID;
	}

	protected boolean makeCompetitionStart()
	{
		_startTime = System.currentTimeMillis();
		return !_aborted;
	}

	protected void addPointsToParticipant(Participant par, int points)
	{
		par.updateStat(POINTS, points);
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_GAINED_S2_OLYMPIAD_POINTS);
		sm.addString(par.getName());
		sm.addNumber(points);
		broadcastPacket(sm);

		if(par.getPlayer() != null)
		{
			HookManager.getInstance().notifyEvent(HookType.ON_OLY_BATTLE_END, par.getPlayer().getHookContainer(), par.getPlayer(), getType(), true);
		}
	}

	protected void removePointsFromParticipant(Participant par, int points)
	{
		par.updateStat(POINTS, -points);
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_LOST_S2_OLYMPIAD_POINTS);
		sm.addString(par.getName());
		sm.addNumber(points);
		broadcastPacket(sm);

		if(par.getPlayer() != null)
		{
			HookManager.getInstance().notifyEvent(HookType.ON_OLY_BATTLE_END, par.getPlayer().getHookContainer(), par.getPlayer(), getType(), false);
		}
	}

	public abstract CompetitionType getType();

	public abstract String[] getPlayerNames();

	public abstract boolean containsParticipant(int playerId);

	public abstract void sendOlympiadInfo(L2Character player);

	public abstract void broadcastOlympiadInfo(L2OlympiadStadiumZone stadium);

	protected abstract void broadcastPacket(L2GameServerPacket packet);

	protected abstract boolean checkDefaulted();

	protected abstract void removals();

	protected abstract boolean portPlayersToArena(List<Location> spawns);

	protected abstract void cleanEffects();

	protected abstract void portPlayersBack();

	protected abstract void playersStatusBack();

	protected abstract void clearPlayers();

	protected abstract void handleDisconnect(L2PcInstance player);

	protected abstract void resetDamage();

	protected abstract void addDamage(L2PcInstance player, int damage);

	protected abstract boolean checkBattleStatus();

	protected abstract boolean haveWinner();

	protected abstract void validateWinner(L2OlympiadStadiumZone stadium);

	protected abstract int getDivider();

	protected abstract int[][] getReward();

	protected abstract String getWeeklyMatchType();
}