package org.apache.ws.commons.util;


/** An XML-RPC compliant variant of {@link XsDateTimeFormat}. Basically,
 * this is a standard xs:dateTime with no timezone specification.
 */
public class XmlRpcDateTimeFormat extends XsDateTimeFormat {
    private static final long serialVersionUID = 2338529740463497255L;

    /** Creates a new instance.
     */
    public XmlRpcDateTimeFormat() {
        super(true, true, false);
    }
}
