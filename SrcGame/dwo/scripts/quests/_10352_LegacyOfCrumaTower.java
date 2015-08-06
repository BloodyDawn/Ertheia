package dwo.scripts.quests;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.06.12
 * Time: 0:37
 *
 * TODO: http://www.l2database.ws/2011/10/legacy-of-cruma-tower-quest-38/
 * TODO: FullRaid http://www.youtube.com/watch?v=QC91hUL-SDQ
 * TODO: http://l2central.info/wiki/%D0%9D%D0%B0%D1%81%D0%BB%D0%B5%D0%B4%D0%B8%D0%B5_%D0%91%D0%B0%D1%88%D0%BD%D0%B8_%D0%9A%D1%80%D1%83%D0%BC%D1%8B
 */

public class _10352_LegacyOfCrumaTower extends Quest
{
	// Квестовые персонажи
	private static final int Лиэй = 33155;
	private static final int Линкес = 33163;
	private static final int ТриггерНПЦ = 999999;
	private static final int СтранноеМеханическоеУстройство = 33158;
	private static final int Мартес = 25829;
	private static final int РемонтныйГолем = 25830;
	private static final int ТрупМортеса = 33292;

	// Квестовые предметы
	private static final int УстройствоПоискаСокровищ = 17619;
	private static final int ЯдроМортеса = 17728;

	// Разное
	private static final int СкиллУстройстваПоиска = 12004; // TODO: Верный ИД

	public _10352_LegacyOfCrumaTower()
	{
		setMinMaxLevel(38, 100);
		addStartNpc(Лиэй);
		addTalkId(Лиэй, Линкес, ТрупМортеса, СтранноеМеханическоеУстройство);
		addFirstTalkId(ТрупМортеса, СтранноеМеханическоеУстройство);
		addKillId(Мартес);
		addSkillSeeId(ТриггерНПЦ);
		questItemIds = new int[]{ЯдроМортеса, УстройствоПоискаСокровищ};
	}

	public static void main(String[] args)
	{
		new _10352_LegacyOfCrumaTower();
	}

	private void enterInstance(L2Party party)
	{
		// TODO: Вход в инстанс
		QuestState pst;
		for(L2PcInstance member : party.getMembers())
		{
			pst = member.getQuestState(getClass());
			if(pst != null && pst.getCond() > 3)
			{
				pst.setCond(4);
				pst.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 10352;
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
			case "33155-08.htm":
				st.startQuest();
				break;
			case "33155-11.htm":
				// TODO: Бафы игрока
				break;
			case "33155-12.htm":
				// TODO: Бафы слуги
				break;
			case "teleto_cruma":
				player.teleToInstance(new Location(17119, 114729, -3440), 0);
				return null;
			case "33163-09":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				if(!st.hasQuestItems(УстройствоПоискаСокровищ))
				{
					st.giveItem(УстройствоПоискаСокровищ);
				}
				break;
			case "33163-09c.htm":
				if(st.hasQuestItems(УстройствоПоискаСокровищ))
				{
					return "33163-09d.htm";
				}
				st.giveItem(УстройствоПоискаСокровищ);
				break;
			case "enter_instance":
				if(player.getParty() == null)
				{
					return "33158-02.htm";
				}
				// Проверяем группу
				for(L2PcInstance member : player.getParty().getMembers())
				{
					if(!Util.checkIfInRange(1000, player, member, true))
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(member));
						return null;
					}
				}
				enterInstance(player.getParty());
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "exit_instance":
				// TODO: Телепорт к устройству поискаа сокровищ в Круме в комнате РБ
				player.teleToInstance(new Location(16056, 114200, -3581), 0);
				return null;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(st.getCond() == 4)
		{
			if(npc.getNpcId() == Мартес)
			{
				if(killer.getParty() != null)
				{
					QuestState pst;
					for(L2PcInstance partyMember : killer.getParty().getMembers())
					{
						pst = partyMember.getQuestState(getClass());
						if(pst != null && pst.getCond() == 4)
						{
							pst.setCond(5);
							pst.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
					}
				}
				else
				{
					st.setCond(5);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Лиэй)
		{
			if(player.getLevel() < 38)
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "33155-02.htm";
			}
			switch(st.getState())
			{
				case COMPLETED:
					return "33155-03.htm";
				case CREATED:
					return "33155-01.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33155-10.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == Линкес)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "33163-03.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33163-01.htm";
					}
					if(st.getCond() >= 2 && st.getCond() < 5)
					{
						return !st.hasQuestItems(УстройствоПоискаСокровищ) ? "33163-09b.htm" : "33163-09a.htm";
					}
					if(st.getCond() == 5)
					{
						if(st.hasQuestItems(ЯдроМортеса))
						{
							st.addExpAndSp(480000, 312000);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.exitQuest(QuestType.ONE_TIME);
							return "33163-11.htm";
						}
						else
						{
							return "33163-10.htm";
						}
					}
					break;
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return super.onFirstTalk(npc, player);
		}

		if(npc.getNpcId() == СтранноеМеханическоеУстройство)
		{
			if(st.getCond() == 3 || st.getCond() == 4)
			{
				return "33158-01.htm";
			}
		}
		if(npc.getNpcId() == ТрупМортеса && st.getCond() == 5)
		{
			return "33292-01.htm";
		}

		return super.onFirstTalk(npc, player);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(skill.getId() == СкиллУстройстваПоиска)
		{
			if(npc.getNpcId() == ТриггерНПЦ && Util.checkIfInRange(skill.getSkillRadius(), caster, npc, true))
			{
				addSpawn(СтранноеМеханическоеУстройство, npc.getLoc(), 0, false, 120000);
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 38;

	}

	private class MartesWorld extends InstanceManager.InstanceWorld
	{
		public List<L2PcInstance> playersInInstance = new ArrayList<>();

		public MartesWorld()
		{
		}
	}
}