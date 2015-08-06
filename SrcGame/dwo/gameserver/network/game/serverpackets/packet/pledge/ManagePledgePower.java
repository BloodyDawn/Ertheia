package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ManagePledgePower extends L2GameServerPacket
{
	private int _action;
	private L2Clan _clan;
	private int _rank;
	private int _privs;

	public ManagePledgePower(L2Clan clan, int action, int rank)
	{
		_clan = clan;
		_action = action;
		_rank = rank;
	}

	@Override
	protected void writeImpl()
	{
		if(_action == 1)
		{
			_privs = _clan.getRankPrivs(_rank);
		}
		else
		{
			return;
			/*
		    if (L2World.getInstance().findObject(_clanId) == null)
                return;

			privs = ((L2PcInstance)L2World.getInstance().findObject(_clanId)).getClanPrivileges();
			 */
		}

		writeD(0);
		writeD(0);
		writeD(_privs);
	}
}
