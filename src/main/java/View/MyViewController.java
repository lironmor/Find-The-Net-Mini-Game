package View;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import ViewModel.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;


public class MyViewController implements Initializable,IView, Observer {
    private MyViewModel viewModel;
    private int[][] maze = null;
    private int[][] solution = null;
    public MenuItem solveGame;
    public MenuItem saveGame;
    public MazeDisplayer mazeDisplayer;
    public Label playerRow;
    public Label playerCol;
    public Label playerRowText;
    public Label playerColText;
    StringProperty updatePlayerRow = new SimpleStringProperty();
    StringProperty updatePlayerCol = new SimpleStringProperty();
    private URL solveSound = this.getClass().getResource("/soundtrack/champions.mp3");
    private MediaPlayer mediaPlayer1;
    private boolean isControllPressed = false;
    private boolean isDragged = false;

    private double locX = 0;
    private double locY = 0;


    public MyViewController() {}


    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public String getUpdatePlayerRow() {
        return (String)this.updatePlayerRow.get();
    }

    public void setUpdatePlayerRow(int updatePlayerRow) {
        this.updatePlayerRow.set(updatePlayerRow + "");
    }

    public String getUpdatePlayerCol() {
        return (String)this.updatePlayerCol.get();
    }

    public void setUpdatePlayerCol(int updatePlayerCol) {
        this.updatePlayerCol.set(updatePlayerCol + "");
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.playerRowText.setVisible(false);
        this.playerColText.setVisible(false);
        this.playerRow.textProperty().bind(this.updatePlayerRow);
        this.playerCol.textProperty().bind(this.updatePlayerCol);
        this.solveGame.setDisable(true);
        this.saveGame.setDisable(true);
    }


    private void playEffect(URL path){
        Media m = new Media(path.toString());
        mediaPlayer1 = new MediaPlayer(m);
        mediaPlayer1.setOnEndOfMedia(new Runnable() {
            public void run() {
                mediaPlayer1.stop();
            }
        });
        mediaPlayer1.setStartTime(new Duration(53000));
        mediaPlayer1.play();
    }

    @Override
    public void stopEffect(){
        mediaPlayer1.stop();
    }

    public void generateMaze(int rows, int cols) {
        viewModel.generateMaze(rows,cols);
        this.solveGame.setDisable(false);
        this.playerRowText.setVisible(true);
        this.playerColText.setVisible(true);
    }

    public void solveGame(ActionEvent actionEvent) {
        viewModel.solveMaze();
    }

    public void saveGame(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save maze");
        fc.getExtensionFilters().add(new ExtensionFilter("Maze files (*.maze)", new String[]{"*.maze"}));
        fc.setInitialDirectory(new File("./resources"));
        File chosen = fc.showSaveDialog((Window)null);
        viewModel.saveGame(chosen);
    }

    public void openFile(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open maze");
        fc.getExtensionFilters().add(new ExtensionFilter("Maze files (*.maze)", new String[]{"*.maze"}));
        fc.setInitialDirectory(new File("./resources"));
        File chosen = fc.showOpenDialog((Window)null);
        viewModel.loadGame(chosen);
    }

    public void exitGame(ActionEvent actionEvent) {
        exit();
    }

    public void exit(){viewModel.exitGame();}

