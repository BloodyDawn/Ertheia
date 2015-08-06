package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.serverpackets.packet.gmview.ExGMViewQuestItemList;
import dwo.gameserver.network.game.serverpackets.packet.gmview.GMHennaInfo;
import dwo.gameserver.network.game.serverpackets.packet.gmview.GMViewCharacterInfo;
import dwo.gameserver.network.game.serverpackets.packet.gmview.GMViewItemList;
import dwo.gameserver.network.game.serverpackets.packet.gmview.GMViewPledgeInfo;
import dwo.gameserver.network.game.serverpackets.packet.gmview.GMViewQuestInfo;
import dwo.gameserver.network.game.serverpackets.packet.gmview.GMViewSkillInfo;
import dwo.gameserver.network.game.serverpackets.packet.gmview.GMViewWarehouseWithdrawList;

public class RequestGMCommand extends L2GameClientPacket
{
	private String _targetName;
	private int _command;

	@Override
	protected void readImpl()
	{
		_targetName = readS();
		_command = readD();
		//_unknown  = readD();
	}

	@Override
	protected void runImpl()
	{
		// prevent non gm or low level GMs from viewing player stuff
		if(!getClient().getActiveChar().isGM() || !getClient().getActiveChar().getAccessLevel().allowAltG())
		{
			return;
		}

		L2PcInstance player = WorldManager.getInstance().getPlayer(_targetName);

		L2Clan clan = ClanTable.getInstance().getClanByName(_targetName);

		// player name was incorrect?
		if(player == null && (clan == null || _command != 6))
		{
			return;
		}

		switch(_command)
		{
			case 1: // player status
				sendPacket(new GMViewCharacterInfo(player));
				sendPacket(new GMHennaInfo(player));
				break;
			case 2: // player clan
				if(player != null && player.getClan() != null)
				{
					sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				}
				break;
			case 3: // player skills
				sendPacket(new GMViewSkillInfo(player));
				break;
			case 4: // player quests
				sendPacket(new GMViewQuestInfo(player));
				break;
			case 5: // player inventory
				sendPacket(new GMViewItemList(player));
				sendPacket(new ExGMViewQuestItemList(player));
				sendPacket(new GMHennaInfo(player));
				break;
			case 6: // player warehouse
				// gm warehouse view to be implemented
				if(player != null)
				{
					sendPacket(new GMViewWarehouseWithdrawList(player));
				}
				// clan warehouse
				else
				{
					sendPacket(new GMViewWarehouseWithdrawList(clan));
				}
				break;
		}
	}

	@Override
	public String getType()
	{
		return "[C] 6E RequestGMCommand";
	}
}