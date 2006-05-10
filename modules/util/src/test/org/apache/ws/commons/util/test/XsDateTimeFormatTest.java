package org.apache.ws.commons.util.test;

import java.text.Format;
import java.util.Calendar;

import org.apache.ws.commons.util.XsDateTimeFormat;
import org.apache.ws.commons.util.XsTimeFormat;

import junit.framework.TestCase;


/** Test case for {@link XsDateTimeFormat}.
 */
public class XsDateTimeFormatTest extends TestCase {
    /** Tests, whether e zero as suffix matters in milliseconds.
     */
    public void testZeroSuffix() throws Exception {
        Format format = new XsDateTimeFormat();
        Calendar c1 = (Calendar) format.parseObject("2006-05-03T15:29:17.15Z");
        Calendar c2 = (Calendar) format.parseObject("2006-05-03T15:29:17.150Z");
        assertEquals(c1, c2);

        format = new XsTimeFormat();
        c1 = (Calendar) format.parseObject("15:29:17.15Z");
        c2 = (Calendar) format.parseObject("15:29:17.150Z");
        assertEquals(c1, c2);
    }
}
