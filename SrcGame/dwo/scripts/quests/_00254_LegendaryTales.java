package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Util;
import org.apache.commons.lang3.ArrayUtils;

public class _00254_LegendaryTales extends Quest
{
	// NPC
	private static final int GILMORE = 30754;

	// Items
	private static final int LARGE_DRAGON_SKULL = 17249;

	// Mobs
	private static final int EMERALD_HORN = 25718;
	private static final int DUST_RIDER = 25719;
	private static final int BLEEDING_FLY = 25720;
	private static final int BLACKDAGGER_WING = 25721;
	private static final int SHADOW_SUMMONER = 25722;
	private static final int SPIKE_SLASHER = 25723;
	private static final int MUSCLE_BOMBER = 25724;

	private static final int[] BOSS = {
		25718,    //Emerald Horn Raid Boss
		25719,    //Dust Rider	Raid Boss
		25720,    //Bleeding Fly	Raid Boss
		25721,    //Blackdagger Wing	Raid Boss
		25722,    //Shadow Summoner	Raid Boss
		25723,    //Spike Slasher	Raid Boss
		25724    //Muscle Bomber	Raid Boss
	};

	private static final int[] REWARDS = {
		0, 13457,    //Vesper Cutter
		13458,    //Vesper Slasher
		13459,    //Vesper Buster
		13460,    //Vesper Shaper
		13461,    //Vesper Fighter
		13462,    //Vesper Stormer
		13463,    //Vesper Avenger
		13464,    //Vesper Retributor
		13465,    //Vesper Caster
		13466,    //Vesper Singer
		13467    //Vesper Thrower
	};

	public _00254_LegendaryTales()
	{
		questItemIds = new int[]{LARGE_DRAGON_SKULL};
		addStartNpc(GILMORE);
		addTalkId(GILMORE);
		addKillId(BOSS);
	}

	public static void main(String[] args)
	{
		new _00254_LegendaryTales();
	}

	private void rewardPlayer(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		int rb = npc.getNpcId();
		if(st != null && st.isStarted() && player.isInsideRadius(npc, 2000, false, false))
		{
			if(rb == EMERALD_HORN && st.getInt("emerald") != 1)
			{
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				st.set("emerald", "1");
			}
			else if(rb == DUST_RIDER && st.getInt("dust") != 1)
			{
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				st.set("dust", "1");
			}
			else if(rb == BLEEDING_FLY && st.getInt("bleeding") != 1)
			{
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				st.set("bleeding", "1");
			}
			else if(rb == BLACKDAGGER_WING && st.getInt("blackdagger") != 1)
			{
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				st.set("blackdagger", "1");
			}
			else if(rb == SHADOW_SUMMONER && st.getInt("shadow") != 1)
			{
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				st.set("shadow", "1");
			}
			else if(rb == SPIKE_SLASHER && st.getInt("spike") != 1)
			{
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				st.set("spike", "1");
			}
			else if(rb == MUSCLE_BOMBER && st.getInt("muscle") != 1)
			{
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				st.set("muscle", "1");
			}

			if(st.getInt("emerald") == 1 && st.getInt("dust") == 1 && st.getInt("bleeding") == 1 && st.getInt("blackdagger") == 1 && st.getInt("shadow") == 1 && st.getInt("spike") == 1 && st.getInt("muscle") == 1)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 254;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(npc.getNpcId() == GILMORE)
		{
			if(event.equalsIgnoreCase("accept"))
			{
				st.startQuest();
				return "30754-7.htm";
			}

			else if(event.equalsIgnoreCase("emerald"))
			{
				return st.getInt("emerald") != 1 ? "30754-16.html" : "30754-22.html";
			}
			else if(event.equalsIgnoreCase("dust"))
			{
				return st.getInt("dust") != 1 ? "30754-17.html" : "30754-23.html";
			}
			else if(event.equalsIgnoreCase("bleeding"))
			{
				return st.getInt("bleeding") != 1 ? "30754-18.html" : "30754-24.html";
			}
			else if(event.equalsIgnoreCase("daggerwyrm"))
			{
				return st.getInt("blackdagger") != 1 ? "30754-19.html" : "30754-25.html";
			}
			else if(event.equalsIgnoreCase("shadowsummoner"))
			{
				return st.getInt("shadow") != 1 ? "30754-16.html" : "30754-26.html";
			}
			else if(event.equalsIgnoreCase("spikeslasher"))
			{
				return st.getInt("spike") != 1 ? "30754-17.html" : "30754-27.html";
			}
			else if(event.equalsIgnoreCase("muclebomber"))
			{
				return st.getInt("muscle") != 1 ? "30754-18.html" : "30754-28.html";
			}

			else if(Util.isDigit(event))
			{
				int reward_id = Integer.parseInt(event);
				if(reward_id > 0)
				{
					if(st.getQuestItemsCount(LARGE_DRAGON_SKULL) == 7)
					{
						int REWARD = REWARDS[reward_id];

						st.takeItems(LARGE_DRAGON_SKULL, -1);
						st.giveItems(REWARD, 1);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.unset("emerald");
						st.unset("dust");
						st.unset("bleeding");
						st.unset("blackdagger");
						st.unset("shadow");
						st.unset("spike");
						st.unset("muscle");
						st.exitQuest(QuestType.ONE_TIME);
						return "30754-13.html";
					}
					else
					{
						return "30754-12.htm";
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(ArrayUtils.contains(BOSS, npc.getNpcId()) && st != null)
		{
			if(player.isInParty())
			{
				for(L2PcInstance memb : player.getParty().getMembers())
				{
					rewardPlayer(npc, memb);
				}
			}
			else
			{
				rewardPlayer(npc, player);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == GILMORE)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() < 80 ? "30754-3.html" : "30754-1.htm";
				case STARTED:
					if(st.getCond() == 1 && (st.getInt("emerald") != 1 || st.getInt("dust") != 1 || st.getInt("bleeding") != 1 || st.getInt("blackdagger") != 1 || st.getInt("shadow") != 1 || st.getInt("spike") != 1 || st.getInt("muscle") != 1))
					{
						return "30754-9.htm";
					}
					if(st.getCond() == 2 && st.getInt("emerald") == 1 && st.getInt("dust") == 1 && st.getInt("bleeding") == 1 && st.getInt("blackdagger") == 1 && st.getInt("shadow") == 1 && st.getInt("spike") == 1 && st.getInt("muscle") == 1)
					{
						return "30754-10.html";
					}
					break;
				case COMPLETED:
					return "30754-2.html";
			}
		}
		return null;
	}
}