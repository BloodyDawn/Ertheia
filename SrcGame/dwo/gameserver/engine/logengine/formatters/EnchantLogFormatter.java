package dwo.gameserver.engine.logengine.formatters;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.util.StringUtil;

public class EnchantLogFormatter
{
	public static String format(String message, Object[] params)
	{
		StringBuilder output = StringUtil.startAppend(30 + message.length() + (params == null ? 0 : params.length * 10), message);
		for(Object p : params)
		{
			if(p == null)
			{
				continue;
			}
			StringUtil.append(output, ", ");
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
				StringUtil.append(output, "item=[name=", ((L2ItemInstance) p).getItem().getName(), " obj=", String.valueOf(((L2ItemInstance) p).getObjectId()), " id=", String.valueOf(((L2ItemInstance) p).getItemId()), " count=", String.valueOf(((L2ItemInstance) p).getCount()), " enchant=", String.valueOf(((L2ItemInstance) p).getEnchantLevel()), "]");
			}
			else if(p instanceof L2Skill)
			{
				String enchant = "";
				if(((L2Skill) p).getLevel() > 100)
				{
					enchant = " enchant=" + ((L2Skill) p).getLevel() % 100;
				}
				StringUtil.append(output, "skill=[name=", ((L2Skill) p).getName(), " id=", String.valueOf(((L2Skill) p).getId()), " lvl=", String.valueOf(((L2Skill) p).getLevel()), enchant, "]");
			}
			else
			{
				StringUtil.append(output, p.toString());
			}
		}
		return output.toString();
	}
}
