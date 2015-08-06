package dwo.gameserver.handler.items;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;

public class ChristmasTree implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2NpcTemplate template1 = null;

		switch(item.getItemId())
		{
			case 5560:
				template1 = NpcTable.getInstance().getTemplate(13006);
				break;
			case 5561:
				template1 = NpcTable.getInstance().getTemplate(13007);
				break;
		}

		if(template1 == null)
		{
			return false;
		}

		L2Object target = activeChar.getTarget();
		if(target == null)
		{
			target = activeChar;
		}

		try
		{
			L2Spawn spawn = new L2Spawn(template1);
			spawn.setLocx(target.getX());
			spawn.setLocy(target.getY());
			spawn.setLocz(target.getZ());
			spawn.spawnOne(false);

			activeChar.destroyItem(ProcessType.CONSUME, item.getObjectId(), 1, null, false);

			activeChar.sendMessage("Created " + template1.getName() + " at x: " + spawn.getLocx() + " y: " + spawn.getLocy() + " z: " + spawn.getLocz());
		}
		catch(Exception e)
		{
			activeChar.sendMessage("Цели нет в игре.");
		}
		return true;
	}
}
