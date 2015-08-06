package dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch;

import dwo.gameserver.instancemanager.ClanSearchManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ClanSearchPlayerHolder;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeDraftListSearch;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 19:26
 */
public class RequestPledgeDraftListApply extends L2GameClientPacket
{
	private int _applyType;       // Согласие или отмена на регистрацию в списке ожидающих
	private ExPledgeDraftListSearch.ClanSearchListType _searchType;      // ESearchListType

	@Override
	protected void readImpl()
	{
		_applyType = readD();
		_searchType = ExPledgeDraftListSearch.ClanSearchListType.getType(readD());
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(player.getClan() != null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(4031));
			return;
		}

		//	if (player.isGM())
		//		player.sendMessage("RequestPledgeDraftListApply _unk: " + _applyType + " searchType: " + _searchType);

		switch(_applyType)
		{
			case 0:
				if(ClanSearchManager.getInstance().removeWaiter(player.getObjectId()))
				{
					player.sendPacket(SystemMessage.getSystemMessage(4040));
				}
				break;
			case 1:
				if(ClanSearchManager.getInstance().addPlayer(new ClanSearchPlayerHolder(player.getObjectId(), player.getName(), player.getLevel(), player.getBaseClassId(), _searchType)))
				{
					player.sendPacket(SystemMessage.getSystemMessage(4043));
				}
				else
				{
					SystemMessage message = SystemMessage.getSystemMessage(4038);
					player.sendPacket(message);
				}
				break;
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:EA RequestPledgeDraftListApply";
	}
}
