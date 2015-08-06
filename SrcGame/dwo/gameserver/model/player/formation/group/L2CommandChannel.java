/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.player.formation.group;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.party.ExCloseMPCC;
import dwo.gameserver.network.game.serverpackets.packet.party.ExMPCCPartyInfoUpdate;
import dwo.gameserver.network.game.serverpackets.packet.party.ExOpenMPCC;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.arrays.IL2Procedure;
import javolution.util.FastList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class serves as a container for command channels
 * @author chris_00
 */

public class L2CommandChannel extends AbstractPlayerGroup
{
	private final List<L2Party> _parties;
	private L2PcInstance _commandLeader;
	private int _channelLvl;

	/**
	 * Create a new command channel and add the leader's party to it.
	 * @param leader the leader of this command channel
	 */
	public L2CommandChannel(L2PcInstance leader)
	{
		_commandLeader = leader;
		L2Party party = leader.getParty();
		_parties = new FastList<L2Party>().shared();
		_parties.add(party);
		_channelLvl = party.getLevel();
		party.setCommandChannel(this);
		party.broadcastMessage(SystemMessageId.COMMAND_CHANNEL_FORMED);
		party.broadcastPacket(new ExOpenMPCC());
	}

	/**
	 * Добавляет группу в Командный Канал
	 *
	 * @param party группа для добавления в КК
	 */
	public void addParty(L2Party party)
	{
		if(party == null)
		{
			return;
		}
		// Update the CCinfo for existing players
		broadcastPacket(new ExMPCCPartyInfoUpdate(party, 1));

		_parties.add(party);
		if(party.getLevel() > _channelLvl)
		{
			_channelLvl = party.getLevel();
		}
		party.setCommandChannel(this);
		party.broadcastMessage(SystemMessageId.JOINED_COMMAND_CHANNEL);
		party.broadcastPacket(new ExOpenMPCC());
	}

	/**
	 * Removes a Party from the Command Channel
	 *
	 * @param party
	 */
	public void removeParty(L2Party party)
	{
		if(party == null)
		{
			return;
		}

		_parties.remove(party);
		_channelLvl = 0;
		_parties.stream().filter(pty -> pty.getLevel() > _channelLvl).forEach(pty -> _channelLvl = pty.getLevel());
		party.setCommandChannel(null);
		party.broadcastPacket(new ExCloseMPCC());
		if(_parties.size() < 2)
		{
			broadcastMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED);
			disbandChannel();
		}
		else
		{
			// Update the CCinfo for existing players
			broadcastPacket(new ExMPCCPartyInfoUpdate(party, 0));
		}
	}

	/**
	 * disbands the whole Command Channel
	 */
	public void disbandChannel()
	{
		if(_parties != null && !_parties.isEmpty())
		{
			_parties.stream().filter(party -> party != null).forEach(this::removeParty);
			_parties.clear();
		}
	}

	/**
	 * @return list of Parties in Command Channel
	 */
	public List<L2Party> getPartys()
	{
		return _parties;
	}

	/**
	 * @return list of all Members in Command Channel
	 */
	@Override
	public List<L2PcInstance> getMembers()
	{
		List<L2PcInstance> members = new FastList<L2PcInstance>().shared();
		for(L2Party party : _parties)
		{
			members.addAll(party.getMembers());
		}
		return members;
	}

	/**
	 * @return the leader of the Command Channel
	 */
	@Override
	public L2PcInstance getLeader()
	{
		return _commandLeader;
	}

	/**
	 * @param leader sets the leader of the Command Channel
	 */
	@Override
	public void setLeader(L2PcInstance leader)
	{
		_commandLeader = leader;
		if(leader.getLevel() > _channelLvl)
		{
			_channelLvl = leader.getLevel();
		}
	}

	/**
	 * @return overall membercount of the Command Channel
	 */
	@Override
	public int getMemberCount()
	{
		int count = 0;
		for(L2Party party : _parties)
		{
			if(party != null)
			{
				count += party.getMemberCount();
			}
		}
		return count;
	}

	/**
	 * @return Level of CC
	 */
	@Override
	public int getLevel()
	{
		return _channelLvl;
	}

	@Override
	public boolean containsPlayer(L2PcInstance player)
	{
		if(_parties != null && !_parties.isEmpty())
		{
			for(L2Party party : _parties)
			{
				if(party.containsPlayer(player))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Iterates over CC without need to allocate any new list
	 */
	@Override
	public boolean forEachMember(IL2Procedure<L2PcInstance> procedure)
	{
		if(_parties != null && !_parties.isEmpty())
		{
			for(L2Party party : _parties)
			{
				if(!party.forEachMember(procedure))
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @param player игрок
	 * @param radius проверяемый радиус
	 * @return список игроков в группе, которые находятся в указанном радиусе
	 */
	public List<L2PcInstance> getMembersInRadius(L2PcInstance player, int radius)
	{
		List<L2PcInstance> temp = new ArrayList<>();
		for(L2Party party : _parties)
		{
			temp.addAll(party.getMembersInRadius(player, radius).stream().filter(partyMember -> Util.checkIfInRange(radius, player, partyMember, true)).collect(Collectors.toList()));
		}
		return temp;
	}

	/**
	 * @param obj
	 * @return {@code true} if proper condition for RaidWar
	 */
	public boolean meetRaidWarCondition(L2Object obj)
	{
		if(!(obj instanceof L2Character && ((L2Character) obj).isRaid()))
		{
			return false;
		}
		return getMemberCount() >= Config.LOOT_RAIDS_PRIVILEGE_CC_SIZE;
	}

	/**
	 * Возвращает количество партий в КК
	 * @return Int количество партий
	 */
	public int getPartyCount()
	{
		return _parties.size();
	}

	/**
	 * Check whether the leader of this command channel is the same as the leader of the specified command channel<br>
	 * (which essentially means they're the same group).
	 * @param cc the other command channel to check against
	 * @return {@code true} if this command channel equals the specified command channel, {@code false} otherwise
	 */
	public boolean equals(L2CommandChannel cc)
	{
		return getLeaderObjectId() == cc.getLeaderObjectId();
	}
}
