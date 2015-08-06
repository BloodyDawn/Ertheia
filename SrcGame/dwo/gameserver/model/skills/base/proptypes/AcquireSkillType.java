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
package dwo.gameserver.model.skills.base.proptypes;

/**
 * Enumerate containing learning skill types.
 * @author Zoey76
 */
public enum AcquireSkillType
{
	Class(0),
	Fishing(1),
	Pledge(2),
	SubPledge(3),
	Transform(4),
	Transfer(5),
	SubClass(6),
	Collect(7),
	Race(8), // 57 корейцы дебилы в 1 пакете 57 во 2 8!!
	Dual(9),
    Alchemy(140),
	Null(-1);

	final int _type;

	private AcquireSkillType(int type)
	{
		_type = type;
	}

	public int getId()
	{
		return _type;
	}

    public static AcquireSkillType getAcquireSkillType(int id)
    {
        for (AcquireSkillType type : values())
        {
            if (type.getId() == id)
            {
                return type;
            }
        }
        return null;
    }
}
