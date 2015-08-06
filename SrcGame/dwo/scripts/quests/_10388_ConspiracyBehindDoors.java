package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2-GodWorld Team
 * @author Yukio
 * Date: 11.06.13
 * Time: 20:11:59
 */
public class _10388_ConspiracyBehindDoors extends Quest
{
	// Quest Npc
	private static final int ELIYAH = 31329;
	private static final int KARGOS = 33821;
	private static final int KITCHEN = 33820;
	private static final int RAZEN = 33803;

	// Quest Item
	private static final int VISITOR_BADGE = 36228;

	private _10388_ConspiracyBehindDoors()
	{
		addStartNpc(ELIYAH);
		addTalkId(ELIYAH, KARGOS, KITCHEN, RAZEN);

		questItemIds = new int[]{VISITOR_BADGE};
	}

	public static void main(String[] args)
	{
		new _10388_ConspiracyBehindDoors();
	}

	@Override
	public int getQuestId()
	{
		return 10388;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && qs.getPlayer().getLevel() >= 97 && !qs.isCompleted())
		{
			qs.startQuest();
			return "priest_eliyah_q10388_08.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(st == null)
		{
			return null;
		}

		int cond = st.getCond();
		int npcId = npc.getNpcId();
		switch(npcId)
		{
			case ELIYAH:
				if(reply == 1)
				{
					return "priest_eliyah_q10388_05.htm";
				}
				if(reply == 5)
				{
					return "priest_eliyah_q10388_06.htm";
				}
				if(reply == 6)
				{
					return "priest_eliyah_q10388_07.htm";
				}
				break;
			case KARGOS:
				if(reply == 1)
				{
					return "kargos_q10388_03.htm";
				}
				if(reply == 2)
				{
					return "kargos_q10388_04.htm";
				}
				if(reply == 3 && cond == 1)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "kargos_q10388_05.htm";
				}
				break;
			case KITCHEN:
				if(reply == 1 && cond == 2)
				{
					st.setCond(3);
					st.giveItem(VISITOR_BADGE);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "kitchen_q10388_02.htm";
				}
				break;
			case RAZEN:
				if(reply == 1)
				{
					return "razen_q10388_02.htm";
				}
				if(reply == 2 && cond == 3)
				{
					st.takeItems(VISITOR_BADGE, -1);
					st.addExpAndSp(29638350, 7113);
					st.giveAdena(65136, true);
					st.exitQuest(QuestType.ONE_TIME);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "razen_q10388_03.htm";
				}
				break;
		}
		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(st == null)
		{
			return null;
		}

		int cond = st.getCond();
		switch(npc.getNpcId())
		{
			case ELIYAH:
				if(st.isCompleted())
				{
					return "priest_eliyah_q10388_03.htm";
				}
				if(st.isCreated())
				{
					if(st.getPlayer().getLevel() >= 97 && !st.isCompleted())
					{
						return "priest_eliyah_q10388_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "priest_eliyah_q10388_02.htm";
					}
				}
				if(cond >= 1)
				{
					return "priest_eliyah_q10388_09.htm";
				}
				break;
			case KARGOS:
				if(cond == 1)
				{
					return "kargos_q10388_02.htm";
				}
				if(cond >= 2)
				{
					return "kargos_q10388_06.htm";
				}
				break;
			case KITCHEN:
				if(cond == 2)
				{
					return "kitchen_q10388_01.htm";
				}
				if(cond == 3 && !st.getPlayer().getInventory().hasItems(VISITOR_BADGE))
				{
					st.giveItem(VISITOR_BADGE);
					return "kitchen_q10388_03.htm";
				}
				if(cond == 3)
				{
					return "kitchen_q10388_04.htm";
				}
				break;
			case RAZEN:
				if(st.isCompleted())
				{
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
				}
				if(cond == 3)
				{
					return "razen_q10388_01.htm";
				}
				break;
		}
		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 97;
	}
}
