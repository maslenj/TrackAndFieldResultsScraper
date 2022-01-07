import java.io.*;
import java.net.*;

/**
 * This is a simple class for reading from web URLs in Java.
 *
 @author Andrew Merrill, May 2007
 @version 1.0

 <pre>
 Example:

 // create a new SimpleURL object
 SimpleURL myurl = new SimpleURL("http://www.catlin.edu");

 // read the contents of the web site
 String webpage = myurl.readText();

 </pre>
 */



public class SimpleURL
{
    private final URL url;

    /*************************************************************************
     * Construct a new SimpleURL from a String.
     *
     * @param urlstring The URL to read from.
     */

    public SimpleURL(String urlstring)
    {
        try {
            url = new URL(urlstring);
        }
        catch (MalformedURLException e)
        {
            throw new SimpleURLException(e.getMessage());
        }
    }

    /*************************************************************************
     * read and return the contents of the web site
     */

    public String readText()
    {
        try {
            InputStream inputStream = url.openStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuilder alltext = new StringBuilder();
            String nextline = "";
            while (nextline != null)
            {
                nextline = reader.readLine();
                if (nextline != null)
                {
                    alltext.append(nextline);
                }
            }
            return alltext.toString();
        }
        catch (Exception e)
        {
            throw new SimpleURLException(e.getMessage());
        }
    }

//////////////////////////////////////////////////////////////////////////////////////////

    public static class SimpleURLException extends RuntimeException
    {
        SimpleURLException(String message)
        {
            super(message);
        }
    }
}