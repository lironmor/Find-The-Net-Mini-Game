package View;

import Model.IModel;
import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class StartScreenController implements Initializable {
    @FXML
    private Button Start_bu;
    private URL backgroundSound =  this.getClass().getResource("/soundtrack/background.mp3");
    private MediaPlayer mediaPlayer;
    private IView view;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playMusic(backgroundSound);
    }

    private void playMusic(URL path) {
        Media m = new Media(path.toString());
        mediaPlayer = new MediaPlayer(m);
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            public void run() {
                mediaPlayer.seek(Duration.ZERO);
            }
        });
        mediaPlayer.play();
    }

    public void exit(){
        if(view != null)
            view.exit();
    }

    public void StartGame(ActionEvent event ) throws IOException {
        Start_bu.getScene().getWindow().hide();
        Stage window = new Stage();
        window.setTitle("Find The Net");
        window.getIcons().add(new Image("https://upload.wikimedia.org/wikipedia/en/thumb/4/47/FC_Barcelona_%28crest%29.svg/1200px-FC_Barcelona_%28crest%29.svg.png"));
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/fxml/MyView.fxml"));
        Parent root = (Parent) fxmlLoader.load();
        Scene scene = new Scene(root,1020,620);
        window.setScene(scene);
        window.show();
        IModel model = new MyModel();
        MyViewModel viewModel = new MyViewModel(model);
        view = fxmlLoader.getController();
        view.setViewModel(viewModel);
        viewModel.addObserver(view);
    }


}
