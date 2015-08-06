package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.L2GameClient.GameClientState;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.RestartResponse;
import dwo.gameserver.network.game.serverpackets.packet.lobby.CharacterSelectionInfo;
import dwo.gameserver.network.game.serverpackets.packet.party.ExPartyPetWindowDelete;
import dwo.gameserver.network.game.serverpackets.packet.pet.PetDelete;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;
import org.apache.log4j.Level;

public class RequestRestart extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		if(player == null)
		{
			return;
		}

		if(player.getActiveEnchantItem() != null || player.getActiveEnchantAttrItem() != null)
		{
			sendPacket(RestartResponse.valueOf(false));
			return;
		}

		if(player.isLocked())
		{
			_log.log(Level.WARN, "Player " + player.getName() + " tried to restart during class change.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}

		if(player.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			player.sendMessage("Cannot restart while trading");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}

		if(AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) && !(player.isGM() && Config.GM_RESTART_FIGHTING))
		{
			player.sendPacket(SystemMessageId.CANT_RESTART_WHILE_FIGHTING);
			sendPacket(RestartResponse.valueOf(false));
			return;
		}

		// Remove player from Boss Zone
		player.removeFromBossZone();

		L2GameClient client = getClient();

		if(!player.getPets().isEmpty())
		{
			for(L2Summon summon : player.getPets())
			{
				if(player.getParty() != null)
				{
					player.getParty().broadcastPacket(player, new ExPartyPetWindowDelete(summon));
				}

				player.sendPacket(new PetDelete(summon.getSummonType(), summon.getObjectId()));
			}
		}

		// detach the client from the char so that the ThreadConnection isnt closed in the deleteMe
		player.setClient(null);

		player.getLocationController().delete();

		client.setActiveChar(null);

		// return the client to the authed status
		client.setState(GameClientState.AUTHED);

		sendPacket(RestartResponse.valueOf(true));

		// send char list
		CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}

	@Override
	public String getType()
	{
		return "[C] 46 RequestRestart";
	}
}