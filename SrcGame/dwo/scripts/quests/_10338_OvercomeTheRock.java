package dwo.scripts.quests;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.instancemanager.AwakeningManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExCallToChangeClass;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExChangeToAwakenedClass;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import dwo.scripts.instances.AQ_HarnakUndergroundRuinsA;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.12.11
 * Time: 8:24
 */

public class _10338_OvercomeTheRock extends Quest
{
	// Квестовые персонажи
	private static final int SELFIN = 33477;
	private static final int HADEL = 33344;
	private static final int HERMUNCUS = 33340;
	private static final int[] AWAKENING_SYMBOLS = {33397, 33398, 33399, 33400, 33401, 33402, 33403, 33404};
	private static final int[][] AWAKENING_SYMBOLS_DEPEND = {
		{33397, 148, 149, 150, 151}, {33398, 152, 153, 154, 155, 156, 157}, {33399, 158, 159, 160, 161},
		{33400, 162, 163, 164, 165}, {33401, 166, 167, 168, 169, 170}, {33402, 171, 172, 173, 174, 175},
		{33403, 176, 177, 178}, {33404, 179, 180, 181}
	};

	private static final int AFTERLIFE_SCROLL = 17600;

	public _10338_OvercomeTheRock()
	{
		addFirstTalkId(AWAKENING_SYMBOLS);
		addAskId(AWAKENING_SYMBOLS, 10338);
		addStartNpc(SELFIN);
		addTalkId(SELFIN, HADEL, HERMUNCUS);
		addAskId(SELFIN, 10338);
		addAskId(HADEL, 10338);
		addAskId(HERMUNCUS, 10338);
	}

	public static void main(String[] args)
	{
		new _10338_OvercomeTheRock();
	}

