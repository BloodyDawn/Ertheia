package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ConfirmDlg;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;

/**
 * @author BiTi, Sami, ANZO
 */

public class SummonFriend implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {L2SkillType.SUMMON_FRIEND};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!(activeChar instanceof L2PcInstance))
		{
			return;
		}

		boolean isMastersCall = skill.getId() == 23249;

		L2PcInstance activePlayer = activeChar.getActingPlayer();

		if(!isMastersCall && !activePlayer.getSummonFriendController().checkSummonerStatus())
		{
			return;
		}

		try
		{
			for(L2Character target : (L2Character[]) targets)
			{
				if(target == null || activeChar.equals(target) || !target.isPlayer())
				{
					continue;
				}

				if(isMastersCall) //Master's Call
				{
					L2Party party = target.getParty();
					if(party != null)
					{
						party.getMembers().stream().filter(partyMember -> !target.equals(partyMember)).forEach(partyMember -> partyMember.teleToLocation(target.getX(), target.getY(), target.getZ(), true));
					}
					else
					{
						activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
					}
				}
				else
				{
					if(skill.isClanSkill()) // Призыв Клана
					{
						if(!target.getActingPlayer().isInSameClan(activePlayer))
						{
							activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
							continue;
						}
						else if(!target.getActingPlayer().isInSameParty(activePlayer))
						{
							activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
							continue;
						}
					}

					L2PcInstance targetPlayer = target.getActingPlayer();

					if(!Util.checkIfInRange(500, activeChar, target, false))
					{
						if(!targetPlayer.getSummonFriendController().setSummoner(activePlayer, skill))
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ALREADY_SUMMONED);
							sm.addString(target.getName());
							activePlayer.sendPacket(sm);
						}

						ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
						confirm.addCharName(activeChar);
						confirm.addZoneName(activeChar.getX(), activeChar.getY(), activeChar.getZ());
						confirm.addTime(30000);
						confirm.addRequesterId(activePlayer.getObjectId());
						target.sendPacket(confirm);
						// targetPlayer.getSummonFriendController().teleport();
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
