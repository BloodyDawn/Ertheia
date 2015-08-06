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
 * Date: 23.02.12
 * Time: 1:57
 */

public class _10306_FallenLeader extends Quest
{
	// Квестовые персонажи
	private static final int NAOMI_KASHERON = 32896;

	// Квестовые монстры
	private static final int KIMERIAN = -1;

	// Награда
	private static final int[] CRYSTALS = {9552, 9553, 9554, 9555, 9556, 9557};

	public _10306_FallenLeader()
	{
		addStartNpc(NAOMI_KASHERON);
		addTalkId(NAOMI_KASHERON);
		addKillId(KIMERIAN);
	}

	public static void main(String[] args)
	{
		new _10306_FallenLeader();
	}

	@Override
	public int getQuestId()
	{
		return 10306;
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
			case "32896-05.htm":
				st.startQuest();
				break;
			case "32896-08.htm":
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(9479594, 4104484);
				st.giveItem(CRYSTALS[Rnd.get(CRYSTALS.length)]);
				st.exitQuest(QuestType.ONE_TIME);
				break;
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		QuestState prevst = player.getQuestState(_10305_TheEndlessFutileEfforts.class);

		if(npc.getNpcId() == NAOMI_KASHERON)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "32896-02.htm";
				case CREATED:
					if(player.getLevel() >= 90)
					{
						if(prevst != null && prevst.isCompleted())
						{
							return "32896-01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "32896-03.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "32896-03.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "32896-06.htm";
					}
					else if(st.getCond() == 2)
					{
						return "32896-07.htm";
					}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10305_TheEndlessFutileEfforts.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 90;

	}
}
