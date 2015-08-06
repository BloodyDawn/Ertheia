package dwo.scripts.npc;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import org.apache.commons.lang3.ArrayUtils;

/***
 * TODO: Переделать вместе с Эпик квестом вторым по офу
 */
public class FirstTalkNpc extends Quest
{
	private static final int ErisEvilThoughts = 32792;

	private static final int[] NPC_LIST = {
		ErisEvilThoughts,
	};

	public FirstTalkNpc()
	{
		addStartNpc(NPC_LIST);
		addFirstTalkId(NPC_LIST);
	}

	public static void main(String[] args)
	{
		new FirstTalkNpc();
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();

		InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());

		if(npcId == ErisEvilThoughts)
		{
			if(ArrayUtils.contains(new int[]{151}, world.templateId))
			{
				return "32792-monastery.html";
			}
			else
			{
				return ArrayUtils.contains(new int[]{
					157
				}, world.templateId) ? "32792-underground.html" : "32792-default.html";
			}
		}

		return null;
	}
}