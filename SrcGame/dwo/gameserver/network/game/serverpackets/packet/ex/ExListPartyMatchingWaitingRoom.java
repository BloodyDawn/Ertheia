package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.PartyMatchWaitingList;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Gnacik
 */

public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket
{
	private List<PartyMatchingWaitingInfo> _waitingList = Collections.emptyList();
	private int _fullSize;

	public ExListPartyMatchingWaitingRoom(L2PcInstance player, int page, int minLevel, int maxLevel, int[] classes)
	{
		int first = page - 1 << 6;
		int firstNot = page << 6;
		int i = 0;

		List<L2PcInstance> temp = PartyMatchWaitingList.getInstance().getWaitingList(minLevel, maxLevel, classes);
		_fullSize = temp.size();

		_waitingList = new ArrayList<>(_fullSize);
		for(L2PcInstance pc : temp)
		{
			if(i < first || i >= firstNot)
			{
				continue;
			}

			_waitingList.add(new PartyMatchingWaitingInfo(pc));
			i++;
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_fullSize);
		writeD(_waitingList.size());
		for(PartyMatchingWaitingInfo waiting_info : _waitingList)
		{
			writeS(waiting_info.name);
			writeD(waiting_info.classId);
			writeD(waiting_info.level);
			writeD(waiting_info.loc);
			writeD(waiting_info.instanceReuses.size());
			waiting_info.instanceReuses.keySet().forEach(this::writeD);
		}
	}

	static class PartyMatchingWaitingInfo
	{
		public final int classId;
		public final int level;
		public final int loc;
		public final String name;
		public final Map<Integer, Long> instanceReuses;

		public PartyMatchingWaitingInfo(L2PcInstance member)
		{
			name = member.getName();
			classId = member.getActiveClassId();
			level = member.getLevel();
			loc = MapRegionManager.getInstance().getMapRegion(member.getLoc()).getBbs();
			instanceReuses = member.getInstanceReuses();
		}
	}
}