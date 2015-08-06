package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: keiichi
 * Date: 08.02.13
 * Time: 0:10
 */
public class Reyon extends Quest
{
	private static final int NPC = 33834;
	private static final int price = 200000;

	private final int[][] _Buffs = {
		{11517, 11518, 11519, 11520, 11521, 11522, 11523}, {11517, 11518, 11519, 11520, 11521, 11522, 11524},
		{11517, 11518, 11519, 11520, 11521, 11522, 11525}
	};

	public Reyon()
	{
		addFirstTalkId(NPC);
		addAskId(NPC, -4300);
	}

	public static void main(String[] args)
	{
		new Reyon();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 1)
		{
			if(!player.reduceAdena(ProcessType.NPC, price, player.getLastFolkNPC(), true))
			{
				return null;
			}

			for(int skillId : _Buffs[0])
			{
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);

				if(skill != null)
				{
					skill.getEffects(npc, player);
				}
			}
		}
		else if(reply == 2)
		{
			if(!player.reduceAdena(ProcessType.NPC, price, player.getLastFolkNPC(), true))
			{
				return null;
			}

			for(int skillId : _Buffs[1])
			{
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);

				if(skill != null)
				{
					skill.getEffects(npc, player);
				}
			}
		}
		else if(reply == 3)
		{
			if(!player.reduceAdena(ProcessType.NPC, price, player.getLastFolkNPC(), true))
			{
				return null;
			}

			for(int skillId : _Buffs[2])
			{
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);

				if(skill != null)
				{
					skill.getEffects(npc, player);
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "reyon.htm";
	}
}
