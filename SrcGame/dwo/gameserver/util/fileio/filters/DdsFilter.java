package dwo.gameserver.util.fileio.filters;

import java.io.File;
import java.io.FileFilter;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.07.12
 * Time: 16:35
 */

public class DdsFilter implements FileFilter
{
	@Override
	public boolean accept(File file)
	{
		return file.getName().endsWith(".dds");
	}
}
