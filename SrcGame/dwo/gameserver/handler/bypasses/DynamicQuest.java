/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.handler.bypasses;

import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.instancemanager.DynamicQuestManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * Dynamic quest commands handler.
 *
 * @author Yorie
 */
public class DynamicQuest extends CommandHandler<String>
{
	@TextCommand("dynamic_quest_accept")
	public boolean accept(BypassHandlerParams params)
	{
		dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest quest = getQuest(params);

		if(quest == null)
		{
			return false;
		}

		quest.addParticipiant(params.getPlayer());
		return true;
	}

	@TextCommand("dynamic_quest_reject")
	public boolean reject(BypassHandlerParams params)
	{
		dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest quest = getQuest(params);

		if(quest == null)
		{
			return false;
		}

		quest.showDialog(params.getPlayer(), "reject");
		return true;
	}

	@TextCommand("dynamic_quest_reward")
	public boolean reward(BypassHandlerParams params)
	{
		dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest quest = getQuest(params);

		if(quest == null)
		{
			return false;
		}

		quest.giveReward(params.getPlayer());
		return true;
	}

	@TextCommand("dynamic_quest_ranking")
	public boolean ranking(BypassHandlerParams params)
	{
		dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest quest = getQuest(params);

		if(quest == null)
		{
			return false;
		}

		L2PcInstance activeChar = params.getPlayer();
		if(quest.isParticipiant(activeChar))
		{
			quest.sendResults(activeChar);
			quest.sendRewardInfo(activeChar);
			return true;
		}

		return true;
	}

	private dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest getQuest(BypassHandlerParams params)
	{
		if(!params.getQueryArgs().containsKey("dquest_id") || !params.getQueryArgs().containsKey("step_id"))
		{
			return null;
		}

		int questId;
		int stepId;
		try
		{
			questId = Integer.parseInt(params.getQueryArgs().get("dquest_id"));
			stepId = Integer.parseInt(params.getQueryArgs().get("step_id"));
		}
		catch(Exception e)
		{
			return null;
		}

		return DynamicQuestManager.getInstance().getQuest(questId, stepId);
	}
}