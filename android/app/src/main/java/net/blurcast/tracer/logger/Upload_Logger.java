package net.blurcast.tracer.logger;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import net.blurcast.tracer.account.Registration;
import net.blurcast.tracer.app.Geotracer;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.URL;

/**
 * Created by blake on 1/13/15.
 */
public class Upload_Logger extends File_Logger {

    private static final String TAG = Upload_Logger.class.getSimpleName();
    private static final String GLOBAL_SUFFIX = ".uploadable";

    private static final int UPLOAD_TIMEOUT = (int) (20 * DateUtils.SECOND_IN_MILLIS);


    public Upload_Logger(Context context, Bundle args) {
        super(context, args);
    }

    @Override
    protected void createAndOpenFile(String fileName) {
        super.createAndOpenFile(fileName + GLOBAL_SUFFIX);
    }

    @Override
    public void finish(Bundle data) {

        String url = mArgs.getString(Geotracer.ARG_LOGGER_UPLOAD_URL)+"?device="+ Registration.getUniqueId(mContext)+"&file="+mLogFile.getName();

        Log.i(TAG, "Uploading file to "+url+"...");

        HttpPost httpPost = new HttpPost(url);
        MultipartEntity entity = new MultipartEntity();
        entity.addPart("data", new FileBody(mLogFile));
        httpPost.setEntity(entity);

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, UPLOAD_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, UPLOAD_TIMEOUT);

        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);

            // 200
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String response = responseHandler.handleResponse(httpResponse);

                if(response != null && response.equals(mLogFile.length()+"")) {

                    // let user know this was uploaded
                    Toast.makeText(mContext, "Uploaded to "+url.replaceFirst("^(?:[^/]+//)([^/]+).*$", "$1"), Toast.LENGTH_LONG).show();

                    // log to console
                    Log.i(TAG, "Upload Succeeded!");

                    // attempt to delete it
                    if(!mLogFile.delete()) {
                        Log.w(TAG, "Failed to delete uploaded file from device");
                    }
                }
                else {
                    // let user know upload was rejected
                    Toast.makeText(mContext, "Upload was rejected", Toast.LENGTH_LONG).show();

                    // log to console
                    Log.e(TAG, "Upload rejected, response said: " + response);
                }
            }
            else {
                // server-side error
                Toast.makeText(mContext, "Server-side error", Toast.LENGTH_LONG).show();

                // log to console
                StatusLine statusLine = httpResponse.getStatusLine();
                Log.e(TAG, "Server-side error: "+statusLine.getStatusCode()+"/"+statusLine.getReasonPhrase());
            }
        } catch(ClientProtocolException e) {
            e.printStackTrace();
            Log.e(TAG, "Client Protocol Exception");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IO Exception");
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }


//            int statusCode = response.getStatusLine().getStatusCode();
//            if(statusCode < 200 || statusCode >= 300) {
//                Log.e(TAG, "failed to upload file");
//            }
//            else {
//                mLogFile.delete();
//                Log.w(TAG, "Success: "+response.getEntity().getContent().);
//            }

//        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//        builder.addPart("data", new FileBody(mLogFile));

//        httpPost.setEntity(builder.build());

//        FileEntity fileEntity = new FileEntity(mLogFile, "binary/octet-stream");
//        httpPost.setEntity(fileEntity);

//        httpPut.getParams().setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

//        FileEntity entity = new FileEntity(mLogFile, "binary/octet-stream");


}
