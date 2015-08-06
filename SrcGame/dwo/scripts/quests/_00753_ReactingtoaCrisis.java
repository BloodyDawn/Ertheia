package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 28.01.13
 * Time: 16:30
 *
 * http://power.plaync.co.kr/lineage2/%EC%9C%84%EA%B8%B0%EC%97%90+%EB%8C%80%EC%B2%98%ED%95%98%EB%8A%94+%EC%9E%90%EC%84%B8
 * http://l2central.info/wiki/%D0%9A%D1%80%D0%B0%D0%B9%D0%BD%D0%B8%D0%B5_%D0%BC%D0%B5%D1%80%D1%8B
 *
 * TODO: Каким образом появляются устройства создания големов?
 * TODO: Что делают устройства создания големов, если на них не использовать вакцину? (спаунят постоянно големмов, или только при убийстве)
 * TODO: Вакцина квестовый предмет или можно использовать вне квеста?
 */

public class _00753_ReactingtoaCrisis extends Quest
{
	// Квестовые персонажи
	private static final int Berna = 33796;

	// Квестовые предметы
	private static final int Key = 36054;
	private static final int Vaccina = 36065;

	// Квестовые монстры
	private static final int GolemGenerator = 19296;
	private static final int GolemScout = 23268;
	private static final int[] Mobs = {23270, 23271, 23272, 23273, 23274, 23275, 23276};

	// Переменные
	private static final int VaccinaSkill = 9584;

	public _00753_ReactingtoaCrisis()
	{
		addStartNpc(Berna);
		addTalkId(Berna);
		addKillId(GolemGenerator);
		addKillId(GolemScout);
		addKillId(Mobs);
		questItemIds = new int[]{Key, Vaccina};
	}

	public static void main(String[] args)
	{
		new _00753_ReactingtoaCrisis();
	}

	private void giveItem(QuestState st, L2Npc npc)
	{
		if(st != null && st.getCond() == 1)
		{
			// Обычные мобы, с которых добываются ключи
			if(ArrayUtils.contains(Mobs, npc.getNpcId()))
			{
				if(Rnd.getChance(50))
				{
					if(st.getQuestItemsCount(Key) < 30)
					{
						st.giveItem(Key);
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
				// Проверяем общий прогресс квеста
				if(st.getQuestItemsCount(Key) >= 30 && st.getInt("1019296") >= 5)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(2);
				}
			}
			// Големы-разведчики, из которых появляется Генератор Големов при убийстве
			else if(npc.getNpcId() == GolemScout)
			{
				// TODO: Скорее всего нужно вынести спаун генератора в отдельное АИ, т.к. спаун из них может идти наверное и вне квеста
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 753;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			if(qs.isCompleted())
			{
				return "berna_q0753_03.htm";
			}
			else
			{
				qs.startQuest();
				return "berna_q0753_04.htm";
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

		if(npcId == Berna)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "berna_q0753_03.htm";
				case CREATED:
					QuestState pst = st.getPlayer().getQuestState(_10386_MysteriousJourney.class);
					return st.getPlayer().getLevel() < 95 || pst == null || !pst.isCompleted() ? "berna_q0753_02.htm" : "berna_q0753_01.htm";
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
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		// TODO: Вынести в отдельное АИ, после уточнения инфомации по механике работы генераторов големов
		if(npc.getNpcId() == GolemGenerator && skill.getId() == VaccinaSkill)
		{
			QuestState st = caster.getQuestState(getClass());
			if(st != null && st.getCond() == 1)
			{
				if(st.getInt("1019296") < 5)
				{
					st.set("1019296", String.valueOf(st.getInt("1019296") + 1));
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				npc.doDie(caster);

				// Проверяем общий прогресс квеста
				if(st.getQuestItemsCount(Key) >= 30 && st.getInt("1019296") >= 5)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(2);
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pst = player.getQuestState(_10386_MysteriousJourney.class);
		return player.getLevel() >= 93 && pst != null && pst.isCompleted();
	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1023212, st.getInt("1019296"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}