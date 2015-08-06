package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class _00464_Oath extends Quest
{
	private static final int _sophia = 32596;
	private static final int[] _ownerbook = {30657, 30839, 30899, 31350, 30539, 30297, 31960, 31588};
	private static final int[][] _esa = {
		{15449, 17696, 42910}, {189377, 21692, 52599}, {249180, 28542, 69210}, {249180, 28542, 69210},
		{19408, 47062, 169442}, {24146, 58551, 210806}, {15449, 17696, 42910}, {15449, 17696, 42910}
	};
	private static final String[][] bk_own = {
		{"Cardinal", "Seresin", "Oren"}, {"Grocer", "Holly", "Aden"}, {"Gatekeeper", "Flauen", "Heine"},
		{"Priest", "Dominic", "Rune"}, {"Priestess of the Earth", "Chichirin", "Dwarf Village"},
		{"Grand Master", "Tobias", "Gludio"}, {"Blacksmith", "Burun", "Stuttgard"},
		{"Saint of Light", "Agnes", "Goddard"}
	};
	private NpcHtmlMessage htmka;

	public _00464_Oath()
	{
		addTalkId(_sophia);
		addTalkId(_ownerbook);
		questItemIds = new int[]{15538, 15539};
	}

	public static void main(String[] args)
	{
		new _00464_Oath();
	}

	@Override
	public int getQuestId()
	{
		return 464;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return event;
		}
		if(npc.getNpcId() == _sophia)
		{
			if(event.equalsIgnoreCase("GoWay"))
			{
				int numrnd = Rnd.get(8);
				st.takeItems(15538, 1);
				st.giveItems(15539, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(numrnd + 2);
				showHtm(numrnd, "32596-04", npc, player);
				return "";
			}
		}
		else if(ArrayUtils.contains(_ownerbook, npc.getNpcId()))
		{
			if(event.equalsIgnoreCase("Reward"))
			{
				switch(npc.getNpcId())
				{
					case 30657:
						endquest(0, st, npc, player);
						break;
					case 30839:
						endquest(1, st, npc, player);
						break;
					case 30899:
						endquest(2, st, npc, player);
						break;
					case 31350:
						endquest(3, st, npc, player);
						break;
					case 30539:
						endquest(4, st, npc, player);
						break;
					case 30297:
						endquest(5, st, npc, player);
						break;
					case 31960:
						endquest(6, st, npc, player);
						break;
					case 31588:
						endquest(7, st, npc, player);
						break;
				}
				return "";
			}
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return htmltext;
		}

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(player.getLevel() >= 82)
		{
			if(st.getState() == STARTED)
			{
				int condq = st.getCond();
				if(npc.getNpcId() == _sophia)
				{
					if(condq == 1)
					{
						htmltext = "32596-01.htm";
					}
					else
					{
						showHtm(condq - 2, "32596-05", npc, player);
						htmltext = "";
					}
				}
				if(player.getItemsCount(15539) >= 1 && ArrayUtils.contains(_ownerbook, npc.getNpcId()))
				{
					switch(npc.getNpcId())
					{
						case 30657:
							if(condq == 2)
							{
								htmltext = "30657.htm";
							}
							break;
						case 30839:
							if(condq == 3)
							{
								htmltext = "30839.htm";
							}
							break;
						case 30899:
							if(condq == 4)
							{
								htmltext = "30899.htm";
							}
							break;
						case 31350:
							if(condq == 5)
							{
								htmltext = "31350.htm";
							}
							break;
						case 30539:
							if(condq == 6)
							{
								htmltext = "30539.htm";
							}
							break;
						case 30297:
							if(condq == 7)
							{
								htmltext = "30297.htm";
							}
							break;
						case 31960:
							if(condq == 8)
							{
								htmltext = "31960.htm";
							}
							break;
						case 31588:
							if(condq == 9)
							{
								htmltext = "31588.htm";
							}
							break;
					}
				}
			}
			else if(st.getState() == COMPLETED)
			{
				htmltext = "Reenter.htm";
			}
		}
		else
		{
			htmltext = "32596-00.htm";
		}
		return htmltext;
	}

	private void showHtm(int number, String htm, L2Npc npc, L2PcInstance player)
	{
		htmka = new NpcHtmlMessage(npc.getObjectId());
		htmka.setFileQuest(player.getLang(), "quests/464_Oath/" + htm + ".htm");
		htmka.replace("%bk_own%", bk_own[number][0]);
		htmka.replace("%bk_own_name%", bk_own[number][1]);
		htmka.replace("%town%", bk_own[number][2]);
		player.sendPacket(htmka);
	}

	private void endquest(int numnpc, QuestState st, L2Npc npc, L2PcInstance player)
	{
		st.takeItems(15539, 1);
		st.addExpAndSp(_esa[numnpc][0], _esa[numnpc][1]);
		st.giveAdena(_esa[numnpc][2], true);
		st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
		st.exitQuest(QuestType.DAILY);
		showHtm(numnpc, "Reward", npc, player);
	}
}