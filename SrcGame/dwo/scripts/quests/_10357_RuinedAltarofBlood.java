package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 20.11.11
 * Time: 17:14
 * TODO: Незакончен , пока не попал на момент когда проходит задание "Уничтожение Зла"
 */

public class _10357_RuinedAltarofBlood extends Quest
{
	private static final int AGENT_GEORGIO = 33515;
	private static final int WEAPON_BAG = 33466;

	private static final int ISADORA = 25856;
	private static final int MELISSA = 25855;

	public _10357_RuinedAltarofBlood()
	{
		addStartNpc(AGENT_GEORGIO);
		addTalkId(AGENT_GEORGIO);
		addKillId(MELISSA, ISADORA);
	}

	public static void main(String[] args)
	{
		new _10357_RuinedAltarofBlood();
	}

	@Override
	public int getQuestId()
	{
		return 10357;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equalsIgnoreCase("33515-02.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("teleport"))
		{
			// TODO: Говорят, что обычно проникнуть в Алтарь Шилен невозможно. Алтарь отвязали от капмании!
			// Но есть время, когда начинает работать внутренний Алтарь и монстров на всей территории становится намного больше.
			// Примите участие в проходящем в это время <font color="LEVEL">задании "Уничтожение Зла"</font>.
			// Если Вам удастся успешно справиться с этим заданием, то наши лучшие агенты смо
			//event = "33405-00.htm"; // TODO HTML удален
		}
		else if(event.equalsIgnoreCase("33515-finish.htm"))
		{
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.addExpAndSp(11000000, 5000000);
			st.giveItem(WEAPON_BAG);
			st.exitQuest(QuestType.ONE_TIME);
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == AGENT_GEORGIO)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				case CREATED:
					if(player.getLevel() >= 95)
					{
						return "33515-00.htm";
					}
					else
					{

						st.exitQuest(QuestType.REPEATABLE);
						return getLowLevelMsg(95);
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33515-02.htm";
					}
					break;
			}
		}
		return null;
	}
}
