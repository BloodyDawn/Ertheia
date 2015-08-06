package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.Elementals;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _10275_ContainingTheAttributePower extends Quest
{
	private static final int Holly = 30839;
	private static final int Weber = 31307;
	private static final int Yin = 32325;
	private static final int Yang = 32326;
	private static final int Water = 27380;
	private static final int Air = 27381;

	private static final int YinSword = 13845;
	private static final int YangSword = 13881;
	private static final int SoulPieceWater = 13861;
	private static final int SoulPieceAir = 13862;

	public _10275_ContainingTheAttributePower()
	{
		addStartNpc(Holly, Weber);
		addTalkId(Yin, Yang);
		addKillId(Air, Water);
		questItemIds = new int[]{YinSword, YangSword, SoulPieceWater, SoulPieceAir};
	}

	public static void main(String[] args)
	{
		new _10275_ContainingTheAttributePower();
	}

	@Override
	public int getQuestId()
	{
		return 10275;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		switch(event)
		{
			case "30839-02.htm":
			case "31307-02.htm":
				st.startQuest();
				break;
			case "30839-05.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "31307-05.htm":
				st.setCond(7);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32325-03.htm":
				st.setCond(3);
				st.giveItems(YinSword, 1, Elementals.FIRE, 10);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32326-03.htm":
				st.setCond(8);
				st.giveItems(YangSword, 1, Elementals.EARTH, 10);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32325-06.htm":
				if(st.getQuestItemsCount(YinSword) > 0)
				{
					st.takeItems(YinSword, 1);
					return "32325-07.htm";
				}
				st.giveItems(YinSword, 1, Elementals.FIRE, 10);
				break;
			case "32326-06.htm":
				if(st.getQuestItemsCount(YangSword) > 0)
				{
					st.takeItems(YangSword, 1);
					return "32326-07.htm";
				}
				st.giveItems(YangSword, 1, Elementals.EARTH, 10);
				break;
			case "32325-09.htm":
				st.setCond(5);
				SkillTable.getInstance().getInfo(2635, 1).getEffects(player, player);
				st.giveItems(YinSword, 1, Elementals.FIRE, 10);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32326-09.htm":
				st.setCond(10);
				SkillTable.getInstance().getInfo(2636, 1).getEffects(player, player);
				st.giveItems(YangSword, 1, Elementals.EARTH, 10);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			default:
				int item = 0;

				if(event.equalsIgnoreCase("1"))
				{
					item = 10521;
				}
				else if(event.equalsIgnoreCase("2"))
				{
					item = 10522;
				}
				else if(event.equalsIgnoreCase("3"))
				{
					item = 10523;
				}
				else if(event.equalsIgnoreCase("4"))
				{
					item = 10524;
				}
				else if(event.equalsIgnoreCase("5"))
				{
					item = 10525;
				}
				else if(event.equalsIgnoreCase("6"))
				{
					item = 10526;
				}

				if(item > 0)
				{
					st.giveItems(item, 2);
					st.addExpAndSp(202160, 20375);
					st.exitQuest(QuestType.ONE_TIME);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return npc != null ? npc.getNpcId() + "-1" + event + ".htm" : null;
				}
				break;
		}

		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null || st.getState() != STARTED)
		{
			return null;
		}

		int cond = st.getCond();
		int npcId = npc.getNpcId();

		if(npcId == Air)
		{
			if(st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == YangSword && (cond == 8 || cond == 10) && st.getQuestItemsCount(SoulPieceAir) < 6 && Rnd.getChance(30))
			{
				st.giveItems(SoulPieceAir, 1);
				if(st.getQuestItemsCount(SoulPieceAir) >= 6)
				{
					st.setCond(cond + 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
		else if(npcId == Water)
		{
			if(st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == YinSword && (cond == 3 || cond == 5) && st.getQuestItemsCount(SoulPieceWater) < 6 && Rnd.getChance(30))
			{
				st.giveItems(SoulPieceWater, 1);
				if(st.getQuestItemsCount(SoulPieceWater) >= 6)
				{
					st.setCond(cond + 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		QuestStateType id = st.getState();
		int cond = st.getCond();
		int npcId = npc.getNpcId();

		if(id == COMPLETED)
		{
			if(npcId == Holly)
			{
				return "30839-0a.htm";
			}
			else if(npcId == Weber)
			{
				return "31307-0a.htm";
			}
		}
		else if(id == CREATED)
		{
			if(st.getPlayer().getLevel() >= 76)
			{
				return npcId == Holly ? "30839-01.htm" : "31307-01.htm";
			}
			else
			{
				return npcId == Holly ? "30839-00.htm" : "31307-00.htm";
			}
		}
		else if(npcId == Holly)
		{
			if(cond == 1)
			{
				return "30839-03.htm";
			}
			else if(cond == 2)
			{
				return "30839-05.htm";
			}
		}
		else if(npcId == Weber)
		{
			if(cond == 1)
			{
				return "31307-03.htm";
			}
			else if(cond == 7)
			{
				return "31307-05.htm";
			}
		}
		else if(npcId == Yin)
		{
			if(cond == 2)
			{
				return "32325-01.htm";
			}
			else if(cond == 3 || cond == 5)
			{
				return "32325-04.htm";
			}
			else if(cond == 4)
			{
				st.takeItems(YinSword, 1);
				st.takeItems(SoulPieceWater, -1);
				return "32325-08.htm";
			}
			else if(cond == 6)
			{
				return "32325-10.htm";
			}
		}
		else if(npcId == Yang)
		{
			if(cond == 7)
			{
				return "32326-01.htm";
			}
			else if(cond == 8 || cond == 10)
			{
				return "32326-04.htm";
			}
			else if(cond == 9)
			{
				st.takeItems(YangSword, 1);
				st.takeItems(SoulPieceAir, -1);
				return "32326-08.htm";
			}
			else if(cond == 11)
			{
				return "32326-10.htm";
			}
		}

		return null;
	}
}