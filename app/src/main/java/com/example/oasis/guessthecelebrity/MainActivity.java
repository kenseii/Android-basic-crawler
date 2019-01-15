package com.example.oasis.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebUrls = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    // holds index of the randomly selected celeb
    int chosenCeleb = 0;
    // array to hold the answers to be displayed
    String[] answers = new String[4];
    // variable to hold the index of the correct answer
    int correctAnswer = 0;

    ImageView imageView;

    // Answer buttons
    Button button0;
    Button button1;
    Button button2;
    Button button3;

    public void celebChosen(View view) {
        if (view.getTag().toString().equals(Integer.toString(correctAnswer))) {
            Toast.makeText(getApplicationContext(), "Correct", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(), "Not correct, it was " + answers[correctAnswer], Toast.LENGTH_SHORT).show();
        }
        // whether or not the user got the correct answer, bring a new question
        newQuestion();
    }

    public void newQuestion() {
        try {


            // Randomizer
            Random rand = new Random();
            chosenCeleb = rand.nextInt(celebNames.size());
            // after getting the randomly chosen celeb, download the image of that celeb
            ImageDownloader imageTask = new ImageDownloader();
            Bitmap celebImage = imageTask.execute(celebUrls.get(chosenCeleb)).get();
            // put the image on screen
            imageView.setImageBitmap(celebImage);
            // randomly select the correct answers location

            correctAnswer = rand.nextInt(4);
            int incorrectAnswerLocation;

            // populate the answers array
            // if the index is the same as the correct answer's
            // set the value of that location to the the chosen celeb's name
            // else set the values of that location to the randomly fetched celeb
            for (int i = 0; i < 4; i++) {
                if (i == correctAnswer) {
                    answers[i] = celebNames.get(chosenCeleb);

                } else {
                    // randomly
                    incorrectAnswerLocation = rand.nextInt(celebUrls.size());

                    // ensure the data doesnt pick the correct answer in the fake ones
                    while (incorrectAnswerLocation == chosenCeleb) {
                        incorrectAnswerLocation = rand.nextInt(celebUrls.size());
                    }
                    // finally add the fake to the array
                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }

            // set the buttons value to the held answers array
            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);

        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);

        // task caller
        DownloadTask task = new DownloadTask();
        String result = null;

        try {
            result = task.execute("http://www.posh24.se/kandisar").get();
            // get data in a certain div
            String[] splitResult = result.split("<div class=\"listedArticles\">");

            //parse out the img path
            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);
            while (m.find()) {
                // append the returned links to the urls array
                celebUrls.add(m.group(1));
            }
            // parse out artist name
            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);
            while (m.find()) {
                // append the returned names to the names array
                celebNames.add(m.group(1));
            }
            // get a question
            newQuestion();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            //getting the url passed in

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();

                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;

            }


        }
    }

    // Image downloader class, it uses the urls given from the array then get the images
    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
