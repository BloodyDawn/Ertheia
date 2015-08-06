package dwo.scripts.npc.instance;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.scripts.instances.Fortuna;

/**
 * NPC для входа в Фортуну.
 *
 * @author Yorie
 */
public class Izshael extends Quest
{
	private static final int IZSHAEL = 32894;

	public Izshael()
	{
		addAskId(IZSHAEL, 111);
	}

	public static void main(String[] args)
	{
		new Izshael();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		Fortuna.EnterInstanceResult reason;
		switch(reply)
		{
			// Начало
			case 1:
				reason = Fortuna.getInstance().enterInstance(player);

				if(reason == Fortuna.EnterInstanceResult.TOO_LOW_LEVEL)
				{
					return "izshael004.htm";
				}
				break;
			// Повторный вход
			case 2:
				reason = Fortuna.getInstance().reEnterInstance(player);

				if(reason != Fortuna.EnterInstanceResult.OK)
				{
					return "izshael005.htm";
				}
				break;
		}

		return null;
	}
}