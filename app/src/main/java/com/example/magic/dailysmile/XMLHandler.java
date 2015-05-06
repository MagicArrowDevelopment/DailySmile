package com.example.magic.dailysmile;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joel on 5/5/2015.
 */
public class XMLHandler extends DefaultHandler {
    List<XMLData> datalist;
    XMLData data;
    StringBuilder content;
    Boolean inImage;

    public XMLHandler() {
        datalist    = new ArrayList<XMLData>();
        content     = new StringBuilder();
        inImage     = false;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        content = new StringBuilder();

        if(localName.equals("image")) {
            data    = new XMLData();
            inImage = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if(localName.equals("title") && inImage) {
            data.setTitle(content.toString());
        } else if(localName.equals("link") && inImage) {
            data.setLink(content.toString());
            datalist.add(data);
            inImage = false;
        }
    }

    public void characters(char[] buffer, int start, int length) {
        content.append(buffer, start, length);
    }

    public List<XMLData> getData() {
        return datalist;
    }
}
