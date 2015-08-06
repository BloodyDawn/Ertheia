package dwo.gameserver.util.fileio.filters;

import java.io.File;
import java.io.FileFilter;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.11.11
 * Time: 17:09
 */

public class HtmFilter implements FileFilter
{
	@Override
	public boolean accept(File file)
	{
		return file.isDirectory() || file.getName().endsWith(".htm") || file.getName().endsWith(".html") || file.getName().endsWith(".pack");
	}
}
