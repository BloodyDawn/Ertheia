package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestTimer;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;

public class _10365_SeekerEscort extends Quest
{
	//npc
	private static final int DEP = 33453;
	private static final int SEBION = 32978;
	private static final int BLOODHOUND = 32988;
	private static final int[][] waypoint = {
		{-110602, 238963, -2920, 0}, {-110759, 239339, -2920, 0}, {-111059, 239779, -2920, 0},
		{-110760, 240218, -2920, 0}, {-110831, 240560, -2920, 0}, {-111309, 240342, -2920, 0},
		{-111572, 239939, -2920, 0}, {-112142, 239786, -2920, 0}, {-112635, 239934, -2920, 0},
		{-112673, 240211, -2920, 0}, {-112347, 240508, -2920, 0}, {-112267, 240277, -2920, 0},
		{-112267, 240277, -2920, 0}  // don't touch
	};
	/*
	{-112180, 234393, -3132, 0},
		{-112439, 234058, -3107, 0},
		{-112089, 233459, -3141, 0},
	 */
	private static final int[][] waypoint2 = {
		{-111066, 233798, -3200, 0}, {-111770, 234221, -3284, 0}, {-112180, 234393, -3132, 0},
		{-112439, 234058, -3107, 0}, {-112436, 233701, -3096, 0}, {-112182, 233490, -3120, 0},
		{-112124, 233130, -3136, 0}, {-112389, 232931, -3096, 0}, {-112689, 232566, -3072, 0},
		{-112716, 232359, -3072, 0}, {-112533, 232054, -3080, 0}, {-112308, 232084, -3104, 0},
		{-112071, 232359, -3136, 0}, {-111766, 232566, -3160, 0}, {-111219, 232723, -3224, 0},
		{-110813, 232482, -3256, 0}, {-110764, 232124, -3256, 0}, {-111152, 231842, -3224, 0},
		{-111472, 231976, -3200, 0}, {-111666, 231951, -3168, 0}, {-111734, 231831, -3175, 0}  // don't touch
	};
	private boolean spawned;
	private boolean spawned2;
	private int count;
	private int count2;
	private int patchpoint;
	private int patchpoint2;

	public _10365_SeekerEscort()
	{
		addStartNpc(DEP);
		addTalkId(DEP, SEBION);
		addEnterZoneId(33012);
	}

	public static void main(String[] args)
	{
		new _10365_SeekerEscort();
	}

	private void spawnWalker(L2Npc npc, L2PcInstance player)
	{
		L2Npc walker = addSpawn(BLOODHOUND, -110620, 238394, -2920, 0, false, 0, false);
		walker.setTitle(player.getName());
		walker.setIsRunning(true);
		walker.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(-110602, 238963, -2920, 0));
		patchpoint++;
		startQuestTimer("1", 4000, walker, player);
	}

	@Override
	public int getQuestId()
	{
		return 10365;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			spawned = true;
			spawnWalker(npc, player);
		}
		else if(event.equalsIgnoreCase("1"))
		{
			int[] p = waypoint[patchpoint];
			npc.sendPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), 17178340));
			if(npc.isInsideRadius(-112267, 240277, 200, false))
			{
				st.set("resc", "1");
				player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(17178341), ExShowScreenMessage.MIDDLE_CENTER, 2000));
				cancelQuestTimer("1", npc, player);
				npc.getLocationController().delete();
				count = 0;
				patchpoint = 0;
				spawned = false;
				return null;
			}
			else if(!npc.isInsideRadius(player, 200, true, false))
			{
				count++;
				player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(17178339), ExShowScreenMessage.MIDDLE_CENTER, 2000));
				if(count < 8)
				{
					startQuestTimer("1", 2000, npc, player);
					return null;
				}
				else if(count >= 8)
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(17178342), ExShowScreenMessage.MIDDLE_CENTER, 2000));
					npc.getLocationController().delete();
					spawned = false;
					count = 0;
					patchpoint = 0;
					return null;
				}
			}
			else
			{
				patchpoint++;
				count = 0;
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(p[0], p[1], p[2], p[3]));
				startQuestTimer("1", 4000, npc, player);
				return null;
			}
		}
		else if(event.equalsIgnoreCase("2"))
		{
			int[] p2 = waypoint2[patchpoint2];
			npc.sendPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), 17178340));
			if(npc.isInsideRadius(-111754, 231861, 200, false))
			{
				st.setCond(2);
				cancelQuestTimer("2", npc, player);
				npc.getLocationController().delete();
				st.unset("resc");
				count2 = 0;
				patchpoint2 = 0;
				spawned2 = false;
				return null;
			}
			else if(!npc.isInsideRadius(player, 200, true, false))
			{
				count2++;
				player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(17178339), ExShowScreenMessage.MIDDLE_CENTER, 2000));
				if(count2 < 8)
				{
					startQuestTimer("2", 2000, npc, player);
					return null;
				}
				else if(count2 >= 8)
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(17178342), ExShowScreenMessage.MIDDLE_CENTER, 2000));
					npc.getLocationController().delete();
					spawned2 = false;
					count2 = 0;
					patchpoint2 = 0;
					st.unset("resc");
					return null;
				}
			}
			else
			{
				patchpoint2++;
				count2 = 0;
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(p2[0], p2[1], p2[2], p2[3]));
				startQuestTimer("2", 4000, npc, player);
				return null;
			}
		}
		return event;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == DEP)
		{
			if(reply == 1)
			{
				return "si_illusion_def_q10365_02.htm";
			}
			else if(reply == 2)
			{
				if(!spawned && st.getInt("resc") != 1)
				{
					spawned = true;
					spawnWalker(npc, player);
				}
				return "si_illusion_def_q10365_06.htm";
			}
		}
		else if(npc.getNpcId() == SEBION)
		{
			if(reply == 3 && st.getCond() == 2)
			{
				st.giveAdena(65000, true);
				st.addExpAndSp(120000, 28);
				st.unset("resc");
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "si_illusion_sebion_q10365_02.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == DEP)
		{
			switch(st.getState())
			{
				case CREATED:
					if(canBeStarted(player))
					{
						return "si_illusion_def_q10365_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_illusion_def_q10365_04.htm";
					}
				case STARTED:
					if(st.getCond() == 1 && !spawned)
					{
						return "si_illusion_def_q10365_07.htm";
					}
					if(st.getCond() == 1 && spawned)
					{
						return "si_illusion_def_q10365_06.htm";
					}
					break;
				case COMPLETED:
					return "si_illusion_def_q10365_05.htm";
			}
		}
		else if(npc.getNpcId() == SEBION)
		{
			if(st.getCond() == 1)
			{
				return "si_illusion_sebion_q10365_05.htm";
			}
			else if(st.getCond() == 2)
			{
				return "si_illusion_sebion_q10365_01.htm";
			}
			else if(st.isCompleted())
			{
				return "si_illusion_sebion_q10365_04.htm";
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

		QuestTimer timer = getQuestTimer(character.getActingPlayer().getName(), null, character.getActingPlayer());
		QuestState st = player.getQuestState(getClass());

		if(st != null && st.getInt("resc") == 1 && !spawned2 && timer == null)
		{
			L2Npc walker = addSpawn(BLOODHOUND, -111108, 233911, -3219, 0, false, 0, false);
			spawned2 = true;
			count2 = 0;
			walker.setTitle(player.getName());
			walker.setIsRunning(true);
			walker.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(-111311, 234097, -3239, 0));
			patchpoint2++;
			startQuestTimer("2", 10000, walker, player);
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10364_ObligationsOfTheSeeker.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 16 && player.getLevel() <= 25;

	}
} 