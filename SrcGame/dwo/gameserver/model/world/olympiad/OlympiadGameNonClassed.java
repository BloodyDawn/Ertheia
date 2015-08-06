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

import java.util.List;

/**
 * @author DS
 */
public class OlympiadGameNonClassed extends OlympiadGameNormal
{
	private OlympiadGameNonClassed(int id, Participant[] opponents)
	{
		super(id, opponents);
	}

	protected static OlympiadGameNonClassed createGame(int id, List<Integer> list)
	{
		Participant[] opponents = OlympiadGameNormal.createListOfParticipants(list);
		if(opponents == null)
		{
			return null;
		}

		return new OlympiadGameNonClassed(id, opponents);
	}

	@Override
	public CompetitionType getType()
	{
		return CompetitionType.NON_CLASSED;
	}

	@Override
	protected int getDivider()
	{
		return 5;
	}

	@Override
	protected int[][] getReward()
	{
		return Config.ALT_OLY_NONCLASSED_REWARD;
	}

	@Override
	protected String getWeeklyMatchType()
	{
		return COMP_DONE_WEEKLY_NON_CLASSED;
	}
}
