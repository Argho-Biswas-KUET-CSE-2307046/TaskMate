
package com.example.taskmate;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;

public class XmlParser {

    public static String parseTasks(String xmlData) {

        StringBuilder result = new StringBuilder();

        try {

            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(xmlData));

            int eventType = parser.getEventType();

            String currentTag = "";
            String id = "";
            String title = "";
            String priority = "";
            String completed = "";

            int taskNumber = 0;

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {

                    currentTag = parser.getName();

                    if (currentTag.equals("task")) {

                        id = "";
                        title = "";
                        priority = "";
                        completed = "";
                    }

                } else if (eventType == XmlPullParser.TEXT) {

                    String value = parser.getText().trim();

                    switch (currentTag) {

                        case "id":
                            id = value;
                            break;

                        case "title":
                            title = value;
                            break;

                        case "priority":
                            priority = value;
                            break;

                        case "completed":
                            completed = value;
                            break;
                    }

                } else if (eventType == XmlPullParser.END_TAG) {

                    String tag = parser.getName();

                    if (tag.equals("task")) {

                        taskNumber++;

                        result.append("Task ")
                                .append(taskNumber)
                                .append("\n");

                        result.append("ID: ")
                                .append(id)
                                .append("\n");

                        result.append("Title: ")
                                .append(title)
                                .append("\n");

                        result.append("Priority: ")
                                .append(priority)
                                .append("\n");

                        result.append("Completed: ")
                                .append(completed)
                                .append("\n\n");
                    }

                    currentTag = "";
                }

                eventType = parser.next();
            }

        } catch (Exception e) {

            return "XML Parsing Error: " + e.getMessage();
        }

        return result.toString();
    }
}
