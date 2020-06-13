package es.upv.etsit.aatt.trabajo.fit_map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.accessibility.AccessibilityViewCommand;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.widget.TextView;

import org.json.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    boolean primera_vez = true;
    TextView municipio;
    TextView temperatura;
    TextView probPrec;
    TextView dir_viento;
    TextView vel_viento;
    TextView estadoCielo;


    String TAG="tag";
    //String url =  "https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/diaria/23039?api_key=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkYW5pc2VyZmF0eUBnbWFpbC5jb20iLCJqdGkiOiIyZDJlNzdkMC1iMGZiLTRjMjUtYjE0Ni0zNzM4Njk5NzA4MDciLCJpc3MiOiJBRU1FVCIsImlhdCI6MTU5MTQ5MTk0NiwidXNlcklkIjoiMmQyZTc3ZDAtYjBmYi00YzI1LWIxNDYtMzczODY5OTcwODA3Iiwicm9sZSI6IiJ9.qijNbtjUiNLH_Ffk2tv8fH2ES6h2_l0eLXRkHVnoYXc";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        municipio=(TextView) findViewById(R.id.municipio);
        temperatura = (TextView) findViewById(R.id.temperatura12_18);
        probPrec=(TextView) findViewById(R.id.probPrecipit);
        // Creación de tarea asíncrona
        // Ejecución de hilo de tarea asíncrona
        String url =  "https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/diaria/23039?api_key=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkYW5pc2VyZmF0eUBnbWFpbC5jb20iLCJqdGkiOiIyZDJlNzdkMC1iMGZiLTRjMjUtYjE0Ni0zNzM4Njk5NzA4MDciLCJpc3MiOiJBRU1FVCIsImlhdCI6MTU5MTQ5MTk0NiwidXNlcklkIjoiMmQyZTc3ZDAtYjBmYi00YzI1LWIxNDYtMzczODY5OTcwODA3Iiwicm9sZSI6IiJ9.qijNbtjUiNLH_Ffk2tv8fH2ES6h2_l0eLXRkHVnoYXc";
        TareaAsincrona tareaAsincrona = new TareaAsincrona();
        tareaAsincrona.execute(url);

    }



    class TareaAsincrona extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... uri) {
            String response = null;
            for (String p : uri) {
                if (p == null) {publishProgress("API_REST ha sufrido problemas."); break;}
                else {
                    response = API_REST(p);
                    publishProgress("API_REST ha funcionado correctamente");
                }
            }
            return response;
        }

        @Override
        protected void onPreExecute(){}

        @Override
        protected void onProgressUpdate(String... k){
            for (String p : k)
                Log.d("charged", p);
        }

        @Override
        protected void onPostExecute(String respuesta) {

            if (respuesta!=null) {
                try {
                    JSONObject raiz_1 = new JSONObject(respuesta);
                    String link_2 = raiz_1.getString("datos");
                    if (primera_vez) {
                        primera_vez = false;

                        // Obtención de la propiedad "datos" del JSON


                        // Creación de una nuevo objeto de TareaAsincrona
                        // Ejecución del hilo correspondiente
                        TareaAsincrona tareaAsincrona2= new TareaAsincrona();
                        tareaAsincrona2.execute(link_2);

                    } else { // segunda vez: recogida de respuesta de la segunda llamada
                        JSONArray raiz_2 = new JSONArray(respuesta);

                        // Obtencion de las propiedades oportunas del JSON recibido
                        // Aquí ya se puede acceder a la UI, ya que estamos en el hilo
                        // convencional de ejecución, y por tanto ya se puede modificar
                        // el contenido de los TextView que contienen los valores de los datos.

                        String nombre = raiz_2.getJSONObject(0).getString("nombre");
                        double temperatura_2=raiz_2.getJSONObject(0).getJSONObject("prediccion").getJSONArray("dia")
                                .getJSONObject(1).getJSONObject("temperatura").getJSONArray("dato").getJSONObject(2).getInt("value");

                        municipio.setText(nombre);
                        temperatura.setText(String.valueOf(temperatura_2));

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Problemas decodificando JSON");
                }
            }

        } // onPostExecute

    } // TareaAsincrona






    /** La peticion del argumento es recogida y devuelta por el método API_REST.
     Si hay algun problema se retorna null */
    public String API_REST(String uri){

        StringBuffer response = null;

        try {
            URL url = new URL(uri); //"URL" anyadido
            Log.d(TAG, "URL: " + uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Detalles de HTTP
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Codigo de respuesta: " + responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String output;
                response = new StringBuffer();

                while ((output = in.readLine()) != null) {
                    response.append(output);
                }
                in.close();
            } else {
                Log.d(TAG, "responseCode: " + responseCode);
                return null; // retorna null anticipadamente si hay algun problema
            }
        } catch(Exception e) { // Posibles excepciones: MalformedURLException, IOException y ProtocolException
            e.printStackTrace();
            Log.d(TAG, "Error conexión HTTP:" + e.toString());
            return null; // retorna null anticipadamente si hay algun problema
        }

        return new String(response); // de StringBuffer -response- pasamos a String

    } // API_REST

    public void contador_pasos {
        //Activity recognition
        android.permission.ACTIVITY_RECOGNITION;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);



    }




}