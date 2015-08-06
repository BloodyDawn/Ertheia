package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 02.09.12
 * Time: 11:05
 * TODO: Рецепты и цельные куски брони в награды
 */

public class _00751_LiberatingtheSpirits extends Quest
{
	// Квестовые персонажи
	private static final int Родерик = 30631;

	// Квестовые монстры
	private static final int[] Монстры = {
		23199, 23200, 23201, 23202, 23203, 23204, 23205, 23206, 23207, 23208, 23209, 23242, 23243, 23244, 23245
	};
	private static final int Скальдисект = 23212;

	// Квестовые предметы
	private static final int МясоМертвеца = 34971;
	private static final ItemHolder[] НаградыЦельные = {
		new ItemHolder(17899, 1), // Скованное Острие Фантазмы
		new ItemHolder(17900, 1), // Скованный Резак Фантазмы
		new ItemHolder(17901, 1), // Скованный Эспадон Фантазмы
		new ItemHolder(17902, 1), // Скованный Мститель Фантазмы
		new ItemHolder(17903, 1), // Скованный Воитель Фантазмы
		new ItemHolder(17904, 1), // Скованный Буревестник Фантазмы
		new ItemHolder(17905, 1), // Скованный Бросок Фантазмы
		new ItemHolder(17906, 1), // Скованный Страж Фантазмы
		new ItemHolder(17907, 1), // Скованный Расчленитель Фантазмы
		new ItemHolder(17908, 1), // Скованный Заклинатель Фантазмы
		new ItemHolder(17909, 1), // Скованное Возмездие Фантазмы
		new ItemHolder(17910, 1), // Скованные Парные Мечи Фантазмы
		new ItemHolder(17911, 1), // Скованные Парные Кинжалы Фантазмы
		new ItemHolder(17912, 1), // Скованные Парные Дубины Фантазмы
	};
	private static final ItemHolder[] НаградыРецепты = {
		new ItemHolder(19378, 1), // Рецепт - Неопознанное Острие Фантазмы (60%)
		new ItemHolder(19379, 1), // Рецепт - Неопознанный Резак Фантазмы (60%)
		new ItemHolder(19380, 1), // Рецепт - Неопознанный Эспадон Фантазмы (60%)
		new ItemHolder(19381, 1), // Рецепт - Неопознанный Мститель Фантазмы (60%)
		new ItemHolder(19382, 1), // Рецепт - Неопознанный Воитель Фантазмы (60%)
		new ItemHolder(19383, 1), // Рецепт - Неопознанный Буревестник Фантазмы (60%)
		new ItemHolder(19384, 1), // Рецепт - Неопознанный Бросок Фантазмы (60%)
		new ItemHolder(19385, 1), // Рецепт - Неопознанный Страж Фантазмы (60%)
		new ItemHolder(19386, 1), // Рецепт - Неопознанный Расчленитель Фантазмы (60%)
		new ItemHolder(19387, 1), // Рецепт - Неопознанный Заклинатель Фантазмы (60%)
		new ItemHolder(19388, 1), // Рецепт - Неопознанное Возмездие Фантазмы (60%)
	};
	private static final ItemHolder[] НаградыРесурсы = {
		new ItemHolder(34990, 100), // Обломок Оружия
		new ItemHolder(34991, 100), // Обломок Доспеха
		new ItemHolder(34992, 100)  // Драгоценные Аксессуары
	};

	public _00751_LiberatingtheSpirits()
	{
		addStartNpc(Родерик);
		addTalkId(Родерик);
		addKillId(Монстры);
		addKillId(Скальдисект);
		questItemIds = new int[]{МясоМертвеца};
	}

	public static void main(String[] args)
	{
		new _00751_LiberatingtheSpirits();
	}

	private void giveItem(QuestState st, L2Npc npc)
	{
		if(st != null && st.getCond() == 1)
		{
			if(npc.getNpcId() == Скальдисект)
			{
				st.set("1023212", "1");
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if(Rnd.getChance(50) && ArrayUtils.contains(Монстры, npc.getNpcId()) && st.getQuestItemsCount(МясоМертвеца) < 40)
			{
				st.giveItem(МясоМертвеца);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			if(st.getInt("1023212") == 1 && st.getQuestItemsCount(МясоМертвеца) >= 40)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 751;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "warden_roderik_q0751_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Родерик)
		{
			if(reply == 1)
			{
				return "warden_roderik_q0751_04.htm";
			}
			else if(reply == 2)
			{
				return "warden_roderik_q0751_05.htm";
			}
			else if(reply == 10 && cond == 2)
			{
				// Fantasy weapon recipe or piece, Seraph armor recipe or material, a weapon, an armor item, or an accessory gem at random.
				String htmltext = null;
				ItemHolder item;
				switch(Rnd.get(1, 3))
				{
					case 1:
						item = НаградыРецепты[Rnd.get(НаградыРецепты.length)];
						st.giveItems(item.getId(), item.getCount());
						htmltext = "warden_roderik_q0751_09a.htm";
						break;
					case 2:
						item = НаградыЦельные[Rnd.get(НаградыЦельные.length)];
						st.giveItems(item.getId(), item.getCount());
						htmltext = "warden_roderik_q0751_09b.htm";
						break;
					case 3:
						item = НаградыРесурсы[Rnd.get(НаградыРесурсы.length)];
						st.giveItems(item.getId(), item.getCount());
						htmltext = "warden_roderik_q0751_09c.htm";
						break;
				}
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.DAILY);
				return htmltext;
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(player.getParty() == null)
		{
			giveItem(st, npc);
		}
		else
		{
			for(L2PcInstance member : player.getParty().getMembersInRadius(player, 900))
			{
				QuestState pst = member.getQuestState(getClass());
				giveItem(pst, npc);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npcId == Родерик)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "warden_roderik_q0751_03.htm";
				case CREATED:
					return st.getPlayer().getLevel() < 95 ? "warden_roderik_q0751_02.htm" : "warden_roderik_q0751_01.htm";
				case STARTED:
					if(cond == 1)
					{
						return "warden_roderik_q0751_07.htm";
					}
					else if(cond == 2)
					{
						return "warden_roderik_q0751_08.htm";
					}
			}
		}

		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 95;
	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1023212, st.getInt("1023212"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}