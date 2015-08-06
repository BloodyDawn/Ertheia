package dwo.scripts.npc.instance;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2CommandChannel;
import dwo.gameserver.model.world.quest.Quest;
import dwo.scripts.instances.RB_Octavis;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class Typia extends Quest
{
	private static final int TYPIA = 32892;

	public Typia()
	{
		addStartNpc(TYPIA);
		addFirstTalkId(TYPIA);
		addTalkId(TYPIA);
		addAskId(TYPIA, -2892);
	}

	public static void main(String[] args)
	{
		new Typia();
	}

	/**
	 * Проверка условий для входа в инстанс
	 *
	 * @param player Игрок.
	 * @param isExtreme Экстрим Октавис?
	 * @return
	 */
	private String checkEnterConditions(L2PcInstance player, boolean isExtreme)
	{
		if(player.isGM())
		{
			return null;
		}

		if(player.getParty() == null || player.getParty().getCommandChannel() == null)
		{
			return "orbis_typia006.htm";
		}

		L2CommandChannel channel = player.getParty().getCommandChannel();

		if(!channel.getLeader().equals(player))
		{
			return "orbis_typia007.htm";
		}

		if(channel.getMemberCount() > 49)
		{
			return "orbis_typia010.htm";
		}

		for(L2PcInstance member : channel.getMembers())
		{
			if(isExtreme && member.getLevel() < 97)
			{
				return "orbis_typia009.htm";
			}
			else if(!isExtreme && member.getLevel() < 95)
			{
				return "orbis_typia008.htm";
			}
		}

		return null;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(reply)
		{
			// Начало
			case 1:
				return "orbis_typia003.htm";
			// Обычный Октавис, описание
			case 2:
				return "orbis_typia004.htm";
			// Экстрим Октавис, описание
			case 3:
				return "orbis_typia005.htm";
			// Обычный Октавис
			case 11:
			{
				String message = checkEnterConditions(player, false);
				if(message != null)
				{
					return message;
				}

				if(((RB_Octavis) QuestManager.getInstance().getQuest("RB_Octavis")).enterInstance(player, false))
				{
					return "orbis_typia011.htm";
				}
			}
			// Экстрим Октавис
			case 12:
				String message = checkEnterConditions(player, true);
				if(message != null)
				{
					return message;
				}

				if(((RB_Octavis) QuestManager.getInstance().getQuest("RB_Octavis")).enterInstance(player, true))
				{
					return "orbis_typia011.htm";
				}
				// Продолжить битву с Октависом
			case 13:
				RB_Octavis.OctavisWorld world = InstanceManager.getInstance().getInstanceWorld(player, RB_Octavis.OctavisWorld.class);

				if(world != null)
				{
					if(world.isHardInstance)
					{
						((RB_Octavis) QuestManager.getInstance().getQuest("RB_Octavis")).enterInstance(player, true);
					}
					else
					{
						((RB_Octavis) QuestManager.getInstance().getQuest("RB_Octavis")).enterInstance(player, false);
					}
					return "orbis_typia011.htm";
				}
		}

		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		RB_Octavis.OctavisWorld world = InstanceManager.getInstance().getInstanceWorld(player, RB_Octavis.OctavisWorld.class);

		if(world != null)
		{
			return "orbis_typia002.htm";
		}

		return "orbis_typia001.htm";
	}
}