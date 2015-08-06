package dwo.scripts.npc.town;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 17.05.12
 * Time: 23:37
 */

public class FortuneTelling extends Quest
{
	private static final int COST = 1000;

	private static final int NPC = 32616;

	public FortuneTelling()
	{
		addAskId(NPC, -2013);
	}

	public static void main(String[] args)
	{
		new FortuneTelling();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 1)
		{
			if(player.getInventory().getAdenaCount() >= COST)
			{
				int rndInt = Rnd.get(386);
				int rndNpcString = 1800309 + rndInt;
				player.getInventory().reduceAdena(ProcessType.NPC, COST, player, npc);
				String content = HtmCache.getInstance().getHtm(player.getLang(), "default/fortune_teller_minne003.htm");
				content = content.replace("<?fortune_number?>", "<fstring>" + rndNpcString + "</fstring>");
				return content;
			}
			else
			{
				return "fortune_teller_minne004.htm";
			}

		}
		return null;
	}
}