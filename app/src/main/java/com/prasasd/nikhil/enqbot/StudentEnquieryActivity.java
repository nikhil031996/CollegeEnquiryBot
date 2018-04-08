package com.prasasd.nikhil.enqbot;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.felipecsl.gifimageview.library.GifImageView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class StudentEnquieryActivity extends AppCompatActivity implements AIListener {

    private static final String TAG = "tag";
    AIService aiService;
    AIDataService aiDataService;
    TextView message;
    TextView input, answerFromDatabase;
    TextView questionText;
    String subject, section, place, day, question;
    static int time;
    GifImageView gifrobot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_enquiery);

        gifrobot = (GifImageView) findViewById(R.id.robot);
        new RetArray().execute("https://cdn.dribbble.com/users/37530/screenshots/2937858/drib_blink_bot.gif");
        gifrobot.startAnimation();




        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");
            makeRequest();
        }
        final AIConfiguration config = new AIConfiguration("0f8d000eb5a446c7a5d59127df28014c",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);
        aiDataService = new AIDataService(this, config);
        message = (TextView) findViewById(R.id.answer_df);
        input = (TextView) findViewById(R.id.question_show);
        questionText = (TextView) findViewById(R.id.m_question);
        answerFromDatabase = (TextView) findViewById(R.id.answer_db);

    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {


                } else {

                }
                return;
            }
        }
    }

    public void buttonClicked(View view) {

        aiService.startListening();


    }


    public void textquestion(View view) {
        input.setText(questionText.getText().toString());


    }

    @Override
    public void onResult(AIResponse result) {
        Result result1 = result.getResult();
        input.setText(result1.getResolvedQuery());
        message.setText(result1.getFulfillment().getSpeech());
        day = result1.getStringParameter("Day");
        section = result1.getStringParameter("Section");
        place = result1.getStringParameter("class_number");
        subject = result1.getStringParameter("Subject");
        question = result1.getAction().toString();
        Toast.makeText(this, question, Toast.LENGTH_SHORT).show();

        if (result1.getStringParameter("Day").equals("today")) {

            Calendar calendar = Calendar.getInstance();
            int CurDay = calendar.get(Calendar.DAY_OF_WEEK);
            switch (CurDay) {
                case Calendar.SUNDAY:
                    day = "Sun";
                    break;

                case Calendar.MONDAY:
                    // Current day is Monday
                    day = "Mon";
                    break;

                case Calendar.TUESDAY:
                    day = "Tues";
                    break;
                case Calendar.WEDNESDAY:
                    day = "Wed";
                    break;
                case Calendar.THURSDAY:
                    day = "Thur";
                    break;
                case Calendar.FRIDAY:
                    day = "Fri";
                    break;
                case Calendar.SATURDAY:
                    day = "Sat";
                    break;


            }
        }


        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (subject.equals("next")) {
            if (hour > 18) {
                message.setText("You have No more class today");
            } else if (hour < 10) {
                time = 10;
            }
        }
        time = hour;

        Toast.makeText(this, "day=" + day + "time=" + time, Toast.LENGTH_SHORT).show();
        new GetData().execute();
    }


    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }






    class GetData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            StringBuffer sb = new StringBuffer("");
            String link;
            try {

                link = "http://10.0.165.165/checkConnection.php?subject=" + subject.toUpperCase() + "&time=" + time + "&day=" + day;

                URL url = new URL(link);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(link));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
            } catch (Exception e) {
                sb.append("Error:" + e);
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            answerFromDatabase.setText(s);
        }
    }


    class RetArray extends AsyncTask<String, Void, byte[]> {


        @Override
        protected byte[] doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[10240];
                    while ((nRead = in.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    return buffer.toByteArray();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            gifrobot.setBytes(bytes);

        }
    }
}





