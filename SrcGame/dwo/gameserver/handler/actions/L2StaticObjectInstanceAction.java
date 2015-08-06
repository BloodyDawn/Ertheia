package dwo.gameserver.handler.actions;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.handler.IActionHandler;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2StaticObjectInstance;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import org.apache.log4j.Level;

public class L2StaticObjectInstanceAction implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		L2StaticObjectInstance staticObject = (L2StaticObjectInstance) target;
		if(staticObject.getType() < 0)
		{
			_log.log(Level.WARN, "L2StaticObjectInstance: StaticObject with invalid type! StaticObjectId: " + staticObject.getStaticObjectId());
		}

		// Check if the L2PcInstance already target the L2NpcInstance
		if(activeChar.getTarget() != staticObject)
		{
			// Set the target of the L2PcInstance activeChar
			activeChar.setTarget(staticObject);
			activeChar.sendPacket(new MyTargetSelected(staticObject.getObjectId(), 0));
		}
		else if(interact)
		{
			activeChar.sendPacket(new MyTargetSelected(staticObject.getObjectId(), 0));

			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if(activeChar.isInsideRadius(staticObject, L2Npc.INTERACTION_DISTANCE, false, false))
			{
				if(staticObject.getType() == 2)
				{
					String content = HtmCache.getInstance().getHtm(activeChar.getLang(), "default/" + staticObject.getName() + "001.htm");
					NpcHtmlMessage html = new NpcHtmlMessage(staticObject.getObjectId());

					if(content == null)
					{
						html.setHtml("<html><body>Нет данных о доске:<br>" + staticObject.getName() + "</body></html>");
					}
					else
					{
						html.setHtml(content);
					}

					activeChar.sendPacket(html);
				}
				else if(staticObject.getType() == 0)
				{
					activeChar.sendPacket(staticObject.getMap());
				}
			}
			else
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, staticObject);
			}
		}
		return true;
	}

	@Override
	public Class<? extends L2Object> getInstanceType()
	{
		return L2StaticObjectInstance.class;
	}
}