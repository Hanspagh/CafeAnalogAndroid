package dk.cafeanalog;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Communicator {
    public static class AnalogTask extends AsyncTask<Void, Void, Boolean> {
        private final Runnable<Boolean> mPostExecute;
        private final java.lang.Runnable mCancel;
        private final long timeout;

        public AnalogTask(Runnable<Boolean> postExecute, java.lang.Runnable cancel, long timeout) {
            this.mPostExecute = postExecute;
            this.mCancel = cancel;
            this.timeout = timeout;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http", "cafeanalog.dk", "REST");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                String read;
                JSONObject obj;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder builder = new StringBuilder();
                    while ((read = reader.readLine()) != null) {
                        builder.append(read);
                    }
                    obj = new JSONObject(builder.toString());
                    Thread.sleep(timeout, 0);
                    return obj.getBoolean("open");
                }
            } catch (IOException | JSONException | InterruptedException e) {
                e.printStackTrace();
                cancel(true);
                return false;
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mPostExecute.run(aBoolean);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mCancel.run();
        }
    }

    public interface Runnable<T> {
        void run(T param);
    }
}
