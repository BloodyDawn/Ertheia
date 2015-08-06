package dwo.scripts.ai.zone;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.05.12
 * Time: 22:21
 */

public class TowerOfInsolence extends Quest
{
	// Монстры
	private static final int ВоинВладимира = 23069;
	private static final int ВоинРаджуоса = 23070;

	// Минибоссы
	private static final int ВладимирРазоритель = 25809;
	private static final int РаджуосРазоритель = 25811;

	public TowerOfInsolence()
	{
		addKillId(ВоинВладимира, ВоинРаджуоса);
	}

    public static void main(String[] args)
    {
        new TowerOfInsolence();
    }
    
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(Rnd.getChance(5))
		{
			if(npc.getNpcId() == ВоинВладимира)
			{
				L2Npc mob = addSpawn(ВладимирРазоритель, npc, true, 300000);
				mob.getAttackable().attackCharacter(killer);
			}
			else if(npc.getNpcId() == ВоинРаджуоса)
			{
				L2Npc mob = addSpawn(РаджуосРазоритель, npc, true, 300000);
				mob.getAttackable().attackCharacter(killer);
			}
		}
		return super.onKill(npc, killer, isPet);
	}
}