	@Override
	public int getQuestId()
	{
		return 10338;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !isCompleted(qs))
		{
			qs.startQuest();
			qs.getPlayer().sendPacket(new ExCallToChangeClass(qs.getPlayer().getClassId(), false));
			return "selphin_q10338_03.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		QuestState st = player.getQuestState(getClass());

		if(ArrayUtils.contains(AWAKENING_SYMBOLS, npc.getNpcId()))
		{
			switch(reply)
			{
				case 1: // Переродиться
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
				case 8:
					int essencesCount = AwakeningManager.getInstance().giveGiantEssences(player, true);
					String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + npc.getServerName() + "005.htm");
					content = content.replace("<?reward_SP?>", String.valueOf(Rnd.get(10000000))); //TODO: Пока наебос :D
					content = content.replace("<?reward_item_num?>", String.valueOf(essencesCount));
					return content;
				case 401: // Как тренироваться?
				case 402:
				case 403:
				case 404:
				case 405:
				case 406:
				case 407:
				case 408:
					return "awakening_reward_skill_" + player.getActiveClassId() + ".htm";
				case 201: // Переродиться
				case 202:
				case 203:
				case 204:
				case 205:
				case 206:
				case 207:
				case 208:
					AwakeningManager.getInstance().giveGiantEssences(player, false);
					player.getVariablesController().set("skillsDeleted-" + player.getClassIndex(), true);
					player.sendPacket(new ExChangeToAwakenedClass(Util.getAwakenRelativeClass(player.getClassId().getId())));
					break;
			}
		}
		else if(npc.getNpcId() == SELFIN)
		{
			switch(reply)
			{
				case 1:
					return "selphin_q10338_02.htm";
			}
		}
		else if(npc.getNpcId() == HADEL)
		{
			switch(reply)
			{
				case 1:
					return "hadel_q10338_02.htm";
				case 2:
					return "hadel_q10338_03.htm";
				case 3:
					return "hadel_q10338_04.htm";
				case 4:
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "hadel_q10338_05.htm";
				case 10:
					player.teleToLocation(-114962, 226564, -2864);
					//player.showQuestMovie(ExStartScenePlayer.SCENE_AWAKENING_VIEW); Глюченный ролик
					return null;
				case 5:
				case 11:
					AQ_HarnakUndergroundRuinsA.getInstance().enterInstance(player);
					return null;
			}
		}
		else if(npc.getNpcId() == HERMUNCUS)
		{
			switch(reply)
			{
				case 1:
					return "herumankos_q10338_02.htm";
				case 2:
					st.giveItems(AFTERLIFE_SCROLL, 1);
					st.unset("cond");
					player.getVariablesController().set("q10338_" + player.getClassIndex(), true);
					st.exitQuest(QuestType.ONE_TIME);
					InstanceManager.getInstance().destroyInstance(player.getInstanceId());
					player.teleToLocation(-114962, 226564, -2864);
					// TODO после видео крит (
					//player.showQuestMovie(ExStartScenePlayer.SCENE_AWAKENING_VIEW);
					return null;
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();

		L2PcInstance player = st.getPlayer();

		switch(npcId)
		{
			case SELFIN:
				switch(st.getState())
				{
					case COMPLETED:
						if(player.getVariablesController().get("q10338_" + player.getClassIndex(), Boolean.class, false))
						{
							return "selphin_q10338_05.htm";
						}
						if(player.getLevel() >= 85 && player.getClassId().level() == ClassLevel.THIRD.ordinal())
						{
							if(player.isSubClassActive() && !player.getSubclass().isDualClass())
							{
								return "selphin_q10338_04.htm";
							}
							if(!player.isAwakened())
							{
								return "selphin_q10338_01.htm";
							}
						}
						else
						{
							return "selphin_q10338_04.htm";
						}
						break;
					case CREATED:
						if(player.getLevel() >= 85 && player.getClassId().level() == ClassLevel.THIRD.ordinal())
						{
							if(player.isSubClassActive() && !player.getSubclass().isDualClass())
							{
								return "selphin_q10338_04.htm";
							}
							if(!player.isAwakened())
							{
								return "selphin_q10338_01.htm";
							}
						}
						else
						{
							return "selphin_q10338_04.htm";
						}
						break;
					case STARTED:
						return "selphin_q10338_06.htm";
				}
				break;
			case HADEL:
				if(player.getLevel() < 85)
				{
					return "hadel_q10338_06.htm";
				}
				if(player.isSubClassActive() && !player.getSubclass().isDualClass())
				{
					return "hadel_q10338_09.htm";
				}
				if(st.isStarted())
				{
					switch(st.getCond())
					{
						case 1:
							return "hadel_q10338_01.htm";
						case 2:
							return "hadel_q10338_08.htm";
						case 3:
							return "hadel_q10338_12.htm";
					}
				}
				else if(isCompleted(st) && player.getClassId().level() == ClassLevel.THIRD.ordinal())
				{
					return "hadel_q10338_07.htm";
				}
				else if(isCompleted(st) && player.getClassId().level() < ClassLevel.THIRD.ordinal())
				{
					return "hadel_q10338_10.htm";
				}
				break;
			case HERMUNCUS:
				if(player.isSubClassActive() && !player.getSubclass().isDualClass())
				{
					return "herumankos_q10338_04.htm";
				}
				if(st.getCond() == 3)
				{
					return "herumankos_q10338_01.htm";
				}
				if(isCompleted(st))
				{
					return "herumankos_q10338_03.htm";
				}
				break;
		}

		return getNoQuestMsg(player);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(player.isAwakened())
		{
			return npc.getServerName() + "004.htm";
		}

		boolean isSubclass = player.isSubClassActive();
		int classIdToAwakening = Util.getAwakenRelativeClass(isSubclass ? player.getSubclass().getClassId() : player.getClassId().getId());

		boolean isCheckOk = true;
		// Доступно только персонажу 85+ уровня, имеющего Записи Ада (возможно в дуал саб-классе)
		if(player.getLevel() < 85 || player.isSubClassActive() && !player.getSubclass().isDualClass() ||
			player.getItemsCount(AFTERLIFE_SCROLL) < 1 || classIdToAwakening < 0)
		{
			isCheckOk = false;
		}

		// Проверяем, что подошли к нужному НПЦ для пробуждения
		boolean isNpcCheckFailed = true;
		for(int[] reqData : AWAKENING_SYMBOLS_DEPEND)
		{
			if(reqData[0] == npc.getNpcId())
			{
				for(int classData : reqData)
				{
					if(classData == classIdToAwakening)
					{
						isNpcCheckFailed = false;
						break;
					}
				}
				if(!isNpcCheckFailed)
				{
					break;
				}
			}
		}

		if(isNpcCheckFailed || !isCheckOk)
		{
			// TODO: Отправка системного сообщения
			return npc.getServerName() + "002.htm";
		}

		if(!player.getPets().isEmpty())
		{
			return npc.getServerName() + "007.htm";
		}

		if(isSubclass)
		{
			if(player.getSubclass().isDualClass() && ClassId.getClassId(player.getActiveClassId()).level() == ClassLevel.THIRD.ordinal())
			{
				// Подкласс должен отличаться от основного класса.
				if(Util.getGeneralIdForAwaken(Util.getAwakenedClassForId(player.getActiveClassId())) == Util.getGeneralIdForAwaken(player.getClassId().getId()))
				{
					return npc.getServerName() + "008.htm";
				}
				if(player.getVariablesController().get("skillsDeleted-" + player.getClassIndex(), Boolean.class, false))
				{
					player.sendPacket(new ExChangeToAwakenedClass(classIdToAwakening));
				}
				else
				{
					return "awakening_reward_skill_" + player.getActiveClassId() + ".htm";
				}
			}
			else
			{
				return npc.getServerName() + "004.htm";
			}
		}
		else
		{
			if(player.getClassId().level() == ClassLevel.THIRD.ordinal())
			{
				if(player.getVariablesController().get("skillsDeleted-" + player.getClassIndex(), Boolean.class, false))
				{
					player.sendPacket(new ExChangeToAwakenedClass(classIdToAwakening));
				}
				else
				{
					return npc.getServerName() + Util.getAwakenRelativeClass(player.getClassId().getId()) + ".htm";
				}
			}
			else
			{
				return npc.getServerName() + "004.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return !(player.getLevel() < 85 || player.isSubClassActive() && !player.getSubclass().isDualClass() || player.isAwakened());
	}

	/***
	 * @param qs статус квеста
	 * @return {@code true} если квест был уже выполнен на этом саб-классе
	 */
	private boolean isCompleted(QuestState qs)
	{
		return qs.isCompleted() && qs.getPlayer().getVariablesController().get("q10338_" + qs.getPlayer().getClassIndex(), Boolean.class, false);
	}
}