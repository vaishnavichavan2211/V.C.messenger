package com.example.vcmessenger;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class StorageHelper {

    public static final int PROFILE = 0;
    static String accessToken = "sl.CEafOV35TOrkDm_EAYfGCsJVjmo0l1iStDyo-rr5WfJFDtLRXFrobt7_YbGazIbge4anhs7m8l6mVGrb3GxlAqm1xK7zw3jryEa_czZoj9ZJPVdfCGVSK7uWjh7-2wSuyhXoDzY4FRQ-gQbhpOuWfR8";
    static String refreshToken = "Sc5d_TBBlTkAAAAAAAAAAeqVwtVtH8s9afFtOjCu6dMc1o4Oj3ngUHpSym3w4o0f";
    static String clientId = "c12lhsszlw3ljom"; // Your App Key
    static String clientSecret = "mvgvkg82iwguk1v";
    static DbxRequestConfig config;
    static DbxClientV2 client;

    public static void uploadImage(Context context,int TYPE, Uri imageURI, String id, ImageUploadListener imageUploadListener) {
        String path;
        if (TYPE == PROFILE) {
            path = "/profile/" + id + ".jpg";
        } else {
            path = "";
        }
        File file = new File(imageURI.getPath());

        new Thread(() -> {
            if(client == null || config == null) {
                try {
                    initialiseDropboxClient();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(isAccessTokenExpired()){
                try {
                    initialiseDropboxClient();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                InputStream in = new FileInputStream(file);
                FileMetadata metadata = client.files().uploadBuilder(path).uploadAndFinish(in);
                imageUploadListener.onUploadSuccess(metadata.getPathDisplay());
            } catch (Exception e) {
                e.printStackTrace();
                imageUploadListener.onUploadError(e.getMessage());
            }
        }).start();
    }

    public static void refreshAccessToken() throws IOException {
        HttpURLConnection con = getHttpURLConnection();

        int responseCode = con.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        // Read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Output the response (contains the new access token)
        System.out.println("Response: " + response);

        accessToken = response.toString().split(",")[0].split(":")[1].replace("\"", "");
    }

    @NonNull
    private static HttpURLConnection getHttpURLConnection() throws IOException {
        String params = "grant_type=refresh_token" +
                "&refresh_token=" + refreshToken +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret;

        String url = "https://api.dropboxapi.com/oauth2/token";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set request method to POST
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        // Send the request data
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.writeBytes(params);
            wr.flush();
        }
        return con;
    }

    public static void listAllFiles(TaskListener taskListener) {
        new Thread(() -> {
            if(client == null || config == null) {
                try {
                    initialiseDropboxClient();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(isAccessTokenExpired()){
                try {
                    initialiseDropboxClient();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                ListFolderResult result = client.files().listFolder("");
                taskListener.onSuccess(result);
            } catch (DbxException e) {
                e.printStackTrace();
                taskListener.onError(e.getMessage());
            }
        }).start();
    }

    static void initialiseDropboxClient() throws IOException {
        config = DbxRequestConfig.newBuilder("vaishnavi ka app").build();
        client = new DbxClientV2(config, accessToken);

        if(isAccessTokenExpired()){
            Log.d("Dropbox", "Token expired. Refreshing token...");
            refreshAccessToken();
            initialiseDropboxClient();
        }else {
            Log.d("Dropbox", "Token is valid");
        }
    }

    private static boolean isAccessTokenExpired() {
        try {
            FullAccount account = client.users().getCurrentAccount();
            return false; // If the call succeeds, the token is valid
        } catch (DbxException e) {
            return e.getMessage().contains("expired");
        }

    }

    public static void downloadFile(String dropboxPath, File localFile, TaskListener taskListener) {
        new Thread(()->{
            if(localFile.exists()) {
                taskListener.onSuccess(localFile);
                return;
            }
            if(client == null || config == null) {
                try {
                    initialiseDropboxClient();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(isAccessTokenExpired()){
                try {
                    initialiseDropboxClient();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try (OutputStream out = new FileOutputStream(localFile)) {
                FileMetadata metadata = client.files().downloadBuilder(dropboxPath)
                        .download(out);
                taskListener.onSuccess(localFile);
            } catch (Exception e) {
                e.printStackTrace();
                taskListener.onError(e.getMessage());
            }
        }).start();
    }


    public interface ImageUploadListener {
        void onUploadSuccess(String imageUrl);
        void onUploadError(String message);
    }

    public interface TaskListener {
        void onSuccess(Object object);
        void onError(String message);
    }
}
