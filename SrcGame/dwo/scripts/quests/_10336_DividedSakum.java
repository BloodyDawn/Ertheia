package dwo.scripts.quests;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.TutorialEnableClientEvent;

public class _10336_DividedSakum extends Quest
{
	// нпц
	private static final int ZENATH = 33509;
	private static final int ADVENTURE_GUILDSMAN = 31795;
	// Итемы
	private static final int SAKUMS_SKETCH_A = 17584;
	// Мобы
	private static final int KANILOV = 27451;

	public _10336_DividedSakum()
	{
		addStartNpc(ZENATH);
		addTalkId(ZENATH, ADVENTURE_GUILDSMAN);
		addKillId(KANILOV);
		questItemIds = new int[]{SAKUMS_SKETCH_A};
		addEventId(HookType.ON_LEVEL_INCREASE);
	}

	public static void main(String[] args)
	{
		new _10336_DividedSakum();
	}

	@Override
	public int getQuestId()
	{
		return 10336;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(event.equalsIgnoreCase("33509-1b.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("31795-2.htm"))
		{
			st.giveAdena(100000, true);
			st.takeItems(SAKUMS_SKETCH_A, -1);
			st.addExpAndSp(350000, 150000);
			st.giveItems(955, 3); //Scroll: Enchant Weapon (D-Grade)
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.ONE_TIME);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}
		if(st.getCond() == 1)
		{
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == ZENATH)
		{
			switch(st.getState())
			{
				case CREATED:
					if(canBeStarted(player))
					{
						return "33509.htm";
					}
					else
					{

						st.exitQuest(QuestType.REPEATABLE);
						return "33509-2.htm";
					}
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "33509-3.htm";
						case 2:
							st.giveItems(SAKUMS_SKETCH_A, 1);
							st.setCond(3);
							return "33509-4.htm";
						case 3:
							return "33509-5.htm";
					}
					break;
				case COMPLETED:
					return "33509-6.htm";
			}
		}
		else if(npc.getNpcId() == ADVENTURE_GUILDSMAN)
		{
			if(st.isCompleted())
			{
				return "31795-3.htm";
			}
			switch(st.getCond())
			{
				case 1:
					return "31795-4.htm";
				case 2:
					return "31795-5.htm";
				case 3:
					return "31795.htm";
			}
		}
		return null;
	}

	@Override
	public void onLevelIncreased(L2PcInstance player)
	{
		// TODO: Хз при каких обстоятельствах шлется после 1-ого раза
		if(player.getLevel() > 26 && player.getLevel() < 28)
		{
			player.sendPacket(new TutorialEnableClientEvent(0));
		}
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 27 && player.getLevel() <= 40;

	}
} 