package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 11.03.13
 * Time: 20:28
 */

public class BlackJudge extends Quest
{
	private static final int NPC = 30981;

	public BlackJudge()
	{
		addAskId(NPC, -506);
	}

	public static void main(String[] args)
	{
		new BlackJudge();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -506)
		{
			int shillenLvl = player.getDeathPenaltyController().getDeathPenaltyLevel();
			if(reply == 1) // Ослабить дыхание Шилен 3 уровня и выше
			{
				if(shillenLvl > 0)
				{
					if(shillenLvl > 2)
					{
						player.getDeathPenaltyController().reduceDeathPenalty();
						if(player.getDeathPenaltyController().getDeathPenaltyLevel() == 0)
						{
							return "black_judge009.htm";
						}
					}
					else
					{
						return "black_judge002.htm";
					}
				}
				else
				{
					return "black_judge009.htm";
				}
			}
		}
		return null;
	}
}
