package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.datatables.xml.ObsceneFilterTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.network.game.components.SystemMessageId;

public class RequestGiveNickName extends L2GameClientPacket
{
	private String _target;
	private String _title;

	@Override
	protected void readImpl()
	{
		_target = readS();
		_title = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(ObsceneFilterTable.getInstance().isObsceneWord(_title))
		{
			activeChar.sendMessage("Ваш ник или титул содержит нецензурную лексику. Выберите что-нибудь более достойное.");
			return;
		}

		// Noblesse can bestow a title to themselves
		if(activeChar.isNoble() && _target.matches(activeChar.getName()))
		{
			activeChar.setTitle(_title);
			activeChar.sendPacket(SystemMessageId.TITLE_CHANGED);
			activeChar.broadcastTitleInfo();
		}
		//Can the player change/give a title?
		else if((activeChar.getClanPrivileges() & L2Clan.CP_CL_GIVE_TITLE) == L2Clan.CP_CL_GIVE_TITLE)
		{
			if(activeChar.getClan().getLevel() < 3)
			{
				activeChar.sendPacket(SystemMessageId.CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE);
				return;
			}

			L2ClanMember member1 = activeChar.getClan().getClanMember(_target);
			if(member1 != null)
			{
				L2PcInstance member = member1.getPlayerInstance();
				if(member != null)
				{
					//is target from the same clan?
					member.setTitle(_title);
					member.sendPacket(SystemMessageId.TITLE_CHANGED);
					member.broadcastTitleInfo();
				}
				else
				{
					activeChar.sendMessage("Target needs to be online to get a title");
				}
			}
			else
			{
				activeChar.sendMessage("Target does not belong to your clan");
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] 55 RequestGiveNickName";
	}
}
