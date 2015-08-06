package dwo.scripts.npc.fort;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.01.13
 * Time: 18:12
 */

public class FortCitizen extends Quest
{
	private static final int[] Archers = {
		35661, 35692, 35730, 35761, 35799, 35830, 35861, 35899, 35930, 35968, 36006, 36037, 36075, 36113, 36144, 36175,
		36213, 36251, 36289, 36320, 36358
	};
	private static final int[] Officers = {
		35664, 35696, 35733, 35765, 35802, 35833, 35865, 35902, 35934, 35972, 36009, 36041, 36079, 36116, 36147, 36179,
		36217, 36255, 36292, 36324, 36362
	};
	private static final int[] Engineers = {
		35663, 35695, 35732, 35764, 35801, 35832, 35864, 35901, 35933, 35971, 36008, 36040, 36078, 36115, 36146, 36178,
		36216, 36254, 36291, 36323, 36361
	};

	public FortCitizen()
	{
		addFirstTalkId(Archers);
		addFirstTalkId(Officers);
		addFirstTalkId(Engineers);
	}

	public static void main(String[] args)
	{
		new FortCitizen();
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(ArrayUtils.contains(Archers, npc.getNpcId()))
		{
			return "fortress_archer.htm";
		}
		if(ArrayUtils.contains(Officers, npc.getNpcId()))
		{
			return "fortress_officer.htm";
		}
		if(ArrayUtils.contains(Engineers, npc.getNpcId()))
		{
			return "fortress_engineer.htm";
		}
		return null;
	}
}
