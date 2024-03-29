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
package dwo.gameserver.model.world.olympiad;

import dwo.config.Config;
import dwo.gameserver.util.Rnd;

import java.util.List;

/**
 * @author DS
 */
public class OlympiadGameClassed extends OlympiadGameNormal
{
	private OlympiadGameClassed(int id, Participant[] opponents)
	{
		super(id, opponents);
	}

	protected static OlympiadGameClassed createGame(int id, List<List<Integer>> classList)
	{
		if(classList == null || classList.isEmpty())
		{
			return null;
		}

		List<Integer> list;
		Participant[] opponents;
		while(!classList.isEmpty())
		{
			list = classList.get(Rnd.get(classList.size()));
			if(list == null || list.size() < 2)
			{
				classList.remove(list);
				continue;
			}

			opponents = OlympiadGameNormal.createListOfParticipants(list);
			if(opponents == null)
			{
				classList.remove(list);
				continue;
			}

			return new OlympiadGameClassed(id, opponents);
		}
		return null;
	}

	@Override
	public CompetitionType getType()
	{
		return CompetitionType.CLASSED;
	}

	@Override
	protected int getDivider()
	{
		return 3;
	}

	@Override
	protected int[][] getReward()
	{
		return Config.ALT_OLY_CLASSED_REWARD;
	}

	@Override
	protected String getWeeklyMatchType()
	{
		return COMP_DONE_WEEKLY_CLASSED;
	}
}