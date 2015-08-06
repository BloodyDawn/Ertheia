package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.config.Config;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PledgeInfo extends L2GameServerPacket
{
	private L2Clan _clan;

	public PledgeInfo(L2Clan clan)
	{
		_clan = clan;
	}

	@Override
	protected void writeImpl()
	{
		writeD(Config.SERVER_ID);
		writeD(_clan.getClanId());
		writeS(_clan.getName());
		writeS(_clan.getAllyName());
	}
}
