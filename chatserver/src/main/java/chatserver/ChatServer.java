package chatserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpContext;

public class ChatServer 
{
    public static void main( String[] args )
    {
        try{
            log("Starting server....");

            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
            SSLContext sslContext = chatServerSSLContext();
            server.setHttpsConfigurator (new HttpsConfigurator(sslContext) { 
                public void configure (HttpsParameters params) { 
                 InetSocketAddress remote = params.getClientAddress(); 
                 SSLContext c = getSSLContext(); 
                 SSLParameters sslparams = c.getDefaultSSLParameters(); 
                 params.setSSLParameters(sslparams); 
                } 
               });
            ChatAuthenticator auth = new ChatAuthenticator();

            HttpContext chatContext = server.createContext("/chat", new ChatHandler());
            chatContext.setAuthenticator(auth);
            server.createContext("/registration", new RegistrationHandler(auth));
            server.setExecutor(null);
            log("Chatserver is now ON!");
            server.start();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static final String ANSI_GREEN = "\u001b[32m";

    public static void log(String message){
        System.out.println(ANSI_GREEN + LocalDateTime.now() + " " + message);
    }

    private static SSLContext chatServerSSLContext() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException{
        char[] passphrase = "paskaperse".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("keystore.jks"), passphrase);
     
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
     
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
     
        SSLContext ssl = SSLContext.getInstance("TLS"); 
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ssl; 
    }
}