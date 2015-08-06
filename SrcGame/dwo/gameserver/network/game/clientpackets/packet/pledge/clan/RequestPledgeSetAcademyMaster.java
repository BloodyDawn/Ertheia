package dwo.gameserver.network.game.clientpackets.packet.pledge.clan;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class RequestPledgeSetAcademyMaster extends L2GameClientPacket
{
	private String _currPlayerName;
	private int _set; // 1 set, 0 delete
	private String _targetPlayerName;

	@Override
	protected void readImpl()
	{
		_set = readD();
		_currPlayerName = readS();
		_targetPlayerName = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		L2Clan clan = activeChar.getClan();
		if(clan == null)
		{
			return;
		}

		if((activeChar.getClanPrivileges() & L2Clan.CP_CL_APPRENTICE) != L2Clan.CP_CL_APPRENTICE)
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_DISMISS_AN_APPRENTICE);
			return;
		}

		L2ClanMember currentMember = clan.getClanMember(_currPlayerName);
		L2ClanMember targetMember = clan.getClanMember(_targetPlayerName);
		if(currentMember == null || targetMember == null)
		{
			return;
		}

		L2ClanMember apprenticeMember;
		L2ClanMember sponsorMember;
		if(currentMember.getPledgeType() == L2Clan.SUBUNIT_ACADEMY)
		{
			apprenticeMember = currentMember;
			sponsorMember = targetMember;
		}
		else
		{
			apprenticeMember = targetMember;
			sponsorMember = currentMember;
		}

		L2PcInstance apprentice = apprenticeMember.getPlayerInstance();
		L2PcInstance sponsor = sponsorMember.getPlayerInstance();

		SystemMessage sm = null;
		if(_set == 0)
		{
			// test: do we get the current sponsor & apprentice from this packet or no?
			if(apprentice != null)
			{
				apprentice.setSponsor(0);
			}
			else // offline
			{
				apprenticeMember.initApprenticeAndSponsor(0, 0);
			}

			if(sponsor != null)
			{
				sponsor.setApprentice(0);
			}
			else // offline
			{
				sponsorMember.initApprenticeAndSponsor(0, 0);
			}

			apprenticeMember.saveApprenticeAndSponsor(0, 0);
			sponsorMember.saveApprenticeAndSponsor(0, 0);

			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_CLAN_MEMBER_C1_APPRENTICE_HAS_BEEN_REMOVED);
		}
		else
		{
			if(apprenticeMember.getSponsor() != 0 || sponsorMember.getApprentice() != 0 || apprenticeMember.getApprentice() != 0 || sponsorMember.getSponsor() != 0)
			{
				activeChar.sendMessage("Remove previous connections first.");
				return;
			}
			if(apprentice != null)
			{
				apprentice.setSponsor(sponsorMember.getObjectId());
			}
			else // offline
			{
				apprenticeMember.initApprenticeAndSponsor(0, sponsorMember.getObjectId());
			}

			if(sponsor != null)
			{
				sponsor.setApprentice(apprenticeMember.getObjectId());
			}
			else // offline
			{
				sponsorMember.initApprenticeAndSponsor(apprenticeMember.getObjectId(), 0);
			}

			// saving to database even if online, since both must match
			apprenticeMember.saveApprenticeAndSponsor(0, sponsorMember.getObjectId());
			sponsorMember.saveApprenticeAndSponsor(apprenticeMember.getObjectId(), 0);

			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HAS_BEEN_DESIGNATED_AS_APPRENTICE_OF_CLAN_MEMBER_S1);
		}
		sm.addString(sponsorMember.getName());
		sm.addString(apprenticeMember.getName());
		if(!sponsor.equals(activeChar) && !sponsor.equals(apprentice))
		{
			activeChar.sendPacket(sm);
		}
		if(sponsor != null)
		{
			sponsor.sendPacket(sm);
		}
		if(apprentice != null)
		{
			apprentice.sendPacket(sm);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:12 RequestPledgeSetAcademyMaster";
	}
}