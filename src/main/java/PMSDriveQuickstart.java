import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class PMSDriveQuickstart {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();
		
		// create a folder	
		
		File fileMetadata = new File();
		fileMetadata.setName("pms");
		fileMetadata.setMimeType("application/vnd.google-apps.folder");

		File createFolder = service.files().create(fileMetadata).setFields("id").execute();
		System.out.println(createFolder.getId());

		// create a file inside folder 

		String folderId = "folderId";		
		fileMetadata.setName("pms.jpg");
		fileMetadata.setParents(Collections.singletonList(folderId));
		java.io.File filePath = new java.io.File("pms/pms.jpg");
		FileContent mediaContent = new FileContent("image/jpeg", filePath);
		File CreateFileInsideFolder = service.files().create(fileMetadata, mediaContent).setFields("id, parents").execute();
		

		// Copy a file from folder to another folder

		File pmsfile = new File();
		copiedFile.setTitle("pms.txt");

		File pmsnew = new File();
		pmsnew.setName("pmsnew");
		pmsnew.setMimeType("application/vnd.google-apps.folder");
		service.files().create(pmsnew).setFields("id").execute();
		
		try {
		  service.files().copy("pms", "pmsnew").execute();
		} catch (IOException e) {
		  System.out.println("An error occurred: " + e);
		}


		// Delete a file inside folder

		String FileName = "pms.jpg";		
		File FileDelete = service.files().delete(FileName).execute();
		System.out.println(FileDelete.getId());
		
		
		// Delete a folder
		
		FileList result = service.files().list().setPageSize(999).setFields("nextPageToken, files(id, name)").execute();
        
		List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        }else {
            for (File file : files) {
				service.files().delete(file.getId());
            }
			
			String folderId = createFolder.getId();
			if (files == null || files.isEmpty()) {
				service.files().delete(folderId);
			}
        }
    }
}