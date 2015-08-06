package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 10.02.12
 * Time: 5:02
 */

public class _00466_ApplyingOurModestEfforts extends Quest
{
	// Квестовые персонажи
	private static final int Астериус = 30154;
	private static final int Мимирид = 32895;

	// Квестовые монстры
	private static final int[] БуйныеФеи = {
		22867, // Воин Фей Буйный
		22875, // Терзатель Фей Буйный
		22883, // Мститель Фей Буйный
		22868, // Воин Фей Сдерживающий Тьму Буйный
		22876, // Терзатель Фей Сдерживающий Тьму Буйный
		22884, // Мститель Фей Сдерживающий Тьму Буйный
		22869, // Воин Фей Завершивший Мутацию Буйный
		22877, // Терзатель Фей Завершивший Мутацию Буйный
		22885, // Мститель Фей Завершивший Мутацию Буйный
	};

	private static final int[] ПрекратившиеМутациюФеи = {
		22870, // Воин Фей Прекративший Мутацию
		22878, // Терзатель Фей Прекративший Мутацию
		22886, // Мститель Фей Прекративший Мутацию
		22866, // Воин Фей Прекративший Мутацию
		22874, // Терзатель Фей Прекративший Мутацию
		22882, // Мститель Фей Прекративший Мутацию
	};

	private static final int БольшойКокон = 32920;

	// Квестовые предметы
	private static final int КрылоФеи = 17597;
	private static final int ЧастьКокона = 17598;
	private static final int ДыханиеКимериана = 17599;
	private static final int ДоказательствоОбещания = 30384;
	private static final int РецептУдобрения = 17603;
	private static final int Удобрение = 17596;

	public _00466_ApplyingOurModestEfforts()
	{

		addStartNpc(Астериус);
		addTalkId(Астериус, Мимирид);
		addKillId(БуйныеФеи);
		addKillId(ПрекратившиеМутациюФеи);
		addKillId(БольшойКокон);
		questItemIds = new int[]{КрылоФеи, ЧастьКокона, ДыханиеКимериана};
	}

	public static void main(String[] args)
	{
		new _00466_ApplyingOurModestEfforts();
	}

	@Override
	public int getQuestId()
	{
		return 466;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(npc.getNpcId() == Астериус && event.equalsIgnoreCase("30154-05.htm"))
		{
			st.startQuest();
		}
		else if(npc.getNpcId() == Мимирид && event.equalsIgnoreCase("32895-03.htm"))
		{
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
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
		if(st.getCond() == 2)
		{
			if(ArrayUtils.contains(БуйныеФеи, npc.getNpcId()))
			{
				if(Rnd.getChance(20))
				{
					if(st.getQuestItemsCount(КрылоФеи) < 5)
					{
						st.giveItem(КрылоФеи);
					}
				}
			}
			else if(npc.getNpcId() == БольшойКокон)
			{
				if(Rnd.getChance(50))
				{
					if(st.getQuestItemsCount(ЧастьКокона) < 5)
					{
						st.giveItem(ЧастьКокона);
					}
				}
			}
			else if(ArrayUtils.contains(ПрекратившиеМутациюФеи, npc.getNpcId()))
			{
				if(Rnd.getChance(20))
				{
					if(st.getQuestItemsCount(ДыханиеКимериана) < 5)
					{
						st.giveItem(ДыханиеКимериана);
					}
				}
			}
			if(st.getQuestItemsCount(КрылоФеи) >= 5 && st.getQuestItemsCount(ЧастьКокона) >= 5 && st.getQuestItemsCount(ДыханиеКимериана) >= 5)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == Астериус)
		{
			if(player.getLevel() < 88)
			{
				return "30154-02.htm";
			}
			switch(st.getState())
			{
				case CREATED:
					return "30154-01.htm";
				case STARTED:
					switch(st.getCond())
					{
						case 1:
						case 2:
						case 3:
						case 4:
							return "30154-06.htm";
						case 5:
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.takeItems(Удобрение, -1);
							st.giveItems(ДоказательствоОбещания, 3);
							st.exitQuest(QuestType.DAILY);
							return "30154-07.htm";
					}
					break;
				case COMPLETED:
					return "30154-03.htm";
			}
		}
		else if(npc.getNpcId() == Мимирид)
		{
			if(st.isCompleted())
			{
				return "32895-08.htm";
			}
			switch(st.getCond())
			{
				case 1:
					return "32895-01.htm";
				case 2:
					return "32895-04.htm";
				case 3:
					st.setCond(4);
					st.giveItem(РецептУдобрения);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "32895-05.htm";
				case 4:
					if(st.getQuestItemsCount(Удобрение) == 5)
					{
						st.takeItems(Удобрение, -1);
						st.giveItem(Удобрение);
						st.setCond(5);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "32895-07.htm";
					}
					else
					{
						return "32895-06.htm";
					}
				case 5:
					return "32895-07.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 88;

	}
}
