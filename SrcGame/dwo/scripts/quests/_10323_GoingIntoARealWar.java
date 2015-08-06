package dwo.scripts.quests;

import dwo.gameserver.instancemanager.WalkingManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.TutorialShowHtml;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;

/**
 * L2-GodWorld Team
 * @author ANZO, Yukio
 * Date: 18.11.11
 * Time: 7:45
 */

public class _10323_GoingIntoARealWar extends Quest
{
	// Npcs
	private static final int EVAIN = 33464;
	private static final int HOLDEN = 33194;
	private static final int GUARD = 33021;
	private static final int SHANNON = 32974;
	private static final int ARENA_WALKER = 33016;

	// Coords
	private static final int[] WALKER_LEFT = {-110984, 253663, -1773};
	private static final int[] WALKER_RIGHT = {-110618, 253655, -1792};

	// Mobs
	private static final int CRAWLER = 23113;

	// Items
	private static final int TRAINING_KEY = 17574;

	public _10323_GoingIntoARealWar()
	{
		addStartNpc(EVAIN, GUARD);
		addTalkId(EVAIN, HOLDEN, GUARD, SHANNON);
		addFirstTalkId(GUARD);
		addKillId(CRAWLER);
		questItemIds = new int[]{TRAINING_KEY};
	}

	public static void main(String[] args)
	{
		new _10323_GoingIntoARealWar();
	}

	@Override
	public int getQuestId()
	{
		return 10323;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		L2PcInstance player = qs.getPlayer();
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			qs.set("10323_kills", "0");
			qs.giveItems(TRAINING_KEY, 1);
			L2Npc arenaWalker = addSpawn(ARENA_WALKER, new Location(-110802, 253711, -1788));
			arenaWalker.broadcastPacket(new NS(arenaWalker.getObjectId(), ChatType.NPC_ALL, arenaWalker.getNpcId(), 1032302).addStringParameter(player.getName()));
			arenaWalker.setTarget(player);
			double distLeft = player.getDistanceSq(WALKER_LEFT[0], WALKER_LEFT[1], WALKER_LEFT[2]);
			double distRight = player.getDistanceSq(WALKER_RIGHT[0], WALKER_RIGHT[1], WALKER_RIGHT[2]);
			if(distLeft < distRight)
			{
				WalkingManager.getInstance().startMoving(arenaWalker, 16);
			}
			else
			{
				WalkingManager.getInstance().startMoving(arenaWalker, 38);
			}
			return "si_illusion_evein_q10323_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		int npcId = npc.getNpcId();
		int cond = st.getCond();
		switch(npcId)
		{
			case EVAIN:
				if(reply == 1)
				{
					return "si_illusion_evein_q10323_04.htm";
				}
				break;
			case GUARD:
				if(reply == 1 && cond == 3)
				{
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.giveItems(1835, 500); // Soulshot: No Grade
					player.sendPacket(new TutorialShowHtml(TutorialShowHtml.CLIENT_SIDE, "..\\L2text\\QT_003_bullet_01.htm"));
					return "si_illusion_people2_q10323_04.htm";
				}
				if(reply == 2 && cond == 3)
				{
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.giveItems(2509, 500); // Spiritshot: No Grade
					player.sendPacket(new TutorialShowHtml(TutorialShowHtml.CLIENT_SIDE, "..\\L2text\\QT_003_bullet_01.htm"));
					return "si_illusion_people2_q10323_05.htm";
				}
				break;
			case SHANNON:
				if(reply == 1 && cond == 8)
				{
					st.giveAdena(9000, true);
					st.takeItems(TRAINING_KEY, -1);
					st.addExpAndSp(300, 5);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					return "si_illusion_shannon_q10323_02.htm";
				}
				break;
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

		int kills = st.getInt("10323_kills");

		if(st.getCond() == 2)
		{
			kills++;
			st.set("10323_kills", String.valueOf(kills));
			if(kills == 4)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.set("10323_kills", "0");
			}
		}
		else if(st.getCond() == 6)
		{
			kills++;
			st.set("10323_kills", String.valueOf(kills));
			if(kills == 4)
			{
				st.setCond(8);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.set("10323_kills", "0");
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == EVAIN)
		{
			switch(st.getState())
			{
				case CREATED:
					if(canBeStarted(player))
					{
						return "si_illusion_evein_q10323_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_illusion_evein_q10323_03.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "si_illusion_evein_q10323_06.htm";
					}
					if(st.getCond() == 8)
					{
						return "si_illusion_evein_q10323_08.htm";
					}
					break;
				case COMPLETED:
					return "si_illusion_evein_q10323_02.htm";
			}
		}
		else if(npc.getNpcId() == SHANNON)
		{
			if(st.getCond() == 1)
			{
				return "si_illusion_shannon_q10323_05.htm";
			}
			else if(st.getCond() == 8)
			{
				return "si_illusion_shannon_q10323_01.htm";
			}
			else
			{
				return st.isCompleted() ? "si_illusion_shannon_q10323_04.htm" : "si_illusion_shannon_q10323_03.htm";
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
			return null;
		}

		int cond = st.getCond();
		if(npc.getNpcId() == GUARD)
		{
			switch(cond)
			{
				case 2:
					return "si_illusion_people2_q10323_01.htm";
				case 3:
					return player.isMageClass() ? "si_illusion_people2_q10323_03.htm" : "si_illusion_people2_q10323_02.htm";
				case 4:
					if(player.isSoulShotActivated())
					{
						// TODO: Пропущен 5-ый пункт - он получается тогда, когда включаешь соски выданные в 4-ом
						// Делать общий notify не вариант - идиотизм при каждом юзе соски уведомлять об этом квест-двиг
						// Возможно стоит сделать отдельный хук в квест, позже что-нибудь придумаю.
						st.setCond(6);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						spawnMonsters(player);
						return player.isMageClass() ? "si_illusion_people2_q10323_07.htm" : "si_illusion_people2_q10323_06.htm";
					}
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1032351), ExShowScreenMessage.TOP_CENTER, 3000));
				case 6:
					return player.isMageClass() ? "si_illusion_people2_q10323_07.htm" : "si_illusion_people2_q10323_06.htm";
				case 8:
					return "si_illusion_people2_q10323_08.htm";
			}
		}

		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10322_SearchingForMysteriousForce.class);
		return previous != null && previous.isCompleted() && player.getLevel() < 20;
	}

	private void spawnMonsters(L2PcInstance player)
	{
		addSpawn(CRAWLER, -114941, 247946, -7872, 25269, false, 0, false, player.getInstanceId());
		addSpawn(CRAWLER, -115036, 248152, -7872, 16500, false, 0, false, player.getInstanceId());
		addSpawn(CRAWLER, -114542, 248512, -7872, 0, false, 0, false, player.getInstanceId());
		addSpawn(CRAWLER, -114050, 248393, -7872, 0, false, 0, false, player.getInstanceId());
	}
}
