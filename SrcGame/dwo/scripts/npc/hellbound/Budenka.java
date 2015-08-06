package dwo.scripts.npc.hellbound;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

public class Budenka extends Quest
{
	private static final int BUDENKA = 32294;
	private static final int NORMAL_CERT = 9850;
	private static final int STANDART_CERT = 9851;
	private static final int PREMIUM_CERT = 9852;

	public Budenka()
	{
		addFirstTalkId(BUDENKA);
	}

	public static void main(String[] args)
	{
		new Budenka();
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(player.getItemsCount(NORMAL_CERT) < 1 && player.getItemsCount(STANDART_CERT) < 1 && player.getItemsCount(PREMIUM_CERT) < 1)
		{
			return "caravan_budenka001.htm";
		}
		if(player.getItemsCount(NORMAL_CERT) >= 1 && player.getItemsCount(STANDART_CERT) < 1 && player.getItemsCount(PREMIUM_CERT) < 1)
		{
			return "caravan_budenka002.htm";
		}
		if(player.getItemsCount(STANDART_CERT) >= 1 || player.getItemsCount(PREMIUM_CERT) >= 1)
		{
			return "caravan_budenka003.htm";
		}
		return null;
	}
}