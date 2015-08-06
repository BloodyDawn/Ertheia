package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 05.03.12
 * Time: 2:19
 */

public class _00136_MoreThanMeetsTheEye extends Quest
{
	// Квестовые персонажи
	private static final int HARDIN = 30832;
	private static final int ERRICKIN = 30701;
	private static final int CLAYTON = 30464;

	// Квестовые предметы
	private static final int TransformSealbook = 9648;
	private static final int Ectoplasm = 9787;
	private static final int StabilizedEctoplasm = 9786;
	private static final int HardinsInstructions = 9788;
	private static final int GlassJaguarCrystal = 9789;
	// Дроп
	//# [COND, NEWCOND, ID, REQUIRED, ITEM, NEED_COUNT, CHANCE, DROP]
	private static final int[][] DROPLIST_COND = {
		{
			3, 4, 20636, 0, Ectoplasm, 35, 100, 1
		}, {
		3, 4, 20637, 0, Ectoplasm, 35, 100, 1
	}, {
		3, 4, 20638, 0, Ectoplasm, 35, 100, 1
	}, {
		3, 4, 20639, 0, Ectoplasm, 35, 100, 2
	}, {
		7, 8, 20250, 0, GlassJaguarCrystal, 5, 100, 1
	}
	};
	private static final int BlankSealbook = 9790;

	public _00136_MoreThanMeetsTheEye()
	{
		addStartNpc(HARDIN);
		addTalkId(HARDIN, ERRICKIN, CLAYTON);

		questItemIds = new int[]{
			StabilizedEctoplasm, HardinsInstructions, BlankSealbook, Ectoplasm, GlassJaguarCrystal
		};

		for(int[] aDROPLIST_COND : DROPLIST_COND)
		{
			addKillId(aDROPLIST_COND[2]);
		}
	}

	public static void main(String[] args)
	{
		new _00136_MoreThanMeetsTheEye();
	}

	@Override
	public int getQuestId()
	{
		return 136;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		switch(event)
		{
			case "30832-02.htm":
				st.startQuest();
				break;
			case "30832-05.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30832-10.htm":
				st.takeItems(StabilizedEctoplasm, 1);
				st.giveItems(HardinsInstructions, 1);
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30832-14.htm":
				st.takeItems(BlankSealbook, 1);
				st.giveAdena(67550, true);
				st.giveItems(TransformSealbook, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.setState(COMPLETED);
				st.exitQuest(QuestType.ONE_TIME);
				break;
			case "30701-02.htm":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30464-02.htm":
				st.takeItems(HardinsInstructions, 1);
				st.setCond(7);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		int npcId = npc.getNpcId();
		int cond = st.getCond();
		for(int[] aDROPLIST_COND : DROPLIST_COND)
		{
			if(cond == aDROPLIST_COND[0] && npcId == aDROPLIST_COND[2])
			{
				if(aDROPLIST_COND[3] == 0 || st.getQuestItemsCount(aDROPLIST_COND[3]) > 0)
				{
					long count = st.getQuestItemsCount(aDROPLIST_COND[4]);
					if(aDROPLIST_COND[5] > count && Rnd.getChance(aDROPLIST_COND[6]))
					{
						long random = 0;
						if(aDROPLIST_COND[7] > 1)
						{
							random = Rnd.get(aDROPLIST_COND[7]) + 1;
							if(count + random > aDROPLIST_COND[5])
							{
								random = aDROPLIST_COND[5] - count;
							}
						}
						else
						{
							random = 1;
						}
						//Аддон
						if(cond == 3)
						{
							if(random == 1)
							{
								if(Rnd.getChance(15))
								{
									random = 2;
								}
							}
							else if(Rnd.getChance(15))
							{
								random = 3;
							}
							if(count + random > aDROPLIST_COND[5])
							{
								random = aDROPLIST_COND[5] - count;
							}
						}
						//Конец Аддона
						st.giveItems(aDROPLIST_COND[4], random);
						if(count + random == aDROPLIST_COND[5])
						{
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							if(aDROPLIST_COND[1] != 0)
							{
								st.setCond(aDROPLIST_COND[1]);
								st.setState(STARTED);
							}
						}
						else
						{
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
						}
					}
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == HARDIN)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				case CREATED:
					if(player.getLevel() >= 50)
					{
						return "30832-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30832-00.htm";
					}
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "30832-02.htm";
						case 5:
							return "30832-06.htm";
						case 9:
							return "30832-11.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == ERRICKIN)
		{
			if(st.getCond() == 2)
			{
				return "30701-01.htm";
			}
			else if(st.getCond() == 4)
			{
				st.takeItems(Ectoplasm, 35);
				st.giveItems(StabilizedEctoplasm, 1);
				st.setCond(5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "30701-03.htm";
			}
		}
		else if(npc.getNpcId() == CLAYTON)
		{
			if(st.getCond() == 6)
			{
				return "30464-01.htm";
			}
			else if(st.getCond() == 8)
			{
				st.takeItems(GlassJaguarCrystal, 5);
				st.giveItems(BlankSealbook, 1);
				st.setCond(9);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "30464-03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 50;

	}
}