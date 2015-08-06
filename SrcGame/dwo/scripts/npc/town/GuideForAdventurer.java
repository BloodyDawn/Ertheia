package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 25.09.11
 * Time: 16:14
 */

public class GuideForAdventurer extends Quest
{
	// Вещи Поддержки Путешественника
	private static final int NewbieItem = 32241;

	// Помощник путешественников
	private static final int NewbieGuide = 33463;

	public GuideForAdventurer()
	{
		addAskId(NewbieGuide, -484);
	}

	public static void main(String[] args)
	{
		new GuideForAdventurer();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -484)
		{
			if(reply == 1)
			{
				if(!player.getVariablesController().get(getClass().getSimpleName(), Boolean.class, false))
				{

					player.addItem(ProcessType.NPC, NewbieItem, 1, npc, true);
					player.getVariablesController().set(getClass().getSimpleName(), true);
				}

				int rndPage = Rnd.get(1, 322);
				String zeroCount = "";
				if(rndPage < 10)
				{
					zeroCount = "00";
				}
				else if(rndPage < 100)
				{
					zeroCount = "0";
				}
				return npc.getServerName() + zeroCount + rndPage + ".htm";
			}
		}
		return null;
	}
}