/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ws.commons.attachments;

import org.apache.ws.commons.attachments.utils.ImageDataSource;
import org.apache.ws.commons.attachments.utils.ImageIO;
import org.apache.ws.commons.om.AbstractTestCase;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMFactory;
import org.apache.ws.commons.om.OMOutputFormat;
import org.apache.ws.commons.om.OMText;
import org.apache.ws.commons.om.impl.llom.OMElementImpl;
import org.apache.ws.commons.om.impl.llom.OMNamespaceImpl;
import org.apache.ws.commons.om.impl.llom.OMTextImpl;
import org.apache.ws.commons.om.impl.mtom.MTOMStAXSOAPModelBuilder;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ImageSampleTest extends AbstractTestCase {

    public ImageSampleTest(String testName) {
        super(testName);
    }

    /*
     * @see TestCase#setUp()
     */
    Image expectedImage;

    MTOMStAXSOAPModelBuilder builder;

    DataHandler expectedDH;

    File outMTOMFile;

    File outBase64File;

    String outFileName = "target/ActualImageMTOMOut.bin";

    String outBase64FileName = "target/OMSerializeBase64Out.xml";

    String imageInFileName = "mtom/img/test.jpg";

    String imageOutFileName = "target/testOut.jpg";

    String inMimeFileName = "mtom/ImageMTOMOut.bin";

    String contentTypeString = "multipart/Related; type=\"application/xop+xml\";start=\"<SOAPPart>\"; boundary=\"----=_AxIs2_Def_boundary_=42214532\"";



    public void testImageSampleSerialize() throws Exception {

        outMTOMFile = new File(outFileName);
        outBase64File = new File(outBase64FileName);
        OMOutputFormat mtomOutputFormat = new OMOutputFormat();
        mtomOutputFormat.setDoOptimize(true); 
        OMOutputFormat baseOutputFormat = new OMOutputFormat();
        baseOutputFormat.setDoOptimize(false);

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespaceImpl soap = new OMNamespaceImpl(
                "http://schemas.xmlsoap.org/soap/envelope/", "soap", fac);
        OMElement envelope = new OMElementImpl("Envelope", soap, fac);
        OMElement body = new OMElementImpl("Body", soap, fac);

        OMNamespaceImpl dataName = new OMNamespaceImpl(
                "http://www.example.org/stuff", "m", fac);
        OMElement data = new OMElementImpl("data", dataName, fac);

        expectedImage =
                new ImageIO().loadImage(
                        new FileInputStream(
                                getTestResourceFile(imageInFileName)));
        ImageDataSource dataSource = new ImageDataSource("WaterLilies.jpg",
                expectedImage);
        expectedDH = new DataHandler(dataSource);
        OMText binaryNode = new OMTextImpl(expectedDH, true, fac);

        envelope.addChild(body);
        body.addChild(data);
        data.addChild(binaryNode);

        envelope.serializeAndConsume(new FileOutputStream(outBase64File), baseOutputFormat);
        envelope.serializeAndConsume(new FileOutputStream(outMTOMFile), mtomOutputFormat);
    }

    public void testImageSampleDeserialize() throws Exception {
        InputStream inStream = new FileInputStream(
                getTestResourceFile(inMimeFileName));
        MIMEHelper mimeHelper = new MIMEHelper(inStream, contentTypeString);
        XMLStreamReader reader = XMLInputFactory.newInstance()
                .createXMLStreamReader(
                        new BufferedReader(
                                new InputStreamReader(
                                        mimeHelper
                .getSOAPPartInputStream())));
        builder = new MTOMStAXSOAPModelBuilder(reader, mimeHelper, null);
        OMElement root = builder.getDocumentElement();
        OMElement body = (OMElement) root.getFirstOMChild();
        OMElement data = (OMElement) body.getFirstOMChild();
        OMText blob = (OMText) data.getFirstOMChild();
        /*
         * Following is the procedure the user has to follow to read objects in
         * OBBlob User has to know the object type & whether it is serializable.
         * If it is not he has to use a Custom Defined DataSource to get the
         * Object.
         */

        DataHandler actualDH;
        actualDH = (DataHandler)blob.getDataHandler();
        Image actualObject = new ImageIO().loadImage(actualDH.getDataSource()
                .getInputStream());
        FileOutputStream imageOutStream = new FileOutputStream(
                new File(imageOutFileName));
        new ImageIO().saveImage("image/jpeg", actualObject, imageOutStream);

    }

}