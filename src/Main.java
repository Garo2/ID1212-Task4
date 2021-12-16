import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {


    //Before you start running the program, edit configuration by go to: -> Run -> Edit Configurations -> Program arguments.
    // Enter first username without hostname (garom) and then password of the email
    //<username> <password>
    public static void main(String[] args) throws IOException {
        int maxSizePerPart = 4096;
        receiveEmails(args, maxSizePerPart);
    }

    public static String buildLoginCommand(String[] args) {
        String userName = args[0];
        String pass = args[1];
        return "A001 LOGIN " + userName + " " + pass + "\r\n";
    }

    public static String buildListInboxCommand() {
        return "A001 list \"INBOX/\" \"*\"\r\n";
    }

    public static String buildSelectInboxCommand() {
        return "A001 SELECT \"INBOX\"\r\n";
    }

    public static String buildFetchFirstMsgCommand() {
        return "A001 FETCH 1 BODY[TEXT]\r\n";
    }

    public static String buildFetchFirstTenMsgCommand() {
        return "A001 FETCH 1:10 (BODY[HEADER.FIELDS (Subject)])\r\n";
    }

    public static String buildLogOutCommand() {
        return "A001 LOGOUT\r\n";
    }

    public static String receiveConnectioncConfirmation(SSLSocket secureSocket, int maxSizePerPart) throws IOException {
        OutputStream serverDataStream = secureSocket.getOutputStream();
        sendDataToServer("", serverDataStream);
        String expectedConfirmResponse = "* OK The Microsoft Exchange IMAP4 service is ready.\r\n";
        String loginConf = readDataFromServerToUser(secureSocket, maxSizePerPart, expectedConfirmResponse);
        return loginConf;
    }

    public static String receiveLoginConfirmation(String[] args, SSLSocket secureSocket, int maxSizePerPart) throws IOException {
        String loginCommand = buildLoginCommand(args);
        OutputStream serverDataStream = secureSocket.getOutputStream();
        sendDataToServer(loginCommand, serverDataStream);
        String expectedConfirmResponse = "A001 OK LOGIN completed.\r\n";
        String loginConf = readDataFromServerToUser(secureSocket, maxSizePerPart, expectedConfirmResponse);
        return loginConf;
    }

    public static String reciveListingInboxContent(SSLSocket secureSocket, int maxSizePerPart) throws IOException {
        String listInboxCommand = buildListInboxCommand();
        OutputStream serverDataStream = secureSocket.getOutputStream();
        sendDataToServer(listInboxCommand, serverDataStream);
        String expectedConfirmResponse = "A001 OK LIST completed.\r\n";
        String getInboxConf = readDataFromServerToUser(secureSocket, maxSizePerPart, expectedConfirmResponse);
        return getInboxConf;
    }

    public static String selectInboxContent(SSLSocket secureSocket, int maxSizePerPart) throws IOException {
        String sltInboxCommand = buildSelectInboxCommand();
        OutputStream serverDataStream = secureSocket.getOutputStream();
        sendDataToServer(sltInboxCommand, serverDataStream);
        String expectedConfirmResponse = "A001 OK [READ-WRITE] SELECT completed.\r\n";
        String getSelectInboxConf = readDataFromServerToUser(secureSocket, maxSizePerPart, expectedConfirmResponse);
        return getSelectInboxConf;
    }

    public static String fetchFirstMsgContent(SSLSocket secureSocket, int maxSizePerPart) throws IOException {
        String firstMsgCommand = buildFetchFirstMsgCommand();
        OutputStream serverDataStream = secureSocket.getOutputStream();
        sendDataToServer(firstMsgCommand, serverDataStream);
        String expectedConfirmResponse = "A001 OK FETCH completed.\r\n";
        String getFetchFirstMsgConf = readDataFromServerToUser(secureSocket, maxSizePerPart, expectedConfirmResponse);
        return getFetchFirstMsgConf;
    }

    public static String fetchFirstTenMsgContent(SSLSocket secureSocket, int maxSizePerPart) throws IOException {
        String firstTenMsgCommand = buildFetchFirstTenMsgCommand();
        OutputStream serverDataStream = secureSocket.getOutputStream();
        sendDataToServer(firstTenMsgCommand, serverDataStream);
        String expectedConfirmResponse = "A001 OK FETCH completed.\r\n";
        String getFirstTenMsgConf = readDataFromServerToUser(secureSocket, maxSizePerPart, expectedConfirmResponse);
        return getFirstTenMsgConf;
    }

    public static String logoutTheUserContent(SSLSocket secureSocket, int maxSizePerPart) throws IOException {
        String logoutCommand = buildLogOutCommand();
        OutputStream serverDataStream = secureSocket.getOutputStream();
        sendDataToServer(logoutCommand, serverDataStream);
        String expectedConfirmResponse = "* BYE Microsoft Exchange Server 2016 IMAP4 server signing off.\r\n";
        String getLogoutConf = readDataFromServerToUser(secureSocket, maxSizePerPart, expectedConfirmResponse);
        return getLogoutConf;
    }




    private static void sendDataToServer(String dataToClient, OutputStream serverDataStream) throws IOException {
        serverDataStream.write(dataToClient.getBytes(StandardCharsets.UTF_8));
    }

    public static void receiveEmails(String[] args, int maxSizePerPart) throws IOException {
        try {
            String host = "webmail.kth.se";
            int port = 993;

            SSLSocketFactory sslSocketFactory = createSSLSocketFactory();
            SSLSocket secureSocket = createSecureSocket(sslSocketFactory, host, port);

            String connectionConfirm = receiveConnectioncConfirmation(secureSocket, maxSizePerPart);
            System.out.println(connectionConfirm);

            String loginConf = receiveLoginConfirmation(args, secureSocket, maxSizePerPart);
            System.out.println(loginConf);

            String listInboxContent = reciveListingInboxContent(secureSocket, maxSizePerPart);
            System.out.println(listInboxContent);

            String sltInboxContent = selectInboxContent(secureSocket,maxSizePerPart);
            System.out.println(sltInboxContent);

            String firstMsgContent = fetchFirstMsgContent(secureSocket,maxSizePerPart);
            System.out.println(firstMsgContent);

            String firstTenMsgContent = fetchFirstTenMsgContent(secureSocket,maxSizePerPart);
            System.out.println(firstTenMsgContent);

            String logoutContent = logoutTheUserContent(secureSocket,maxSizePerPart);
            System.out.println(logoutContent);

        }
        catch (IOException ioEx) {
            System.out.println("creating a SSLSocket failed");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SSLSocketFactory createSSLSocketFactory() throws IOException {
        return (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    private static SSLSocket createSecureSocket(SSLSocketFactory sslSocketFactory, String host, int port) throws IOException {
        return (SSLSocket) sslSocketFactory.createSocket(host, port);
    }


    private static String readDataFromServerToUser(SSLSocket secureSocket, int maxSizePerPart, String confimrationResponse) throws IOException {

        ByteArrayOutputStream userDataHolder = new ByteArrayOutputStream();
        InputStream dataFromUser = secureSocket.getInputStream();
        boolean requestEnded = false;
        while (!requestEnded) {
            byte[] buffer = new byte[maxSizePerPart];
            int length = dataFromUser.read(buffer);
            if (length != -1) {
                userDataHolder.write(buffer, 0, length);
                requestEnded = checkRequestEnd(length, buffer, confimrationResponse); // to prevent checking/ calling the check function for every byte,
                // it can switched switched to use counter and call the checkRequestEnd function every four byte (because we will check 4 bytes each time).
            }
            else {
                break;
            }
        }

        String userData = userDataHolder.toString(StandardCharsets.UTF_8);
        return userData;
    }



    private static boolean checkRequestEnd(int leng, byte[] bufferArray, String confimrationResponse) {

        byte[] validBufferArr = Arrays.copyOf(bufferArray, leng);
        String serverReponse = new String(validBufferArr);
        if (leng > 3) {
            if (serverReponse.contains(confimrationResponse))
            {
                return true;
            }
        }
        return false;
    }

}
