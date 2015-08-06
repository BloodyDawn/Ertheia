package dwo.gameserver.util;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.RelationChanged;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.network.game.serverpackets.packet.info.CI;
import gnu.trove.procedure.TObjectProcedure;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Collection;

public class Broadcast
{
	private static Logger _log = LogManager.getLogger(Broadcast.class);

	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character that have the Character targetted.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR><BR>
	 *
	 */
	public static void toPlayersTargettingMyself(L2Character character, L2GameServerPacket mov)
	{
		for(L2PcInstance player : character.getKnownList().getKnownPlayers().values())
		{
			if(!player.getTarget().equals(character))
			{
				continue;
			}

			player.sendPacket(mov);
		}
	}

	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the
	 * L2Character.<BR>
	 * <BR>
	 *
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in
	 * <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the
	 * L2Character, server just need to go through _knownPlayers to send
	 * Server->Client Packet<BR>
	 * <BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND
	 * Server->Client packet to this L2Character (to do this use method
	 * toSelfAndKnownPlayers)</B></FONT><BR>
	 * <BR>
	 *
	 */
	public static void toKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		for(L2PcInstance player : character.getKnownList().getKnownPlayers().values())
		{
			if(player == null)
			{
				continue;
			}
			try
			{
				player.sendPacket(mov);
				if(mov instanceof CI && character instanceof L2PcInstance)
				{
					int relation = ((L2PcInstance) character).getRelation(player);
					Integer oldRelation = character.getKnownList().getKnownRelations().get(player.getObjectId());
					if(oldRelation != null && oldRelation != relation)
					{
						player.sendPacket(new RelationChanged((L2PcInstance) character, relation, character.isAutoAttackable(player)));
						if(!character.getPets().isEmpty())
						{
							for(L2Summon pet : character.getPets())
							{
								player.sendPacket(new RelationChanged(pet, relation, character.isAutoAttackable(player)));
							}
						}
					}
				}
			}
			catch(NullPointerException e)
			{
				_log.log(Level.ERROR, e.getMessage(), e);
			}
		}
	}

	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers (in the specified
	 * radius) of the L2Character.<BR>
	 * <BR>
	 *
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in
	 * <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the
	 * L2Character, server just needs to go through _knownPlayers to send
	 * Server->Client Packet and check the distance between the targets.<BR>
	 * <BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND
	 * Server->Client packet to this L2Character (to do this use method
	 * toSelfAndKnownPlayers)</B></FONT><BR>
	 * <BR>
	 *
	 */
	public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if(radius < 0)
		{
			radius = 1500;
		}

		for(L2PcInstance player : character.getKnownList().getKnownPlayers().values())
		{
			if(character.isInsideRadius(player, radius, false, false))
			{
				player.sendPacket(mov);
			}
		}
	}

	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character and to the specified character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 *
	 */
	public static void toSelfAndKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		if(character instanceof L2PcInstance)
		{
			character.sendPacket(mov);
		}

		toKnownPlayers(character, mov);
	}

	// To improve performance we are comparing values of radius^2 instead of calculating sqrt all the time
	public static void toSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if(radius < 0)
		{
			radius = 600;
		}

		if(character instanceof L2PcInstance)
		{
			character.sendPacket(mov);
		}

		Collection<L2PcInstance> players = character.getKnownList().getKnownPlayers().values();
		for(L2PcInstance player : players)
		{
			if(player != null && Util.checkIfInRange(radius, character, player, false))
			{
				player.sendPacket(mov);
			}
		}
	}

	/**
	 * Send a packet to all L2PcInstance present in the world.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _allPlayers to send Server->Client Packet<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR><BR>
	 *
	 */
	public static void toAllOnlinePlayers(L2GameServerPacket mov)
	{
		WorldManager.getInstance().forEachPlayer(new ForEachPlayerBroadcast(mov));
	}

	public static void announceToOnlinePlayers(String text)
	{
		Say2 cs = new Say2(0, ChatType.ANNOUNCEMENT, "", text);
		toAllOnlinePlayers(cs);
	}

	public static void toPlayersInInstance(L2GameServerPacket mov, int instanceId)
	{
		WorldManager.getInstance().forEachPlayer(new ForEachPlayerInInstanceBroadcast(mov, instanceId));
	}

	public static void toPlayersInInstance(int npcString, int instanceId)
	{
		WorldManager.getInstance().forEachPlayer(new ForEachPlayerInInstanceBroadcast(NpcStringId.getNpcStringId(npcString).getStaticScreenMessage(), instanceId));
	}

	private static class ForEachPlayerBroadcast implements TObjectProcedure<L2PcInstance>
	{
		L2GameServerPacket _packet;

		private ForEachPlayerBroadcast(L2GameServerPacket packet)
		{
			_packet = packet;
		}

		@Override
		public boolean execute(L2PcInstance onlinePlayer)
		{
			if(onlinePlayer != null && onlinePlayer.isOnline())
			{
				onlinePlayer.sendPacket(_packet);
			}
			return true;
		}
	}

	private static class ForEachPlayerInInstanceBroadcast implements TObjectProcedure<L2PcInstance>
	{
		L2GameServerPacket _packet;
		int _instanceId;

		private ForEachPlayerInInstanceBroadcast(L2GameServerPacket packet, int instanceId)
		{
			_packet = packet;
			_instanceId = instanceId;
		}

		@Override
		public boolean execute(L2PcInstance onlinePlayer)
		{
			if(onlinePlayer != null && onlinePlayer.isOnline() && onlinePlayer.getInstanceId() == _instanceId)
			{
				onlinePlayer.sendPacket(_packet);
			}
			return true;
		}
	}
}