/*
 * This file is part of UCLan-THC server.
 *
 *     UCLan-THC server is free software: you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License as
 *     published by the Free Software Foundation, either version 3 of
 *     the License, or (at your option) any later version.
 *
 *     UCLan-THC server is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.codecyprus.android_client;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

/**
 * User: Nearchos Paspallis
 * Date: 6/11/12
 * Time: 10:06 AM
 */
public class Installation
{
    private static String sID = null;
    private static final String INSTALLATION = "INSTALLATION";

    public synchronized static String id(Context context)
    {
        if (sID == null)
        {
            final File installation = new File(context.getFilesDir(), INSTALLATION);
            try
            {
                if (!installation.exists())
                {
                    writeInstallationFile(installation);
                }
                sID = readInstallationFile(installation);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException
    {
        final RandomAccessFile f = new RandomAccessFile(installation, "r");
        final byte [] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException
    {
        final FileOutputStream out = new FileOutputStream(installation);
        final String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }
}