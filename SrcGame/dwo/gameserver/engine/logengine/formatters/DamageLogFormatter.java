package dwo.gameserver.engine.logengine.formatters;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.util.StringUtil;

public class DamageLogFormatter
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

			if(p instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) p;
				StringUtil.append(output, "char=", "[name=", player.getName(), " obj=" + player.getObjectId(), " lvl=", String.valueOf(player.getLevel()), " classId=", String.valueOf(player.getClassId()), "]");
			}
			else if(p instanceof L2Summon)
			{
				StringUtil.append(output, "summon=[name=", ((L2Character) p).getName(), " obj=", String.valueOf(((L2Character) p).getObjectId()), " lvl=", String.valueOf(((L2Character) p).getLevel()), " owner=", ((L2Summon) p).getName(), "(", String.valueOf(((L2Summon) p).getObjectId()), ")", "]");
			}
			else if(p instanceof L2Skill)
			{
				StringUtil.append(output, " with skill=", "[name=", ((L2Skill) p).getName(), " id=", String.valueOf(((L2Skill) p).getId()), " lvl=", String.valueOf(((L2Skill) p).getId()), "]");
			}
			else
			{
				StringUtil.append(output, p.toString());
			}
		}
		return output.toString();
	}
}