    public void keyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.CONTROL){
            isControllPressed = true;
        }
        else {
            viewModel.updateCharacterLocation(keyEvent.getCode());
        }
        keyEvent.consume();
    }


    public void keyReleased(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.CONTROL)
            isControllPressed = false;
        keyEvent.consume();
    }

    public void setPlayerPosition(int row, int col) {
        this.mazeDisplayer.setPlayerPosition(row, col);
        this.setUpdatePlayerRow(row);
        this.setUpdatePlayerCol(col);
    }

    public void mouseClicked(MouseEvent mouseEvent) {
       this.mazeDisplayer.requestFocus();
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof MyViewModel){
            if(maze == null){ //generate maze
                generate(viewModel.getMaze());
            }
            else{
                int[][] tmpMaze = viewModel.getMaze();

                if(this.maze == tmpMaze){
                    if(Integer.parseInt(playerRow.getText()) != viewModel.getRowChar() ||Integer.parseInt(playerCol.getText()) != viewModel.getColChar()){
                        int row = viewModel.getRowChar();
                        int col = viewModel.getColChar();
                        if(row == viewModel.getRowCharGoal() && col == viewModel.getColCharGoal()){
                            playEffect(solveSound);
                            mazeDisplayer.setDisable(true);
                            Stage winWindow = new Stage();
                            winWindow.setTitle("Champion");
                            winWindow.getIcons().add(new Image("https://upload.wikimedia.org/wikipedia/en/thumb/4/47/FC_Barcelona_%28crest%29.svg/1200px-FC_Barcelona_%28crest%29.svg.png"));
                            FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/fxml/WinScene.fxml"));
                            Parent root2 = null;
                            try {
                                root2 = (Parent) fxmlLoader.load();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Scene scene = new Scene(root2,303,297);
                            winWindow.setScene(scene);
                            winWindow.show();
                            WinSceneController view = fxmlLoader.getController();
                            view.setMainView(this);
                        }
                        setPlayerPosition(row, col);
                    }
                    else{
                        if(viewModel.getSolution() != null) {
                            this.solution = viewModel.getSolution();
                            this.mazeDisplayer.drawSolution(solution);
                            this.mazeDisplayer.requestFocus();
                        }
                    }
                }
                else{
                    generate(tmpMaze);
                }
            }
        }
    }

    public void openNewMazeWindow(ActionEvent actionEvent) throws IOException {
        Stage window = new Stage();
        window.setTitle("Create New Training");
        window.getIcons().add(new Image("https://upload.wikimedia.org/wikipedia/en/thumb/4/47/FC_Barcelona_%28crest%29.svg/1200px-FC_Barcelona_%28crest%29.svg.png"));
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/fxml/NewMaze.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        Scene scene = new Scene(root1,300,200);
        window.setScene(scene);
        window.show();
        NewMazeMenuController view = fxmlLoader.getController();
        view.setMainView(this);
    }

    public void openProperties(ActionEvent actionEvent) throws IOException {
        Stage window = new Stage();
        window.setTitle("Properties");
        window.getIcons().add(new Image("https://cdn6.aptoide.com/imgs/2/4/0/240d1f1b423aaddc8e733c3d496a4f18_screen.png"));
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/fxml/Properties.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        Scene scene = new Scene(root1,400,300);
        window.setScene(scene);
        window.show();
        PropertiesController view = fxmlLoader.getController();
        view.setMainView(this);
    }

    public void setProperties(String sol, String gen, String nThreads){
        viewModel.setProperties(sol,gen,nThreads);
    }


    public void generate(int[][] tmpMaze){
        this.maze = tmpMaze;
        this.mazeDisplayer.setRowCharGoal(viewModel.getRowCharGoal());
        this.mazeDisplayer.setColCharGoal(viewModel.getColCharGoal());
        this.solution = null;
        this.mazeDisplayer.drawMaze(maze);
        this.mazeDisplayer.setDisable(false);
        this.saveGame.setDisable(false);
        this.mazeDisplayer.requestFocus();
        this.setPlayerPosition(viewModel.getRowChar(), viewModel.getColChar());

    }

    public void zoom(ScrollEvent scrollEvent) {
        if(isControllPressed){
            if(scrollEvent.getDeltaY() > 0) {
                mazeDisplayer.ZoomIn();
            }
            else if(scrollEvent.getDeltaY() < 0){
                mazeDisplayer.ZoomOut();
            }
        }
        scrollEvent.consume();
    }

    public void mousePressed(MouseEvent mouseEvent) {
        locX = mouseEvent.getX();
        locY = mouseEvent.getY();
    }

    public void mouseDragged(MouseEvent mouseEvent) {
        if(isDragged && enoughToMove(mouseEvent, locX, locY)){
            viewModel.updateCharacterLocationMouse(mouseEvent, locX ,locY, mazeDisplayer.getCellHeight(),mazeDisplayer.getCellWidth());
            locX = mouseEvent.getX();
            locY = mouseEvent.getY();
        }
    }

    private boolean enoughToMove(MouseEvent mouseEvent, double locX, double locY) {
        double width = mazeDisplayer.getCellWidth();
        double height = mazeDisplayer.getCellHeight();
        return(Math.abs(mouseEvent.getX() - locX) > width || Math.abs(mouseEvent.getY() - locY) > height);
    }

    public void dragDetected(MouseEvent mouseEvent) {
        locX = mouseEvent.getX();
        locY = mouseEvent.getY();
        isDragged = true;
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        isDragged = false;
        dragDone(mouseEvent);
        mouseEvent.consume();
    }

    public void dragDone(MouseEvent mouseEvent){
        locX = mouseEvent.getX();
        locY = mouseEvent.getY();
    }

    public void openAbout(ActionEvent actionEvent) throws IOException {
        Stage window = new Stage();
        window.setTitle("About");
        window.getIcons().add(new Image("https://cdn6.aptoide.com/imgs/2/4/0/240d1f1b423aaddc8e733c3d496a4f18_screen.png"));
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/fxml/About.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        Scene scene = new Scene(root1,530,330);
        window.setScene(scene);
        window.show();
    }

    public void openHelp(ActionEvent actionEvent) throws IOException {
        Stage window = new Stage();
        window.setTitle("Help");
        window.getIcons().add(new Image("https://cdn6.aptoide.com/imgs/2/4/0/240d1f1b423aaddc8e733c3d496a4f18_screen.png"));
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/fxml/Help.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        Scene scene = new Scene(root1,530,330);
        window.setScene(scene);
        window.show();
    }
}