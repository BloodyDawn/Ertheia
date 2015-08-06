package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.acquire.AcquireSkillList;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 23.07.11
 * Time: 20:57
 */

public class ChangeSubClass implements ISkillHandler
{
	private static final L2Skill skillDebuff = SkillTable.getInstance().getInfo(1570, 1);

	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.CHANGE_SUBCLASS
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!activeChar.isPlayer())
		{
			return;
		}

		L2PcInstance player = (L2PcInstance) activeChar;

		if(!player.getFloodProtectors().getSubclass().tryPerformAction(FloodAction.CLASS_CHANGE))
		{
			_log.log(Level.WARN, "ChangeSubClass: Player " + player.getName() + " has performed a subclass change too fast");
			return;
		}
		if(!player.isInventoryUnder(0.8, true))
		{
			player.sendPacket(SystemMessageId.getSystemMessageId(2033));
			return;
		}
		if(player.isOverloaded())
		{
			player.sendPacket(SystemMessageId.getSystemMessageId(1894));
			return;
		}
		if(!player.getPets().isEmpty())
		{
			player.sendPacket(SystemMessageId.getSystemMessageId(1904));
			return;
		}
			/* Узнать где еще менять сабы нельзя */
		if(GrandBossManager.getInstance().checkIfInZone(player))
		{
			// Нельзя менять саб-класс в эпик зонах
			// TODO: Offlike message
			return;
		}
		if(player.isInsideZone(L2Character.ZONE_JAIL) || player.isInsideZone(L2Character.ZONE_JUMP) || player.isInsideZone(L2Character.ZONE_PVP) || player.isInsideZone(L2Character.ZONE_SIEGE))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
			return;
		}
		if(player.isInDuel() || player.isInCombat() || player.getPvPFlagController().isFlagged())
		{
			player.sendMessage("Смена подкласса недоступна в состоянии боя или во время дуели."); //TODO: System Message
			return;
		}
		if(player.getFirstEffect(1570) != null)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_CHANGE_CLASS_BECAUSE_OF_IDENTIFY_CRISIS);
			return;
		}

		int activeClassId = player.getActiveClassId();

		switch(skill.getId())
		{
			case 1566:
				player.setActiveClass(0);
				break;
			case 1567:
				player.setActiveClass(1);
				break;
			case 1568:
				player.setActiveClass(2);
				break;
			case 1569:
				player.setActiveClass(3);
				break;
		}
		player.sendPacket(new AcquireSkillList(player));
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCESSFULLY_SWITCHED_S1_TO_S2).addClassId(activeClassId).addClassId(player.getActiveClassId()));
		skillDebuff.getEffects(player, player);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
