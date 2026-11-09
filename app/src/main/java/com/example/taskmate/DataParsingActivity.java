
package com.example.taskmate;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DataParsingActivity extends AppCompatActivity {

    private Button btnParseJson;
    private Button btnParseXml;
    private TextView tvParsingResult;

    // Sample JSON data.
    private final String jsonData =
            "[{" +
                    "\"id\":1," +
                    "\"title\":\"Study Java\"," +
                    "\"priority\":\"High\"," +
                    "\"completed\":false" +
                    "},{" +
                    "\"id\":2," +
                    "\"title\":\"Complete Assignment\"," +
                    "\"priority\":\"Medium\"," +
                    "\"completed\":true" +
                    "}]";

    // Sample XML data.
    private final String xmlData =
            "<tasks>" +
                    "<task>" +
                    "<id>1</id>" +
                    "<title>Study Java</title>" +
                    "<priority>High</priority>" +
                    "<completed>false</completed>" +
                    "</task>" +
                    "<task>" +
                    "<id>2</id>" +
                    "<title>Complete Assignment</title>" +
                    "<priority>Medium</priority>" +
                    "<completed>true</completed>" +
                    "</task>" +
                    "</tasks>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_data_parsing);

        btnParseJson = findViewById(R.id.btnParseJson);
        btnParseXml = findViewById(R.id.btnParseXml);
        tvParsingResult = findViewById(R.id.tvParsingResult);

        btnParseJson.setOnClickListener(v -> {

            String result = JsonParser.parseTasks(jsonData);

            tvParsingResult.setText(result);
        });

        btnParseXml.setOnClickListener(v -> {

            String result = XmlParser.parseTasks(xmlData);

            tvParsingResult.setText(result);
        });
    }
}
