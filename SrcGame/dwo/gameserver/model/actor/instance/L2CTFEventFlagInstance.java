package dwo.gameserver.model.actor.instance;

import dwo.config.events.ConfigEventCTF;
import dwo.gameserver.instancemanager.events.CTF.CTFEvent;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;

public class L2CTFEventFlagInstance extends L2Npc
{
	int randomX;
	int randomY;
	int spawnX;
	int spawnY;

	public L2CTFEventFlagInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(L2PcInstance player, boolean interract)
	{
		if(!canTarget(player))
		{
			return;
		}

		if(equals(player.getTarget()))
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if(!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else if(canInteract(player))
			{
				flagFuncs(player);
			}
		}
		else
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			// The color to display in the select window is White
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			// Send a Server->Client packet ValidateLocation to correct the L2ArtefactInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendActionFailed();
	}

	private void flagFuncs(L2PcInstance player)
	{
		if(CTFEvent.isStarted() && CTFEvent.isPlayerParticipant(player.getObjectId()))
		{
			byte teamId = CTFEvent.getParticipantTeamId(player.getObjectId());
			if(teamId == -1)
			{
				return;
			}
			// when the flag npc belongs to the player's team
			if(teamId == 0)
			{
				if(getNpcId() == ConfigEventCTF.CTF_EVENT_TEAM_1_FLAG_NPC_ID)
				{
					CTFEvent.addPointsToPlayerTeam(player);
				}

				else if(getNpcId() == ConfigEventCTF.CTF_EVENT_TEAM_2_FLAG_NPC_ID)
				{
					if(CTFEvent.teamFlagOnPlace((byte) 1))
					{
						CTFEvent.addFlagToPlayer(player);
					}
					else
					{
						player.sendMessage("The flag is already taken.");
					}
				}
			}
			if(teamId == 1)
			{
				if(getNpcId() == ConfigEventCTF.CTF_EVENT_TEAM_2_FLAG_NPC_ID)
				{
					CTFEvent.addPointsToPlayerTeam(player);
				}

				else if(getNpcId() == ConfigEventCTF.CTF_EVENT_TEAM_1_FLAG_NPC_ID)
				{
					if(CTFEvent.teamFlagOnPlace((byte) 0))
					{
						CTFEvent.addFlagToPlayer(player);
					}
					else
					{
						player.sendMessage("The flag is already taken.");
					}
				}
			}
		}
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		spawnX = getX();
		spawnY = getY();
	}
}