
package com.example.taskmate;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class TaskContentProvider extends ContentProvider {

    public static final String AUTHORITY =
            "com.example.taskmate.provider";

    public static final String PATH_TASKS = "tasks";

    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + PATH_TASKS);

    private static final int TASKS = 1;
    private static final int TASK_ID = 2;

    private static final UriMatcher URI_MATCHER =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, PATH_TASKS, TASKS);
        URI_MATCHER.addURI(AUTHORITY, PATH_TASKS + "/#", TASK_ID);
    }

    private TaskDatabase taskDatabase;

    @Override
    public boolean onCreate() {
        if (getContext() == null) {
            return false;
        }

        taskDatabase = new TaskDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        SQLiteDatabase db =
                taskDatabase.getReadableDatabase();

        Cursor cursor;

        switch (URI_MATCHER.match(uri)) {
            case TASKS:
                cursor = db.query(
                        "tasks",
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case TASK_ID:
                String id = uri.getLastPathSegment();

                String finalSelection =
                        (selection == null || selection.trim().isEmpty())
                                ? "id = ?"
                                : "id = ? AND (" + selection + ")";

                String[] finalArgs;

                if (selectionArgs == null) {
                    finalArgs = new String[]{id};
                } else {
                    finalArgs = new String[selectionArgs.length + 1];
                    finalArgs[0] = id;
                    System.arraycopy(
                            selectionArgs, 0,
                            finalArgs, 1,
                            selectionArgs.length
                    );
                }

                cursor = db.query(
                        "tasks",
                        projection,
                        finalSelection,
                        finalArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown URI: " + uri
                );
        }

        if (getContext() != null) {
            cursor.setNotificationUri(
                    getContext().getContentResolver(),
                    uri
            );
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case TASKS:
                return "vnd.android.cursor.dir/vnd."
                        + AUTHORITY + ".tasks";

            case TASK_ID:
                return "vnd.android.cursor.item/vnd."
                        + AUTHORITY + ".tasks";

            default:
                throw new IllegalArgumentException(
                        "Unknown URI: " + uri
                );
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (URI_MATCHER.match(uri) != TASKS) {
            throw new IllegalArgumentException(
                    "Invalid URI for insertion: " + uri
            );
        }

        SQLiteDatabase db =
                taskDatabase.getWritableDatabase();

        long id = db.insert("tasks", null, values);

        if (id == -1) {
            return null;
        }

        Uri newUri = Uri.withAppendedPath(
                CONTENT_URI,
                String.valueOf(id)
        );

        notifyChanges(newUri);
        return newUri;
    }

    @Override
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {

        SQLiteDatabase db =
                taskDatabase.getWritableDatabase();

        String finalSelection = selection;
        String[] finalArgs = selectionArgs;

        switch (URI_MATCHER.match(uri)) {
            case TASKS:
                break;

            case TASK_ID:
                String id = uri.getLastPathSegment();

                finalSelection =
                        (selection == null || selection.trim().isEmpty())
                                ? "id = ?"
                                : "id = ? AND (" + selection + ")";

                if (selectionArgs == null) {
                    finalArgs = new String[]{id};
                } else {
                    finalArgs = new String[selectionArgs.length + 1];
                    finalArgs[0] = id;
                    System.arraycopy(
                            selectionArgs, 0,
                            finalArgs, 1,
                            selectionArgs.length
                    );
                }
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown URI: " + uri
                );
        }

        int count = db.update(
                "tasks",
                values,
                finalSelection,
                finalArgs
        );

        if (count > 0) {
            notifyChanges(uri);
        }

        return count;
    }

    @Override
    public int delete(
            Uri uri,
            String selection,
            String[] selectionArgs) {

        SQLiteDatabase db =
                taskDatabase.getWritableDatabase();

        String finalSelection = selection;
        String[] finalArgs = selectionArgs;

        switch (URI_MATCHER.match(uri)) {
            case TASKS:
                break;

            case TASK_ID:
                String id = uri.getLastPathSegment();

                finalSelection =
                        (selection == null || selection.trim().isEmpty())
                                ? "id = ?"
                                : "id = ? AND (" + selection + ")";

                if (selectionArgs == null) {
                    finalArgs = new String[]{id};
                } else {
                    finalArgs = new String[selectionArgs.length + 1];
                    finalArgs[0] = id;
                    System.arraycopy(
                            selectionArgs, 0,
                            finalArgs, 1,
                            selectionArgs.length
                    );
                }
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown URI: " + uri
                );
        }

        int count = db.delete(
                "tasks",
                finalSelection,
                finalArgs
        );

        if (count > 0) {
            notifyChanges(uri);
        }

        return count;
    }

    private void notifyChanges(Uri uri) {
        if (getContext() != null) {
            getContext().getContentResolver()
                    .notifyChange(uri, null);

            if (!CONTENT_URI.equals(uri)) {
                getContext().getContentResolver()
                        .notifyChange(CONTENT_URI, null);
            }
        }
    }
}
