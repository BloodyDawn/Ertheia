package dwo.gameserver.model.player.formation.group;

import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.arrays.IL2Procedure;
import javolution.util.FastList;

import java.util.List;

public abstract class AbstractPlayerGroup
{
	/**
	 * @return all members of this group
	 */
	public abstract List<L2PcInstance> getMembers();

	/**
	 * @return object IDs of all members of this group
	 */
	public List<Integer> getMembersObjectId()
	{
		List<Integer> ids = new FastList<>();
		forEachMember(member -> {
			ids.add(member.getObjectId());
			return true;
		});
		return ids;
	}

	/**
	 * @return leader of this group
	 */
	public abstract L2PcInstance getLeader();

	/**
	 * Change the leader of this group to the specified player.
	 * @param leader the player to set as the new leader of this group
	 */
	public abstract void setLeader(L2PcInstance leader);

	/**
	 * @return the leader's object ID
	 */
	public int getLeaderObjectId()
	{
		return getLeader().getObjectId();
	}

	/**
	 * Check if a given player is the leader of this group.
	 * @param player the player to check
	 * @return {@code true} if the specified player is the leader of this group, {@code false} otherwise
	 */
	public boolean isLeader(L2PcInstance player)
	{
		return getLeaderObjectId() == player.getObjectId();
	}

	/**
	 * @return count of all players in this group
	 */
	public int getMemberCount()
	{
		return getMembers().size();
	}

	/**
	 * @return level of this group
	 */
	public abstract int getLevel();

	/**
	 * Broadcast packet to every member of this group
	 * @param packet packet to broadcast
	 */
	public void broadcastPacket(L2GameServerPacket packet)
	{
		forEachMember(member -> {
			if(member != null)
			{
				member.sendPacket(packet);
			}
			return true;
		});
	}

	/**
	 * Broadcasts a System Message to this group
	 * @param message System Message to bradcast
	 */
	public void broadcastMessage(SystemMessageId message)
	{
		broadcastPacket(SystemMessage.getSystemMessage(message));
	}

	/**
	 * Broadcasts a string message to this group
	 * @param text to broadcast
	 */
	public void broadcastString(String text)
	{
		broadcastPacket(SystemMessage.sendString(text));
	}

	public void broadcastCreatureSay(Say2 msg, L2PcInstance broadcaster)
	{
		forEachMember(member -> {
			if(member != null && !RelationListManager.getInstance().isBlocked(member, broadcaster))
			{
				member.sendPacket(msg);
			}
			return true;
		});
	}

	/**
	 * @param player to be contained
	 * @return {@code true} if this group contains player
	 */
	public boolean containsPlayer(L2PcInstance player)
	{
		return getMembers().contains(player);
	}

	/**
	 * @return random member of this group
	 */
	public L2PcInstance getRandomPlayer()
	{
		return getMembers().get(Rnd.get(getMemberCount()));
	}

	/**
	 * Iterates over the group and executes procedure on each member
	 * @param procedure the prodecure to be executed on each member.<br>
	 *            If executing the procedure on a member returns {@code true}, the loop continues to the next member, otherwise it breaks the loop
	 * @return {@code true} if the procedure executed correctly, {@code false} if the loop was broken prematurely
	 */
	public boolean forEachMember(IL2Procedure<L2PcInstance> procedure)
	{
		for(L2PcInstance player : getMembers())
		{
			if(!procedure.execute(player))
			{
				return false;
			}
		}
		return true;
	}
}
