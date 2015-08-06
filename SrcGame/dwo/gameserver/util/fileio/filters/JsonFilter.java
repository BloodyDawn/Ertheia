package dwo.gameserver.util.fileio.filters;

import java.io.File;
import java.io.FileFilter;

/**
 * L2GOD team
 * User: ANZO
 * Date: 30.05.13
 * Time: 6:26
 */

public class JsonFilter implements FileFilter
{
	@Override
	public boolean accept(File pathname)
	{
		return pathname.getName().endsWith(".json");
	}
}
