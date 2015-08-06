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
 * Date: 26.11.12
 * Time: 20:29
 */

public class Fisherman extends Quest
{
	private static final int[] NPCs = {
		32348, 32007, 31989, 31697, 31696, 31579, 31578, 31577, 31576, 31575, 31574, 31573, 31572, 31571, 31570, 31569,
		31568, 31567, 31566, 31565, 31564, 31563, 31562
	};

	public Fisherman()
	{
		addAskId(NPCs, -404);
		addLearnSkillId(NPCs);
	}

	public static void main(String[] args)
	{
		new Fisherman();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -404)
		{
			return "no_fish_event001.htm"; // TODO
		}
		return null;
	}

	@Override
	public String onLearnSkill(L2Npc npc, L2PcInstance player)
	{
		List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableFishingSkills(player);
		ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireSkillType.Fishing);

		int count = 0;

		for(L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getSkillId(), s.getSkillLevel());

			if(sk == null)
			{
				continue;
			}

			count++;
			asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), s.getLevelUpSp(), 1);
		}

		if(count == 0)
		{
			int minLevel = SkillTreesData.getInstance().getMinLevelForNewSkill(player, SkillTreesData.getInstance().getFishingSkillTree());

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