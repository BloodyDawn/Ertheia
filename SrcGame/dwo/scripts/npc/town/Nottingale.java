package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.serverpackets.RadarControl;
import dwo.scripts.quests._10273_GoodDayToFly;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 15.09.12
 * Time: 20:16
 */

public class Nottingale extends Quest
{
	private static final int NPC = 32627;

	public Nottingale()
	{
		addAskId(NPC, 255);
	}

	public static void main(String[] args)
	{
		new Nottingale();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(reply)
		{
			case 1:
				QuestState qs = player.getQuestState(_10273_GoodDayToFly.class);
				if(qs == null || !qs.isCompleted())
				{
					player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
					player.sendPacket(new RadarControl(0, 2, -184545, 243120, 1581));
					return "mage_notingale002.htm";
				}
				else
				{
					return "mage_notingale003.htm";
				}
			case 2:
				return "mage_notingale004.htm";
			case 3:
				return "mage_notingale005.htm";
			case 21:
				player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
				player.sendPacket(new RadarControl(0, 2, -192361, 254528, 3598));
				return "mage_notingale0041.htm";
			case 22:
				player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
				player.sendPacket(new RadarControl(0, 2, -174600, 219711, 4424));
				return "mage_notingale0042.htm";
			case 23:
				player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
				player.sendPacket(new RadarControl(0, 2, -181989, 208968, 4424));
				return "mage_notingale0043.htm";
			case 24:
				player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
				player.sendPacket(new RadarControl(0, 2, -252898, 235845, 5343));
				return "mage_notingale0044.htm";
			case 31:
				player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
				player.sendPacket(new RadarControl(0, 2, -212819, 209813, 4288));
				return "mage_notingale0051.htm";
			case 32:
				player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
				player.sendPacket(new RadarControl(0, 2, -246899, 251918, 4352));
				return "mage_notingale0052.htm";

		}
		return null;
	}
}