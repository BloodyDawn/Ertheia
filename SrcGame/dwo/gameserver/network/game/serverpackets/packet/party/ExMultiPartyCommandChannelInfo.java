package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.model.player.formation.group.L2CommandChannel;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author chris_00
 */

public class ExMultiPartyCommandChannelInfo extends L2GameServerPacket
{
	private L2CommandChannel _channel;

	public ExMultiPartyCommandChannelInfo(L2CommandChannel channel)
	{
		_channel = channel;
	}

	@Override
	protected void writeImpl()
	{
		if(_channel == null)
		{
			return;
		}
		writeS(_channel.getLeader().getName()); // Владелец КК
		writeD(0); // TODO: Тип лута в КК (0 или 1)
		writeD(_channel.getMemberCount());

		writeD(_channel.getPartys().size());
		for(L2Party p : _channel.getPartys())
		{
			writeS(p.getLeader().getName()); // Имя лидера группы
			writeD(p.getLeaderObjectId()); // ObjectID лидера группы
			writeD(p.getMemberCount()); // Количество человек в группе
		}
	}
}
