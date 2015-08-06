package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 21.07.12
 * Time: 16:19
 * Сами мандрагоры просто мобы которые ничего не делают совсем, когда их бьешь
 * из них рандомно выползает один моб 23210 или 23211 которые агрятся на игрока и сама мандрагора исчезает "самоуничтожается" :).
 */

public class Mandragora extends Quest
{
	private static final int МандрагораФ = 23240;
	private static final int МандрагораМ = 23241;
	private static final int Мандрагора_Скорби = 23210;
	private static final int Молитвенная_Мандрагора = 23211;

	public Mandragora()
	{
		addAttackId(МандрагораФ, МандрагораМ);
		addSpawnId(МандрагораФ, МандрагораМ);
		onSpawnRerun(МандрагораФ, МандрагораМ);
	}

	public static void main(String[] args)
	{
		new Mandragora();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if(npc.getNpcId() == МандрагораФ || npc.getNpcId() == МандрагораМ)
		{
			if(npc.getCustomInt() == 0)
			{
				if(Rnd.getChance(50))
				{
					L2Character attacker = isPet ? player.getPets().getFirst() : player;

					L2Attackable mc = (L2Attackable) addSpawn(Мандрагора_Скорби, npc.getX() + 50, npc.getY() + 50, npc.getZ() + 10, npc.getHeading(), false, 0, false);
					mc.attackCharacter(attacker);
					npc.setCustomInt(1);
					if(!npc.isDead())
					{
						npc.getLocationController().delete();
					}
					return super.onAttack(npc, player, damage, isPet);
				}
				else
				{
					L2Character attacker = isPet ? player.getPets().getFirst() : player;

					L2Attackable mc = (L2Attackable) addSpawn(Молитвенная_Мандрагора, npc.getX() + 50, npc.getY() + 50, npc.getZ() + 10, npc.getHeading(), false, 0, false);
					mc.attackCharacter(attacker);
					npc.setCustomInt(1);
					if(!npc.isDead())
					{
						npc.getLocationController().delete();
					}
					return super.onAttack(npc, player, damage, isPet);
				}
			}
		}
		return super.onAttack(npc, player, damage, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setCustomInt(0);
		npc.setIsMortal(false);
		npc.setIsNoAttackingBack(true);
		return super.onSpawn(npc);
	}
}