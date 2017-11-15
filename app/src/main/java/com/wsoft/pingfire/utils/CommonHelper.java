package com.wsoft.pingfire.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by lia on 14/11/17.
 */

public class CommonHelper
{
    public static String getStackTraceAsString(Throwable thr)
    {
        if (thr != null)
        {
            StringWriter swrt = new StringWriter();
            PrintWriter pwrt = new PrintWriter(swrt);
            thr.printStackTrace(pwrt);
            return swrt.toString();
        }
        else
        {
            return null;
        }
    }

    public static void XCreateFile(String p_sPesan, String p_sPath)
    {
        try
        {
            String l_sContent = p_sPesan;

            File l_fPathOnly = new File(p_sPath);
            File l_fFile = new File(p_sPath);

            if (!l_fFile.exists())
            {
                l_fFile.createNewFile();
            }

            FileWriter fw = new FileWriter(l_fFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(l_sContent);
            bw.close();

        }
        catch (IOException e)
        {

        }
    }
}
