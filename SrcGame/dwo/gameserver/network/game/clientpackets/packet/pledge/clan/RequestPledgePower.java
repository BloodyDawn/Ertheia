package dwo.gameserver.network.game.clientpackets.packet.pledge.clan;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.pledge.ManagePledgePower;

public class RequestPledgePower extends L2GameClientPacket
{
	private int _rank;
	private int _action;
	private int _privs;

	@Override
	protected void readImpl()
	{
		_rank = readD();
		_action = readD();
		_privs = _action == 2 ? readD() : 0;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(_action == 2)
		{
			if(player.isClanLeader())
			{
				if(_rank == 9)
				{
					//The rights below cannot be bestowed upon Academy members:
					//Join a clan or be dismissed
					//Title management, crest management, master management, level management,
					//bulletin board administration
					//Clan war, right to dismiss, set functions
					//ClanHallAuctionEngine, manage taxes, attack/defend registration, mercenary management
					//=> Leaves only CP_CL_VIEW_WAREHOUSE, CP_CH_OPEN_DOOR, CP_CS_OPEN_DOOR?
					_privs = (_privs & L2Clan.CP_CL_VIEW_WAREHOUSE) + (_privs & L2Clan.CP_CH_OPEN_DOOR) + (_privs & L2Clan.CP_CS_OPEN_DOOR);
				}
				player.getClan().setRankPrivs(_rank, _privs);
			}
		}
		else
		{
			player.sendPacket(new ManagePledgePower(getClient().getActiveChar().getClan(), _action, _rank));
		}
	}

	@Override
	public String getType()
	{
		return "[C] C0 RequestPledgePower";
	}
}