package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 07.06.13
 * Time: 17:12
 */

public class Kerenas extends Quest
{
	private static final int NPC = 31281;

	public Kerenas()
	{
		addAskId(NPC, -1021);
		addAskId(NPC, -41021); // TODO: Хз что за оно
	}

	public static void main(String[] args)
	{
		new Kerenas();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == NPC)
		{
			if(ask == -1021)
			{
				if(reply == 1)
				{
					return "priest_cerenas002.htm";
				}
				else if(reply == 2)
				{
					// TODO: Телепорт в инстанс по нублу на последней стадии? http://www.youtube.com/watch?v=bIn5u5Nv5Eg
				}
			}
			else if(ask == 41021)
			{
				if(reply == 1)
				{
					// TODO: Получить Элитное Украшение еще раз
				}
			}
		}
		return null;
	}
}