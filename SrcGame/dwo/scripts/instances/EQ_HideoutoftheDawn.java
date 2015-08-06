package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;

public class EQ_HideoutoftheDawn extends Quest
{

	private static final String qn = "EQ_HideoutoftheDawn";
	private static final int WOOD = 32593;
	private static final int JAINA = 32582;
	private static final int[][] Npcs = {
		{32597, -24034, -8962, -5362, 0}, //misc stats: 333 278 50 50 15 24
		{32582, -24013, -8877, -5386, 0} //misc stats: 333 278 50 50 15 24
	};

	public EQ_HideoutoftheDawn()
	{

		addStartNpc(WOOD);
		addTalkId(WOOD);
		addTalkId(JAINA);
	}

	public static void main(String[] args)
	{
		new EQ_HideoutoftheDawn();
	}

	private void teleportplayer(L2PcInstance player, teleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.getInstanceController().setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
	}

	protected void exitInstance(L2PcInstance player, teleCoord tele)
	{
		player.getInstanceController().setInstanceId(0);
		player.teleToLocation(tele.x, tele.y, tele.z);
	}

	protected int enterInstance(L2PcInstance player, teleCoord teleto)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world != null)
		{
			if(!(world instanceof HoDWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleto.instanceId = world.instanceId;
			teleportplayer(player, teleto);
			return instanceId;
		}
		else
		{
			instanceId = InstanceManager.getInstance().createDynamicInstance(null);
			world = new HoDWorld();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.HIDEOUT_OF_THE_DAWN.getId();
			world.status = 0;
			((HoDWorld) world).storeTime[0] = System.currentTimeMillis();
			InstanceManager.getInstance().addWorld(world);
			_log.log(Level.INFO, "Instance Hideout of the Dawn created with id " + world.instanceId + " and created by player " + player.getName());
			teleto.instanceId = instanceId;
			teleportplayer(player, teleto);
			for(int[] spawn : Npcs)
			{
				addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
			}
			return instanceId;
		}
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		QuestState st = player.getQuestState(qn);
		if(st == null)
		{
			st = newQuestState(player);
		}
		if(npcId == WOOD)
		{
			teleCoord tele = new teleCoord();
			tele.x = -23759;
			tele.y = -8961;
			tele.z = -5385;
			enterInstance(player, tele);
		}
		else if(npcId == JAINA)
		{
			teleCoord tele = new teleCoord();
			tele.instanceId = 0;
			tele.x = 147081;
			tele.y = 23785;
			tele.z = -1990;
			exitInstance(player, tele);
		}
		return "";
	}

	private class HoDWorld extends InstanceWorld
	{
		public long[] storeTime = {0, 0};
	}

	private class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}
}