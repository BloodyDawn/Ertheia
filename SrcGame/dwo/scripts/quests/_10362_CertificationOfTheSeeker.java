package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.scripts.npc.teleporter.RaikelTeleport;
import gnu.trove.map.hash.TIntIntHashMap;

public class _10362_CertificationOfTheSeeker extends Quest
{
	//npc
	private static final int CHESHA = 33449;
	private static final int NAGEL = 33450;
	// mobs
	private static final int CRAWLER = 22991;
	private static final int STALKER = 22992;

	public _10362_CertificationOfTheSeeker()
	{
		addStartNpc(CHESHA);
		addTalkId(CHESHA, NAGEL);
		addKillId(CRAWLER, STALKER);
		addEnterZoneId(33011);
	}

	public static void main(String[] args)
	{
		new _10362_CertificationOfTheSeeker();
	}

	@Override
	public int getQuestId()
	{
		return 10362;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "si_illusion_chesha_q10362_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == CHESHA)
		{
			if(reply == 1)
			{
				return "si_illusion_chesha_q10362_04.htm";
			}
		}
		else if(npc.getNpcId() == NAGEL)
		{
			if(reply == 1)
			{
				return "si_illusion_nazel_q10362_02.htm";
			}
			if(reply == 2)
			{
				st.unset("crawler_kills");
				st.unset("stalker_kills");
				st.giveAdena(43000, true);
				st.addExpAndSp(50000, 12);
				st.giveItems(49, 1);
				st.giveItems(1060, 50);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "si_illusion_nazel_q10362_03.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(npc == null || st == null)
		{
			return super.onKill(npc, player, isPet);
		}
		if(st.getCond() == 1)
		{
			int CRAWLER_KILLS = st.getInt("_1");
			int STALKER_KILLS = st.getInt("_2");
			if(npc.getNpcId() == CRAWLER && CRAWLER_KILLS < 5)
			{
				CRAWLER_KILLS++;
				st.set("_1", String.valueOf(CRAWLER_KILLS));
			}
			else if(npc.getNpcId() == STALKER && STALKER_KILLS < 10)
			{
				STALKER_KILLS++;
				st.set("_2", String.valueOf(STALKER_KILLS));
			}

			// Display progress
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1022991, CRAWLER_KILLS);
			moblist.put(1022992, STALKER_KILLS);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));

			if(CRAWLER_KILLS >= 5 && STALKER_KILLS >= 10)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == CHESHA)
		{
			switch(st.getState())
			{
				case CREATED:
					if(canBeStarted(player))
					{
						return "si_illusion_chesha_q10362_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_illusion_chesha_q10362_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "si_illusion_chesha_q10362_06.htm";
					}
					if(st.getCond() == 2)
					{
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "si_illusion_chesha_q10362_07.htm";
					}
					if(st.getCond() == 3)
					{
						return "si_illusion_chesha_q10362_08.htm";
					}
					break;
				case COMPLETED:
					return "si_illusion_chesha_q10362_03.htm";
			}
		}
		else if(npc.getNpcId() == NAGEL)
		{
			if(st.getCond() == 3)
			{
				return "si_illusion_nazel_q10362_01.htm";
			}
			else if(st.isCompleted())
			{
				return "si_illusion_nazel_q10362_04.htm";
			}
		}
		return null;
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		L2PcInstance player = character.getActingPlayer();
		if(player == null)
		{
			return null;
		}

		// TODO наверно не зависит от квеста
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.isCreated() && !st.getBool(RaikelTeleport.class.getSimpleName()))
		{
			player.getVariablesController().set(RaikelTeleport.class.getSimpleName(), true);
			player.showQuestMovie(ExStartScenePlayer.SCENE_ENTERING_ESAGIR_RUINS);
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10361_RolesOfTheSeeker.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 10 && player.getLevel() <= 20;
	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1022991, st.getInt("_1"));
			moblist.put(1022992, st.getInt("_2"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
} 