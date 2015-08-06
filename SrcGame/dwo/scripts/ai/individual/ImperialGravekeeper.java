package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.06.13
 * Time: 15:22
 */

public class ImperialGravekeeper extends Quest
{
	private static final int NPC = 27181;
	private static final int NPC_MINION = 27180;

	private static final SkillHolder SKILL = new SkillHolder(4080, 1);

	public ImperialGravekeeper()
	{
		addSpawnId(NPC);
		addAttackId(NPC);
	}

	public static void main(String[] args)
	{
		new ImperialGravekeeper();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(!npc.isInsideZone(L2Character.ZONE_PEACE))
		{
			if(npc.getCurrentHp() / npc.getMaxHp() * 100 <= npc.getAiVarInt("i_ai1"))
			{
				if(npc.getAiVarInt("i_ai0") == 1 || npc.getAiVarInt("i_ai0") == 3)
				{
					npc.setAiVar("i_ai0", "2");
					npc.teleToLocation(179520, 6464, -2706, true);
				}
				else
				{
					npc.setAiVar("i_ai0", "3");
					npc.teleToLocation(179520, 6464, -2706, true);
				}
			}
			if(SKILL.getSkill().getMpConsume() < npc.getCurrentMp())
			{
				SKILL.getSkill().getEffects(npc, npc);
			}
			if(npc.getAiVarInt("i_ai1") == 50)
			{
				npc.setAiVar("i_ai1", "30");
			}
			else if(npc.getAiVarInt("i_ai1") == 30)
			{
				npc.setAiVar("i_ai1", "-1");
			}
		}
		if(npc.getCurrentHp() / npc.getMaxHp() * 100 <= npc.getAiVarInt("i_ai2"))
		{
			if(npc.getAiVarInt("i_ai2") == 80)
			{
				npc.setAiVar("i_ai2", "40");
			}
			else if(npc.getAiVarInt("i_ai2") == 40)
			{
				npc.setAiVar("i_ai2", "20");
			}
			else
			{
				npc.setAiVar("i_ai2", "-1");
			}
			addSpawn(NPC_MINION, npc.getLoc(), true);
			addSpawn(NPC_MINION, npc.getLoc(), true);
			addSpawn(NPC_MINION, npc.getLoc(), true);
			addSpawn(NPC_MINION, npc.getLoc(), true);
		}
		if(npc.getCurrentHp() / npc.getMaxHp() * 100 > 50)
		{
			npc.setAiVar("i_ai1", "50");
		}
		else if(npc.getCurrentHp() / npc.getMaxHp() * 100 > 30)
		{
			npc.setAiVar("i_ai1", "30");
		}
		if(npc.getCurrentHp() / npc.getMaxHp() * 100 > 80)
		{
			npc.setAiVar("i_ai2", "80");
		}
		else if(npc.getCurrentHp() / npc.getMaxHp() * 100 > 40)
		{
			npc.setAiVar("i_ai2", "40");
		}
		else if(npc.getCurrentHp() / npc.getMaxHp() * 100 > 20)
		{
			npc.setAiVar("i_ai2", "20");
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setAiVar("i_ai0", "1");
		npc.setAiVar("i_ai1", "50");
		npc.setAiVar("i_ai2", "80");
		return super.onSpawn(npc);
	}
}