package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.ClanInfo;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pledge.AllianceInfo;

public class RequestAllyInfo extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		SystemMessage sm;
		int allianceId = activeChar.getAllyId();
		if(allianceId > 0)
		{
			AllianceInfo ai = new AllianceInfo(allianceId);
			activeChar.sendPacket(ai);

			// send for player
			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_INFO_HEAD);
			activeChar.sendPacket(sm);

			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_NAME_S1);
			sm.addString(ai.getName());
			activeChar.sendPacket(sm);

			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1);
			sm.addString(ai.getLeaderC());
			sm.addString(ai.getLeaderP());
			activeChar.sendPacket(sm);

			sm = SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
			sm.addNumber(ai.getOnline());
			sm.addNumber(ai.getTotal());
			activeChar.sendPacket(sm);

			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_CLAN_TOTAL_S1);
			sm.addNumber(ai.getAllies().length);
			activeChar.sendPacket(sm);

			sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_HEAD);
			for(ClanInfo aci : ai.getAllies())
			{
				activeChar.sendPacket(sm);

				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_NAME_S1);
				sm.addString(aci.getClan().getName());
				activeChar.sendPacket(sm);

				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEADER_S1);
				sm.addString(aci.getClan().getLeaderName());
				activeChar.sendPacket(sm);

				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEVEL_S1);
				sm.addNumber(aci.getClan().getLevel());
				activeChar.sendPacket(sm);

				sm = SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
				sm.addNumber(aci.getOnline());
				sm.addNumber(aci.getTotal());
				activeChar.sendPacket(sm);

				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_SEPARATOR);
			}

			sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_FOOT);
			activeChar.sendPacket(sm);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 8E RequestAllyInfo";
	}
}
