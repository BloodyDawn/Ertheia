package dwo.scripts.npc.castle;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import gnu.trove.procedure.TObjectProcedure;
import org.apache.log4j.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.10.12
 * Time: 1:12
 */

public class CastleMassTeleporter extends Quest
{
	private static final int[] NPCs = {35095, 35137, 35179, 35221, 35266, 35311, 35355, 35502, 35547};

	private static final Map<Integer, ScheduledFuture<?>> _teleTasks = new HashMap<>();

	public CastleMassTeleporter()
	{
		addFirstTalkId(NPCs);
		addTeleportRequestId(NPCs);
	}

	public static void main(String[] args)
	{
		new CastleMassTeleporter();
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		int delay;
		if(_teleTasks.containsKey(npc.getNpcId()))
		{
			return "CastleTeleportDelayed.htm";
		}
		delay = npc.getCastle().getSiege().isInProgress() && npc.getCastle().getSiege().getControlTowerCount() == 0 ? 480000 : 30000;
		_teleTasks.put(npc.getNpcId(), ThreadPoolManager.getInstance().scheduleGeneral(new TeleportPlayers(npc), delay));
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(_teleTasks.containsKey(npc.getNpcId()))
		{
			return "CastleTeleportDelayed.htm";
		}
		else
		{
			return npc.getCastle().getSiege().isInProgress() && npc.getCastle().getSiege().getControlTowerCount() == 0 ? "gludio_mass_teleporter002.htm" : "gludio_mass_teleporter001.htm";
		}
	}

	/**
	 * Задача на телепортацию игроков
	 */
	class TeleportPlayers implements Runnable
	{
		L2Npc _npc;

		public TeleportPlayers(L2Npc npc)
		{
			_npc = npc;
		}

		@Override
		public void run()
		{
			try
			{
				NS cs = new NS(_npc.getObjectId(), ChatType.SHOUT, _npc.getNpcId(), NpcStringId.THE_DEFENDERS_OF_S1_CASTLE_WILL_BE_TELEPORTED_TO_THE_INNER_CASTLE);
				cs.addStringParameter(_npc.getCastle().getName());
				int region = MapRegionManager.getInstance().getMapRegionLocId(_npc.getX(), _npc.getY());
				WorldManager.getInstance().forEachPlayer(new ForEachPlayerInRegionSendPacket(region, cs));

				// Телепортируем игроков, которые в радиусе 200 юнитов от телепортера
				for(L2PcInstance player : _npc.getKnownList().getKnownPlayersInRadius(200))
				{
					player.teleToLocation(_npc.getTemplate().getTelePosition(1));
				}
			}
			catch(NullPointerException e)
			{
				_log.log(Level.ERROR, CastleMassTeleporter.class.getSimpleName() + ": Error while teleporting task: " + e.getMessage(), e);
			}
			finally
			{
				_teleTasks.remove(_npc.getNpcId());
			}
		}
	}

	/**
	 * Процедура отправки пакета персонажам в заданные регион
	 */
	private class ForEachPlayerInRegionSendPacket implements TObjectProcedure<L2PcInstance>
	{
		int _region;
		NS _cs;

		private ForEachPlayerInRegionSendPacket(int region, NS cs)
		{
			_region = region;
			_cs = cs;
		}

		@Override
		public boolean execute(L2PcInstance player)
		{
			if(_region == MapRegionManager.getInstance().getMapRegionLocId(player.getX(), player.getY()))
			{
				player.sendPacket(_cs);
			}
			return true;
		}
	}
}