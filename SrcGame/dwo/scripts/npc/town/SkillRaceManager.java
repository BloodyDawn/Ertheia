package dwo.scripts.npc.town;

import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.AcquireSkillType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.acquire.ExAcquirableSkillListByClass;

import java.util.Collection;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.11.12
 * Time: 22:02
 */

public class SkillRaceManager extends Quest
{
	private static final int NPC = 33880;

	public SkillRaceManager()
	{
		addAskId(NPC, -707);
		addLearnSkillId(NPC);
	}

	public static void main(String[] args)
	{
		new SkillRaceManager();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -707)
		{
			switch(reply)
			{
				case 1: // Получить Откровение Хаоса
					onLearnSkill(npc, player);
					break;
				case 2: // Забыть Откровение Хаоса
					if(player.getAdenaCount() < 100000000 || !player.isAwakened())
					{
						return "orim_normal001.htm"; // TODO: HTML
					}
					boolean hasSkillsToRemove = false;
					for(L2SkillLearn skillLearn : SkillTreesData.getInstance().getRaceActiveSkills(player.isSubClassActive()).values())
					{
						L2Skill skill = player.getKnownSkill(skillLearn.getSkillId());
						if(skill != null)
						{
							player.removeSkill(skill);
							hasSkillsToRemove = true;
						}
					}
					if(hasSkillsToRemove)
					{
						player.reduceAdena(ProcessType.NPC, 100000000, npc, true);
						if(player.isSubClassActive())
						{
							player.addItem(ProcessType.NPC, 37375, 2, npc, true);
						}
						else
						{
							player.addItem(ProcessType.NPC, 37374, 2, npc, true);
						}
					}
					return "orim_normal001.htm";
			}
		}
		return null;
	}

	@Override
	public String onLearnSkill(L2Npc npc, L2PcInstance player)
	{
		if(!player.isAwakened())
		{
			return "orim_normal001.htm"; // TODO верный диалог ( нельзя выучить скилы без 4 профы )
		}

		Collection<L2SkillLearn> skillLearns = SkillTreesData.getInstance().getRaceActiveSkills(player.isSubClassActive()).values();
		ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireSkillType.Race);
		int knownSkillCount = 0;
		for(L2SkillLearn skillLearn : skillLearns)
		{
			L2Skill skill = player.getKnownSkill(skillLearn.getSkillId());
			if(skill != null)
			{
				knownSkillCount++;
			}
			else
			{
				asl.addSkill(skillLearn.getSkillId(), skillLearn.getSkillLevel(), skillLearn.getSkillLevel(), skillLearn.getLevelUpSp(), 1);
			}
		}

		// Можно выучить только 2 расовых скилла
		if(knownSkillCount >= 2)
		{
			player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
			return null;
		}

		// Шлем список скиллов на изучение
		if(asl.getSkillCount() > 0)
		{
			player.sendPacket(asl);
		}

		return null;
	}
}