package dwo.scripts.quests;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class _00458_PerfectForm extends Quest
{
	private static final int _kelleyia = 32768;
	private static final int[] _mobsGrendel = {18899, 18900};
	private static final int[] _mobsBuffalo = {18892, 18893};
	private static final int[] _mobsCougar = {18885, 18886};
	private static final int[] _mobsKookaburra = {18878, 18879};

	public _00458_PerfectForm()
	{
		addStartNpc(_kelleyia);
		addTalkId(_kelleyia);
		addKillId(_mobsGrendel);
		addKillId(_mobsBuffalo);
		addKillId(_mobsCougar);
		addKillId(_mobsKookaburra);
	}

	public static void main(String[] args)
	{
		new _00458_PerfectForm();
	}

	@Override
	public int getQuestId()
	{
		return 458;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return event;
		}
		int cond = st.getCond();

		if(event.equals("32768-11.htm") && cond == 0)
		{
			st.set("grendel_kill", "0");
			st.set("buffalo_kill", "0");
			st.set("cougar_kill", "0");
			st.set("kookaburra_kill", "0");
			st.set("over_hit", "0"); //Общее кол-во оверхитов
			st.set("over_tempcount", "0"); //Количество оверхитов подряд, до того как сфейлил след. оверхит
			st.set("count", "0"); //Записанное, максимальное количество сделанных подряд оверхитов
			st.startQuest();
		}
		else if(event.equals("rating")) //Подводим рейтинг
		{
			int overhits = st.getInt("over_hit");
			if(overhits >= 20)
			{
				String content = HtmCache.getInstance().getHtmQuest(player.getLang(), "quests/458_PerfectForm/32768-14a.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setHtml(content);
				html.replace("<?number?>", String.valueOf(String.valueOf(overhits)));
				player.sendPacket(html);
			}
			else if(overhits < 20 && overhits >= 7)
			{
				String content = HtmCache.getInstance().getHtmQuest(player.getLang(), "quests/458_PerfectForm/32768-14b.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setHtml(content);
				html.replace("<?number?>", String.valueOf(String.valueOf(overhits)));
				player.sendPacket(html);
			}
			else if(overhits < 7)
			{
				String content = HtmCache.getInstance().getHtmQuest(player.getLang(), "quests/458_PerfectForm/32768-14c.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setHtml(content);
				html.replace("<?number?>", String.valueOf(String.valueOf(overhits)));
				player.sendPacket(html);
			}
		}
		else if(event.equals("rating")) //Подводим по оверхитам подряд
		{
			int overhits = st.getInt("count");
			if(overhits >= 20)
			{
				String content = HtmCache.getInstance().getHtmQuest(player.getLang(), "quests/458_PerfectForm/32768-16a.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setHtml(content);
				html.replace("<\\?number\\?>", String.valueOf(String.valueOf(overhits)));
				player.sendPacket(html);
			}
			else if(overhits < 20 && overhits >= 7)
			{
				String content = HtmCache.getInstance().getHtmQuest(player.getLang(), "quests/458_PerfectForm/32768-16b.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setHtml(content);
				html.replace("<\\?number\\?>", String.valueOf(String.valueOf(overhits)));
				player.sendPacket(html);
			}
			else if(overhits < 7)
			{
				String content = HtmCache.getInstance().getHtmQuest(player.getLang(), "quests/458_PerfectForm/32768-16c.htm");
				NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setHtml(content);
				html.replace("<\\?number\\?>", String.valueOf(String.valueOf(overhits)));
				player.sendPacket(html);
			}
		}
		else if(event.equals("32768-17.htm")) //Считаем награду и выставляем реюз квесту
		{
			calcReward(player);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.unset("cond");
			st.exitQuest(QuestType.DAILY);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int tempCount = st.getInt("over_tempcount");
		if(ArrayUtils.contains(_mobsGrendel, npc.getNpcId()))
		{
			int g0 = st.getInt("grendel_kill");
			int g1 = st.getInt("over_hit");

			if(g0 < 10)
			{
				g0++;
				if(((L2Attackable) npc).isOverhit())
				{
					g1++;
					tempCount++;
					st.set("over_tempcount", String.valueOf(tempCount));
					st.set("over_hit", String.valueOf(g1));
					st.set("grendel_kill", String.valueOf(g0));
				}
				else
				{
					st.set("grendel_kill", String.valueOf(g0)); //всего киллов моба
					st.set("count", String.valueOf(tempCount)); //записываем предыдущее комбо оверхитов
					st.set("over_tempcount", "0"); //Обнуляем временный счетчик оверхитов
				}
			}
		}
		else if(ArrayUtils.contains(_mobsBuffalo, npc.getNpcId()))
		{
			int g0 = st.getInt("buffalo_kill");
			int g1 = st.getInt("over_hit");

			if(g0 < 10)
			{
				g0++;
				if(((L2Attackable) npc).isOverhit())
				{
					g1++;
					tempCount++;
					st.set("over_tempcount", String.valueOf(tempCount));
					st.set("over_hit", String.valueOf(g1));
					st.set("buffalo_kill", String.valueOf(g0));
				}
				else
				{
					st.set("buffalo_kill", String.valueOf(g0));
					st.set("count", String.valueOf(tempCount)); //записываем предыдущее комбо оверхитов
					st.set("over_tempcount", "0"); //Обнуляем временный счетчик оверхитов
				}

			}
		}
		else if(ArrayUtils.contains(_mobsCougar, npc.getNpcId()))
		{
			int g0 = st.getInt("cougar_kill");
			int g1 = st.getInt("over_hit");
			if(g0 < 10)
			{
				g0++;
				if(((L2Attackable) npc).isOverhit())
				{
					g1++;
					tempCount++;
					st.set("over_tempcount", String.valueOf(tempCount));
					st.set("over_hit", String.valueOf(g1));
					st.set("cougar_kill", String.valueOf(g0));
				}
				else
				{
					st.set("cougar_kill", String.valueOf(g0));
					st.set("count", String.valueOf(tempCount)); //записываем предыдущее комбо оверхитов
					st.set("over_tempcount", "0"); //Обнуляем временный счетчик оверхитов
				}
			}
		}
		else if(ArrayUtils.contains(_mobsKookaburra, npc.getNpcId()))
		{
			int g0 = st.getInt("kookaburra_kill");
			int g1 = st.getInt("over_hit");
			if(g0 < 10)
			{
				g0++;
				if(((L2Attackable) npc).isOverhit())
				{
					g1++;
					tempCount++;
					st.set("over_tempcount", String.valueOf(tempCount));
					st.set("over_hit", String.valueOf(g1));
					st.set("kookaburra_kill", String.valueOf(g0));
				}
				else
				{
					st.set("kookaburra_kill", String.valueOf(g0));
					st.set("count", String.valueOf(tempCount)); //записываем предыдущее комбо оверхитов
					st.set("over_tempcount", "0"); //Обнуляем временный счетчик оверхитов
				}
			}
		}
		if(st.getInt("grendel_kill") + st.getInt("buffalo_kill") + st.getInt("cougar_kill") + st.getInt("kookaburra_kill") >= 40)
		{
			st.setCond(2);
		}
		if(st.getInt("over_hit") == 40) //Заглушка на случай совершения 40 из 40 оверхитов
		{
			st.set("count", "40");
		}
		return super.onKill(npc, st);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}
		switch(st.getState())
		{
			case CREATED:
				return player.getLevel() >= 82 ? "32768-01.htm" : "32768-nolvl.htm";
			case STARTED:
				if(st.getCond() == 1)
				{
					return "32768-12.htm";
				}
				if(st.getCond() == 2)
				{
					return "32768-13.htm";
				}
				break;
			case COMPLETED:
				return "32768-noday.htm";
		}
		return null;
	}

	private void calcReward(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st.getInt("over_hit") >= 20)
		{
			int i0 = Rnd.get(9);
			switch(i0)
			{
				case 0:
					st.giveItems(10373, 1);
					break;
				case 1:
					st.giveItems(10374, 1);
					break;
				case 2:
					st.giveItems(10375, 1);
					break;
				case 3:
					st.giveItems(10376, 1);
					break;
				case 4:
					st.giveItems(10377, 1);
					break;
				case 5:
					st.giveItems(10378, 1);
					break;
				case 6:
					st.giveItems(10379, 1);
					break;
				case 7:
					st.giveItems(10380, 1);
					break;
				case 8:
					st.giveItems(10381, 1);
					break;
			}
		}
		else if(st.getInt("over_hit") < 20 && st.getInt("over_hit") >= 7)
		{
			int i0 = Rnd.get(9);
			switch(i0)
			{
				case 0:
					st.giveItems(10397, 5);
					break;
				case 1:
					st.giveItems(10398, 5);
					break;
				case 2:
					st.giveItems(10399, 5);
					break;
				case 3:
					st.giveItems(10400, 5);
					break;
				case 4:
					st.giveItems(10401, 5);
					break;
				case 5:
					st.giveItems(10402, 5);
					break;
				case 6:
					st.giveItems(10403, 5);
					break;
				case 7:
					st.giveItems(10404, 5);
					break;
				case 8:
					st.giveItems(10405, 5);
					break;
			}
		}
		else if(st.getInt("over_hit") < 7)
		{
			int i0 = Rnd.get(9);
			switch(i0)
			{
				case 0:
					st.giveItems(10397, 2);
					break;
				case 1:
					st.giveItems(10398, 2);
					break;
				case 2:
					st.giveItems(10399, 2);
					break;
				case 3:
					st.giveItems(10400, 2);
					break;
				case 4:
					st.giveItems(10401, 2);
					break;
				case 5:
					st.giveItems(10402, 2);
					break;
				case 6:
					st.giveItems(10403, 2);
					break;
				case 7:
					st.giveItems(10404, 2);
					break;
				case 8:
					st.giveItems(10405, 2);
					break;
			}
			st.giveItems(15482, 10);
			st.giveItems(15483, 10);
		}
	}
}