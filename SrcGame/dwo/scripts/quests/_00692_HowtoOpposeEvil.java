package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import gnu.trove.map.hash.TIntObjectHashMap;

public class _00692_HowtoOpposeEvil extends Quest
{
	private static final int DILIOS = 32549;
	private static final int LEKONS_CERTIFICATE = 13857;
	private static final int[] QUEST_ITEMS = {13863, 13864, 13865, 13866, 13867, 15535, 15536};

	private static final TIntObjectHashMap<Integer[]> _questMobs = new TIntObjectHashMap<>();

	static
	{
		// Seed of Infinity
		_questMobs.put(22509, new Integer[]{13863, 500});
		_questMobs.put(22510, new Integer[]{13863, 500});
		_questMobs.put(22511, new Integer[]{13863, 500});
		_questMobs.put(22512, new Integer[]{13863, 500});
		_questMobs.put(22513, new Integer[]{13863, 500});
		_questMobs.put(22514, new Integer[]{13863, 500});
		_questMobs.put(22515, new Integer[]{13863, 500});
		// Seed of Destruction
		_questMobs.put(22537, new Integer[]{13865, 250});
		_questMobs.put(22538, new Integer[]{13865, 250});
		_questMobs.put(22539, new Integer[]{13865, 250});
		_questMobs.put(22540, new Integer[]{13865, 250});
		_questMobs.put(22541, new Integer[]{13865, 250});
		_questMobs.put(22542, new Integer[]{13865, 250});
		_questMobs.put(22543, new Integer[]{13865, 250});
		_questMobs.put(22544, new Integer[]{13865, 250});
		_questMobs.put(22546, new Integer[]{13865, 250});
		_questMobs.put(22547, new Integer[]{13865, 250});
		_questMobs.put(22548, new Integer[]{13865, 250});
		_questMobs.put(22549, new Integer[]{13865, 250});
		_questMobs.put(22550, new Integer[]{13865, 250});
		_questMobs.put(22551, new Integer[]{13865, 250});
		_questMobs.put(22552, new Integer[]{13865, 250});
		_questMobs.put(22593, new Integer[]{13865, 250});
		_questMobs.put(22596, new Integer[]{13865, 250});
		_questMobs.put(22597, new Integer[]{13865, 250});
		// Seed of Annihilation
		_questMobs.put(22746, new Integer[]{15536, 125});
		_questMobs.put(22747, new Integer[]{15536, 125});
		_questMobs.put(22748, new Integer[]{15536, 125});
		_questMobs.put(22749, new Integer[]{15536, 125});
		_questMobs.put(22750, new Integer[]{15536, 125});
		_questMobs.put(22751, new Integer[]{15536, 125});
		_questMobs.put(22752, new Integer[]{15536, 125});
		_questMobs.put(22753, new Integer[]{15536, 125});
		_questMobs.put(22754, new Integer[]{15536, 125});
		_questMobs.put(22755, new Integer[]{15536, 125});
		_questMobs.put(22756, new Integer[]{15536, 125});
		_questMobs.put(22757, new Integer[]{15536, 125});
		_questMobs.put(22758, new Integer[]{15536, 125});
		_questMobs.put(22759, new Integer[]{15536, 125});
		_questMobs.put(22760, new Integer[]{15536, 125});
		_questMobs.put(22761, new Integer[]{15536, 125});
		_questMobs.put(22762, new Integer[]{15536, 125});
		_questMobs.put(22763, new Integer[]{15536, 125});
		_questMobs.put(22764, new Integer[]{15536, 125});
		_questMobs.put(22765, new Integer[]{15536, 125});
	}

	public _00692_HowtoOpposeEvil()
	{
		addKillId(_questMobs.keys());
		addStartNpc(DILIOS);
		addTalkId(DILIOS);
		addTalkId(32550);
	}

	public static void main(String[] args)
	{
		new _00692_HowtoOpposeEvil();
	}

	private boolean giveReward(QuestState st, int itemId, int minCount, int rewardItemId, long rewardCount)
	{
		long count = st.getQuestItemsCount(itemId);
		if(count >= minCount)
		{
			count /= minCount;
			st.takeItems(itemId, count * minCount);
			if(rewardItemId == PcInventory.ADENA_ID)
			{
				st.giveAdena(rewardCount * count, true);
			}
			else
			{
				st.giveItems(rewardItemId, rewardCount * count);
			}
			return true;
		}
		return false;
	}

	@Override
	public int getQuestId()
	{
		return 692;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return "";
		}
		switch(event)
		{
			case "32549-03.htm":
				st.startQuest();
				break;
			case "32550-04.htm":
				st.setCond(3);
				break;
			case "32550-07.htm":
				if(!giveReward(st, 13863, 5, 13796, 1))
				{
					return "32550-08.htm";
				}
				break;
			case "32550-09.htm":
				if(!giveReward(st, 13798, 1, PcInventory.ADENA_ID, 5000))
				{
					return "32550-10.htm";
				}
				break;
			case "32550-12.htm":
				if(!giveReward(st, 13865, 5, 13841, 1))
				{
					return "32550-13.htm";
				}
				break;
			case "32550-14.htm":
				if(!giveReward(st, 13867, 1, PcInventory.ADENA_ID, 5000))
				{
					return "32550-15.htm";
				}
				break;
			case "32550-17.htm":
				if(!giveReward(st, 15536, 5, 15486, 1))
				{
					return "32550-18.htm";
				}
				break;
			case "32550-19.htm":
				if(!giveReward(st, 15535, 1, PcInventory.ADENA_ID, 5000))
				{
					return "32550-20.htm";
				}
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, "3");
		if(partyMember == null)
		{
			return null;
		}
		QuestState st = partyMember.getQuestState(getClass());
		if(st != null && _questMobs.containsKey(npc.getNpcId()))
		{
			int chance = (int) (_questMobs.get(npc.getNpcId())[1] * Config.RATE_QUEST_DROP);
			int numItems = chance / 1000;
			chance %= 1000;
			if(st.getRandom(1000) < chance)
			{
				numItems++;
			}
			if(numItems > 0)
			{
				st.giveItems(_questMobs.get(npc.getNpcId())[0], numItems);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		QuestStateType id = st.getState();
		int cond = st.getCond();
		String htmltext = "";
		if(id == CREATED)
		{
			htmltext = player.getLevel() >= 75 ? "32549-01.htm" : "32549-00.htm";
		}
		else
		{
			if(npc.getNpcId() == DILIOS)
			{
				if(cond == 1 && st.hasQuestItems(LEKONS_CERTIFICATE))
				{
					st.takeItems(LEKONS_CERTIFICATE, 1);
					htmltext = "32549-04.htm";
					st.setCond(2);
				}
				else if(cond == 2)
				{
					htmltext = "32549-05.htm";
				}
			}
			else
			{
				if(cond == 2)
				{
					htmltext = "32550-01.htm";
				}
				else if(cond == 3)
				{
					for(int i : QUEST_ITEMS)
					{
						if(st.hasQuestItems(i))
						{
							return "32550-05.htm";
						}
					}
					htmltext = "32550-04.htm";
				}
			}
		}
		return htmltext;
	}
}