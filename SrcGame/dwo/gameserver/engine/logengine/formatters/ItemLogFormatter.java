package dwo.gameserver.engine.logengine.formatters;

import dwo.gameserver.model.actor.instance.L2NpcInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.util.StringUtil;

public class ItemLogFormatter
{
	public static String format(String message, Object[] params)
	{
		int p_len = params == null ? 0 : params.length;
		StringBuilder output = StringUtil.startAppend(30 + message.length() + p_len * 50, message);
		if(p_len > 0)
		{
			for(Object p : params)
			{
				if(p == null)
				{
					continue;
				}
				output.append(',');
				if(p instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) p;
					StringUtil.append(output, "char=", "[name=", player.getName(), " obj=" + player.getObjectId() + " acc=" + player.getAccountName());
					if(player.getClient() != null && !player.getClient().isDetached())
					{
						StringUtil.append(output, " ip=", player.getClient().getConnection().getInetAddress().getHostAddress());
					}
					StringUtil.append(output, "]");
				}
				else if(p instanceof L2ItemInstance)
				{
					String enchant = "";
					if(((L2ItemInstance) p).getEnchantLevel() > 0)
					{
						enchant = " enchant=" + ((L2ItemInstance) p).getEnchantLevel();
					}
					StringUtil.append(output, "item=[name=", ((L2ItemInstance) p).getItem().getName(), " obj=", String.valueOf(((L2ItemInstance) p).getObjectId()), " id=", String.valueOf(((L2ItemInstance) p).getItemId()), " count=", String.valueOf(((L2ItemInstance) p).getCount()), enchant, "]");
				}
				else if(p instanceof L2NpcInstance)
				{
					L2NpcInstance obj = (L2NpcInstance) p;
					StringUtil.append(output, "npc=", "[name=", obj.getName(), " id=" + obj.getNpcId() + ']');
				}
				else
				{
					output.append(p);
				}
			}
		}
		return output.toString();
	}
}
