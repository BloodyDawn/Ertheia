package dwo.scripts.ai.fantasy_isle;

import dwo.config.events.ConfigEvents;
import dwo.gameserver.instancemanager.games.HandysBlockCheckerManager;
import dwo.gameserver.instancemanager.games.HandysBlockCheckerManager.ArenaParticipantsHolder;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExBlockUpSetList;
import org.apache.log4j.Level;

/**
 * @authors BiggBoss, Gigiikun
 */

public class HandysBlockCheckerEvent extends Quest
{
	private static final String qn = "HandysBlockCheckerEvent";

	// Arena Managers
	private static final int A_MANAGER_1 = 32521;
	private static final int A_MANAGER_2 = 32522;
	private static final int A_MANAGER_3 = 32523;
	private static final int A_MANAGER_4 = 32524;

	public HandysBlockCheckerEvent()
	{
		addFirstTalkId(A_MANAGER_1, A_MANAGER_2, A_MANAGER_3, A_MANAGER_4);
	}

	public static void main(String[] args)
	{
		if(ConfigEvents.ENABLE_BLOCK_CHECKER_EVENT)
		{
			new HandysBlockCheckerEvent();
			HandysBlockCheckerManager.getInstance().startUpParticipantsQueue();
			_log.log(Level.INFO, "Handy's Block Checker Event is enabled");
		}
		else
		{
			_log.log(Level.INFO, "Handy's Block Checker Event is disabled");
		}
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc == null || player == null)
		{
			return null;
		}

		int arena = npc.getNpcId() - A_MANAGER_1;
		if(eventIsFull(arena))
		{
			player.sendPacket(SystemMessageId.CANNOT_REGISTER_CAUSE_QUEUE_FULL);
			return null;
		}

		if(HandysBlockCheckerManager.getInstance().arenaIsBeingUsed(arena))
		{
			player.sendPacket(SystemMessageId.MATCH_BEING_PREPARED_TRY_LATER);
			return null;
		}

		if(HandysBlockCheckerManager.getInstance().addPlayerToArena(player, arena))
		{
			ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(arena);

			ExBlockUpSetList tl = new ExBlockUpSetList(holder.getRedPlayers(), holder.getBluePlayers(), arena);

			player.sendPacket(tl);

			int countBlue = holder.getBlueTeamSize();
			int countRed = holder.getRedTeamSize();
			int minMembers = ConfigEvents.MIN_BLOCK_CHECKER_TEAM_MEMBERS;

			if(countBlue >= minMembers && countRed >= minMembers)
			{
				holder.updateEvent();
				holder.broadCastPacketToTeam(new ExBlockUpSetList(false));
				holder.broadCastPacketToTeam(new ExBlockUpSetList(10));
			}
		}
		return null;
	}

	private boolean eventIsFull(int arena)
	{
		return HandysBlockCheckerManager.getInstance().getHolder(arena).getAllPlayers().size() == 12;
	}
}