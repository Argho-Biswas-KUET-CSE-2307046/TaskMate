
package com.example.taskmate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {

    public static String parseTasks(String jsonData) {

        StringBuilder result = new StringBuilder();

        try {
            JSONArray tasks = new JSONArray(jsonData);

            for (int i = 0; i < tasks.length(); i++) {

                JSONObject task = tasks.getJSONObject(i);

                int id = task.optInt("id");
                String title = task.optString("title");
                String priority = task.optString("priority");
                String completed = task.optString("completed");

                result.append("Task ").append(i + 1).append("\n");
                result.append("ID: ").append(id).append("\n");
                result.append("Title: ").append(title).append("\n");
                result.append("Priority: ").append(priority).append("\n");
                result.append("Completed: ").append(completed).append("\n\n");
            }

        } catch (JSONException e) {

            return "JSON Parsing Error: " + e.getMessage();
        }

        return result.toString();
    }
}
