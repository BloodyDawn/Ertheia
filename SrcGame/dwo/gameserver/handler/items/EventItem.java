package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.instancemanager.games.HandysBlockCheckerManager;
import dwo.gameserver.instancemanager.games.HandysBlockCheckerManager.ArenaParticipantsHolder;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2BlockInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;

public class EventItem implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		boolean used = false;
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance activeChar = (L2PcInstance) playable;

		int itemId = item.getItemId();
		switch(itemId)
		{
			case 13787: // Handy's Block Checker Bond
				used = useBlockCheckerItem(activeChar, item);
				break;
			case 13788: // Handy's Block Checker Land Mine
				used = useBlockCheckerItem(activeChar, item);
				break;
			default:
				_log.log(Level.WARN, "EventItemHandler: Item with id: " + itemId + " is not handled");
		}
		return used;
	}

	private boolean useBlockCheckerItem(L2PcInstance castor, L2ItemInstance item)
	{
		int blockCheckerArena = castor.getEventController().getHandysBlockCheckerEventArena();
		if(blockCheckerArena == -1)
		{
			castor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
			return false;
		}

		L2Skill sk = item.getEtcItem().getSkills()[0].getSkill();
		if(sk == null)
		{
			return false;
		}

		if(!castor.destroyItem(ProcessType.EVENT, item, 1, castor, true))
		{
			return false;
		}

		L2BlockInstance block = (L2BlockInstance) castor.getTarget();

		ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(blockCheckerArena);
		if(holder != null)
		{
			int team = holder.getPlayerTeam(castor);
			for(L2PcInstance pc : block.getKnownList().getKnownPlayersInRadius(sk.getEffectRange()))
			{
				int enemyTeam = holder.getPlayerTeam(pc);
				if(enemyTeam != -1 && enemyTeam != team)
				{
					sk.getEffects(castor, pc);
				}
			}
			return true;
		}
		_log.log(Level.WARN, "Char: " + castor.getName() + '[' + castor.getObjectId() + "] has unknown block checker arena");
		return false;
	}
}
