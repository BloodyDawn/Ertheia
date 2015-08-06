package dwo.scripts.npc.town;

import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.AcquireSkillType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.acquire.ExAcquirableSkillListByClass;

import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 04.10.12
 * Time: 23:15
 */

public class OfficerTolonis extends Quest
{
	private static final int NPC = 32611;

	public OfficerTolonis()
	{
		addLearnSkillId(NPC);
	}

	public static void main(String[] args)
	{
		new OfficerTolonis();
	}

	@Override
	public String onLearnSkill(L2Npc npc, L2PcInstance player)
	{
		List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableCollectSkills(player);
		ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireSkillType.Collect);

		int counts = 0;

		for(L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getSkillId(), s.getSkillLevel());

			if(sk != null)
			{
				counts++;
				asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), 0, 1);
			}
		}

		if(counts == 0) // Нет больше скиллов для изучения
		{
			int minLevel = SkillTreesData.getInstance().getMinLevelForNewSkill(player, SkillTreesData.getInstance().getCollectSkillTree());
			if(minLevel > 0)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
				sm.addNumber(minLevel);
				player.sendPacket(sm);
			}
			else
			{
				player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
			}
		}
		else
		{
			player.sendPacket(asl);
		}
		return null;
	}
}