import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EmailSender {
    private String hostname;
    private int port;
    private int maxSizePerPart = 4096;
    private String usernameBase64;
    private String passBase64;
    private String domainName;


    /**
     * @param args
     * @param hostname
     * @param port
     */
    public EmailSender(String[] args, String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.usernameBase64 = args[0];
        this.passBase64 = args[1];
        this.domainName = "a.com";
    }

    /**
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public void startSendingEmailProcess() throws IOException, NoSuchAlgorithmException, KeyManagementException {

        Socket insecureEmailSendingSocket = createSocket(hostname, port);
        passCommandsList(insecureEmailSendingSocket, this.maxSizePerPart);
    }

    /**
     * @param hostname
     * @param port
     * @return
     * @throws IOException
     */
    public Socket createSocket(String hostname, int port) throws IOException {
        return new Socket(hostname, port);
    }

    /**
     * @param inSecureSocket
     * @param maxSizePerPart
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public void passCommandsList(Socket inSecureSocket, int maxSizePerPart) throws IOException, NoSuchAlgorithmException, KeyManagementException {

        String senderEmail = "<garom@kth.se>";
        String destinationEmail = "<bnyo@kth.se>";
        String emailContent = "Hello Mr S!MoN.\r\n.\r\n";

        String[] commandsList = new String[]{"",
                                            "EHLO " + this.domainName + "\r\n",
                                            "STARTTLS\r\n",
                                            "AUTH LOGIN\r\n",
                                            Base64.getEncoder().encodeToString(this.usernameBase64.getBytes()) + "\r\n",
                                            Base64.getEncoder().encodeToString(this.passBase64.getBytes()) + "\r\n",
                                            "MAIL FROM:" + senderEmail + "\r\n",
                                            "RCPT TO:" + destinationEmail + "\r\n",
                                            "DATA\r\n",
                                            emailContent,
                                            "QUIT\r\n"
        };

        String[] expectedReplies = new String[] {"- Spammers be gone!\r\n",
                                                 "250 DSN\r\n",
                                                 "220 2.0.0 Ready to start TLS\r\n",
                                                 "VXNlcm5hbWU6\r\n",
                                                 "UGFzc3dvcmQ6\r\n",
                                                 "Authentication successful\r\n",
                                                 "Ok",
                                                 "Ok",
                                                 "End data with <CR><LF>.<CR><LF>\r\n",
                                                 "Ok: queued as",
                                                 "Bye\r\n"
        };


        for (int i = 0; i < 3; i++) {
            System.out.println(insecureRequestAndReadServerResponse(inSecureSocket, commandsList[i], expectedReplies[i], maxSizePerPart));
        }

        SSLSocket secureSocket = createSecureSocket(inSecureSocket);

        System.out.println(secureRequestAndReadServerResponse(secureSocket, commandsList[1], expectedReplies[1], maxSizePerPart));

        for (int i = 3; i <= 10; i++) {
            System.out.println(secureRequestAndReadServerResponse(secureSocket, commandsList[i], expectedReplies[i], maxSizePerPart));
        }

    }

    /**
     * @param inSecureSocket
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws KeyManagementException
     */
    public static SSLSocket createSecureSocket(Socket inSecureSocket) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, null);
        SSLSocketFactory factory = sslContext.getSocketFactory();
        SSLSocket secureSocket = (SSLSocket) factory.createSocket(inSecureSocket, null, inSecureSocket.getPort(), false);
        secureSocket.startHandshake();
        return secureSocket;
    }


    /**
     * @param inSecureSocket
     * @param runnableCommand
     * @param expectedConfirmResponse
     * @param maxSizePerPart
     * @return
     * @throws IOException
     */
    public static String insecureRequestAndReadServerResponse(Socket inSecureSocket, String runnableCommand, String expectedConfirmResponse, int maxSizePerPart) throws IOException {

        OutputStream serverDataStream = inSecureSocket.getOutputStream();
        Main.sendDataToServer(runnableCommand, serverDataStream);
        return readDataFromServerToUser(inSecureSocket, maxSizePerPart, expectedConfirmResponse);
    }

    /**
     * @param secureSocket
     * @param runnableCommand
     * @param expectedConfirmResponse
     * @param maxSizePerPart
     * @return
     * @throws IOException
     */
    public static String secureRequestAndReadServerResponse(SSLSocket secureSocket, String runnableCommand, String expectedConfirmResponse, int maxSizePerPart) throws IOException {

        OutputStream serverDataStream = secureSocket.getOutputStream();
        Main.sendDataToServer(runnableCommand, serverDataStream);
        return readDataFromServerToUser(secureSocket, maxSizePerPart, expectedConfirmResponse);
    }

    /**
     * @param inSecureSocket
     * @param maxSizePerPart
     * @param confimrationResponse
     * @return
     * @throws IOException
     */
    private static String readDataFromServerToUser(Socket inSecureSocket, int maxSizePerPart, String confimrationResponse) throws IOException {

        ByteArrayOutputStream userDataHolder = new ByteArrayOutputStream();
        InputStream dataFromUser = inSecureSocket.getInputStream();
        boolean requestEnded = false;
        while (!requestEnded) {
            byte[] buffer = new byte[maxSizePerPart];
            int length = dataFromUser.read(buffer);
            if (length != -1) {
                userDataHolder.write(buffer, 0, length);
                requestEnded = Main.checkRequestEnd(length, buffer, confimrationResponse); // to prevent checking/ calling the check function for every byte,
                // it can switched switched to use counter and call the checkRequestEnd function every four byte (because we will check 4 bytes each time).
            }
            else {
                break;
            }
        }

        return userDataHolder.toString(StandardCharsets.UTF_8);
    }

}
