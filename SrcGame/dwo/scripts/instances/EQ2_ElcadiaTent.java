package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.scripts.quests._10292_SevenSignsGirlofDoubt;
import dwo.scripts.quests._10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom;
import dwo.scripts.quests._10294_SevenSignsToTheMonasteryOfSilence;
import dwo.scripts.quests._10296_SevenSignsOneWhoSeeksThePowerOfTheSeal;

public class EQ2_ElcadiaTent extends Quest
{
	// Teleports
	private static final Location ENTRY_POINT = new Location(89706, -238074, -9632);
	private static final Location EXIT_POINT = new Location(43316, -87986, -2832);
	private static EQ2_ElcadiaTent _instance;

	public static void main(String[] args)
	{
		_instance = new EQ2_ElcadiaTent();
	}

	public static EQ2_ElcadiaTent getInstance()
	{
		return _instance;
	}

	protected void enterInstance(L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world != null)
		{
			if(!(world instanceof ElcadiaTentWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				player.teleToInstance(ENTRY_POINT, world.instanceId);
			}
		}
		else
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance("EQ2_ElcadiaTent.xml");

			world = new ElcadiaTentWorld();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.ELCADIAS_TENT.getId();
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);

			world.allowed.add(player.getObjectId());
			player.teleToInstance(ENTRY_POINT, instanceId);
		}
	}

	public String checkAndEnterToInstance(L2PcInstance player)
	{
		if(player.getQuestState(_10292_SevenSignsGirlofDoubt.class) != null && player.getQuestState(_10292_SevenSignsGirlofDoubt.class).isStarted())
		{
			enterInstance(player);
			return null;
		}
		else if(player.getQuestState(_10292_SevenSignsGirlofDoubt.class) != null && player.getQuestState(_10292_SevenSignsGirlofDoubt.class).isCompleted() && player.getQuestState(_10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom.class) == null)
		{
			enterInstance(player);
			return null;
		}
		else if(player.getQuestState(_10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom.class) != null && !player.getQuestState(_10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom.class).isCompleted())
		{
			enterInstance(player);
			return null;
		}
		else if(player.getQuestState(_10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom.class) != null && player.getQuestState(_10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom.class).isCompleted() && player.getQuestState(_10294_SevenSignsToTheMonasteryOfSilence.class) == null)
		{
			enterInstance(player);
			return null;
		}
		else if(player.getQuestState(_10296_SevenSignsOneWhoSeeksThePowerOfTheSeal.class) != null && player.getQuestState(_10296_SevenSignsOneWhoSeeksThePowerOfTheSeal.class).getCond() == 3)
		{
			enterInstance(player);
			return null;
		}
		else
		{
			return "ssq2_door_elcardia003.htm";
		}
	}

	private class ElcadiaTentWorld extends InstanceWorld
	{
		public ElcadiaTentWorld()
		{
		}
	}
}
