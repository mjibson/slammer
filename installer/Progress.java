/*
 * Progress.java
 * Copyright (C) 1999, 2001 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package installer;

/*
 * An interface for reporting installation progress. ConsoleProgress and
 * SwingProcess are the two existing implementations.
 */
public interface Progress
{
	public void setMaximum(int max);
	public void setMaximumDB(int max);

	public void advance(int value);
	public void advanceDB();

	public void done();

	public void error(String message);
}
