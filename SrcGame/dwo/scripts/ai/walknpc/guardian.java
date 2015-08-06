package dwo.scripts.ai.walknpc;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 15.01.12
 * Time: 7:10
 * TODO: Болванка, не удалять пускай будет на всякий до лучших времен :D
 */
public class guardian extends Quest
{
	private static final int GUARDIAN = 33106;
	private static final int TEXT = 1032355;
	//Time Constants
	private static final long SECOND = 1000;
	private static final int[][] WALKS = {
		{-112424, 257813, -1534}, //1
		{-112934, 257783, -1310}, //2
		{-113890, 257765, -1165}, //3
		{-114137, 257385, -1164}, //4
		{-113890, 257793, -1164}, //5
		{-113311, 257791, -1164}, //6
		{-112910, 257796, -1310}, //7
		{-112401, 257794, -1538}, //8
		{-112297, 257690, -1534}, //9
		{-112609, 257329, -1529}, //10
		{-112761, 256812, -1510}, //11
		{-113451, 256512, -1533}, //12
		{-113640, 256292, -1533}, //13
		{-113790, 255997, -1540}, //14
		{-114207, 255827, -1538}, //15
		{-114476, 255815, -1538}, //16
		{-115063, 256176, -1538}, //17
		{-115548, 256638, -1538}, //18
		{-116077, 256793, -1542}, //19
		{-116278, 257493, -1538}, //20
		{-116413, 257801, -1539}, //21
		{-115845, 257795, -1310}, //22
		{-115758, 257799, -1310}, //23
		{-115428, 257793, -1164}, //24
		{-114903, 257597, -1164}, //25
		{-114570, 257332, -1164}  //26

	};
	private static int _isWalkTo;
	private static int X;
	private static int Y;
	private static int Z;
	private static boolean _isSpawned;

	public guardian()
	{
		int[] npc = {GUARDIAN};
		registerMobs(npc, QuestEventType.ON_SPAWN);
		/* Задержка перед стартом ИИ 1-на минута. */
		startQuestTimer("check_ai", 60000, null, null, true);

		_isSpawned = false;
		_isWalkTo = 1;
	}

	public static void main(String[] args)
	{
		//new guardian();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		X = WALKS[_isWalkTo - 1][0];
		Y = WALKS[_isWalkTo - 1][1];
		Z = WALKS[_isWalkTo - 1][2];

		if(event.equalsIgnoreCase("check_ai"))
		{
			cancelQuestTimer("check_ai", null, null);
			if(!_isSpawned)
			{
				L2Npc guardian_ai = SpawnTable.getInstance().getFirstSpawn(GUARDIAN).getLastSpawn();
				if(guardian_ai != null)
				{
					_isSpawned = true;
					ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new VoiceTask(), 10 * SECOND, 15 * SECOND);
					startQuestTimer("Start", 1000, guardian_ai, null, true);
					return super.onAdvEvent(event, npc, player);
				}
			}
		}
		else if(event.equalsIgnoreCase("Start"))
		{
			if(npc != null && _isSpawned)
			{
				if(npc.getNpcId() == GUARDIAN && npc.getX() - 50 <= X && npc.getX() + 50 >= X && npc.getY() - 50 <= Y && npc.getY() + 50 >= Y)
				{
					/*
					if(_isWalkTo == 2)
					{
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getNpcId(), NpcStringId.getNpcStringId(TEXT)));
					}
					//System.out.println("walk point: " + _isWalkTo);
					*/
					//TODO: Сделать чтобы ходил в обратном направлении, а то гг, прописывать обратный маршрут не катит.
					//TODO: Сделать чтобы нпц могли останавливаться и бегать.
					_isWalkTo++;
					if(_isWalkTo > 26)
					{
						_isWalkTo = 1;
					}

					X = WALKS[_isWalkTo - 1][0];
					Y = WALKS[_isWalkTo - 1][1];
					Z = WALKS[_isWalkTo - 1][2];
					npc.setWalking();
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(X, Y, Z, 0));
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc.getNpcId() == GUARDIAN)
		{
			_isSpawned = true;
			_isWalkTo = 1;
			startQuestTimer("Start", 1000, npc, null, true);
		}
		return super.onSpawn(npc);
	}

	private class VoiceTask implements Runnable
	{
		@Override
		public void run()
		{
			L2Npc npc = null;

			npc = SpawnTable.getInstance().getFirstSpawn(GUARDIAN).getLastSpawn();
			if(npc != null && npc.getNpcId() == GUARDIAN)
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.getNpcStringId(TEXT)));
			}
		}
	}
}
