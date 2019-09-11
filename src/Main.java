import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class Main extends Application {

    ArrayList<String> listOfFiles = new ArrayList<>();
    static String pathName;
    static String userName;

    int serverCode;
    int clientCode = 0;

    public static void main(String... args) throws Exception{
        if(args.length!=2)
            System.exit(1);

        pathName = args[0];
        //System.out.println(pathName);
        userName = args[1];
        launch(args);

    }

    public ArrayList<String> getListOfFiles(){

        ArrayList<String> listOfFiles = new ArrayList<>();
        File folder = new File(pathName);
        File[] listofFilesAndDirectoried = folder.listFiles();

        for (int i = 0; i < listofFilesAndDirectoried.length; i++)
            if (listofFilesAndDirectoried[i].isFile())
                listOfFiles.add(listofFilesAndDirectoried[i].getName());
        return listOfFiles;
    }

    @Override
    public void start(Stage stage) throws Exception {
        GridPane gridPane = new GridPane();

        BorderPane borderPane = new BorderPane();
        gridPane.add(borderPane,0,0);

        SplitPane splitPane = new SplitPane();
        borderPane.setTop(splitPane);

        //AnchorPane anchorPaneOne = new AnchorPane();
       // splitPane.getItems().add(anchorPaneOne);
        //AnchorPane anchorPaneTwo = new AnchorPane();
       // splitPane.getItems().add(anchorPaneTwo);

        Button button = new Button("Send");
        splitPane.getItems().add(button);
        ComboBox comboBox = new ComboBox();
        splitPane.getItems().add(comboBox);

        ListView listView = new ListView();
        borderPane.setCenter(listView);
        Label label = new Label("XD");
        borderPane.setBottom(label);

        Scene scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.show();
        //Koniec przygotowywania wyglądu

        //Żaby można było zamykać "x" w prawym górnym rogu
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        new Thread(() -> {

            listOfFiles = getListOfFiles();
            Platform.runLater(() -> listView.getItems().addAll(listOfFiles));

            final int delay = (1000/25); //miliseconds
            ActionListener taskPerformer = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    listOfFiles = getListOfFiles();
                    Platform.runLater(() -> listView.getItems().clear());
                    Platform.runLater(() -> listView.getItems().addAll(listOfFiles));
                }
            };

            Timer timer = new Timer(delay,taskPerformer);
            timer.start();

            try {
                Socket clientSocket = new Socket("localhost", 1207);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                DataOutputStream userNameToServer = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                userNameToServer.writeBytes(userName + '\n');
                while(true){
                    outToServer.write(clientCode);
                    serverCode = inFromServer.read();
                    System.out.println("FROM SERVER: " + serverCode);
                    //outToServer.flush();
                    //clientSocket.close();
                }
            } catch (IOException e){
                System.out.println(e.getStackTrace());
            }

        }).start();
    }
}
