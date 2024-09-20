package com.example.pillanalyser;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.Random;


public class pillChooseController {

    public Button button;
    public ImageView imageView;
    public ImageView imageView1;
    public Button noise;
    public ImageView imageView2;
    int[] disjoint;
    public Color sample;

    public void chooseImage() {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("Files", "*.JPG");
        fileChooser.getExtensionFilters().addAll(extFilterPNG, extFilterJPG);

        //Show open file dialog
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            Image image = new Image(file.toURI().toString(), imageView.getFitWidth(), imageView.getFitHeight(), false, true); // image fits image view
            imageView.setImage(image);
        }
    }

    public void sampleImageColour(MouseEvent mouseEvent) { //get hue of chosen pill
        PixelReader pr = imageView.getImage().getPixelReader();

        sample = (pr.getColor((int) mouseEvent.getX(), (int) mouseEvent.getY()));

        System.out.println("Hue: " + sample.getHue());
        if (imageView1.getImage() == null) {
            processImage(sample);
        } else {
            multipleSelect(sample);

        }

        drawRect();
        OSPillNum();
    }

    public void countNumPixels(MouseEvent mouseEvent) {
        int width = (int) imageView1.getImage().getWidth();
        int height = (int) imageView1.getImage().getHeight();
        int clickedRoot = find(disjoint, ((int) mouseEvent.getY()) * width + ((int) mouseEvent.getX()));
        int pixelCount = 0;

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int index = y * width + x;

                if (find(disjoint, index) == clickedRoot) pixelCount++;

            }
        System.out.println("The size of set id " + clickedRoot + " is " + pixelCount);
    }

    public void reduce(ActionEvent actionEvent) { //todo write method to count how many pixels each set and if less that 35 turn black in image then call method to turn them -1
        WritableImage wimg = new WritableImage((int) imageView1.getImage().getWidth(), (int) imageView1.getImage().getHeight());
        PixelWriter pw = wimg.getPixelWriter();
        int pixelCount = 0;

        for (int y = 0; y < wimg.getHeight(); y++)
            for (int x = 0; x < wimg.getWidth(); x++) {
                int index = y * (int) wimg.getWidth() + x;

                if(index + 1 < disjoint.length && index + wimg.getWidth() < disjoint.length && disjoint[index] != -1 && find(disjoint,index) == disjoint[index]){//if index not -1 and the index == the root
                    pixelCount ++;
                    //if(){//if disjoint set is less than 35
                  //  }
                }
            }
        System.out.println(pixelCount);

    }
    public void processImage(Color sample) { //black/white image conversion
        WritableImage wimg = new WritableImage((int) imageView.getImage().getWidth(), (int) imageView.getImage().getHeight());
        PixelReader pr = imageView.getImage().getPixelReader();
        PixelWriter pw = wimg.getPixelWriter();

        for (int y = 0; y < wimg.getHeight(); y++)
            for (int x = 0; x < wimg.getWidth(); x++) {
                Color col = pr.getColor(x, y);
                if (Math.abs(col.getHue() - sample.getHue()) <= 10) {
                    pw.setColor(x, y, Color.WHITE);
                } else pw.setColor(x, y, Color.BLACK);
            }
        imageView1.setImage(wimg);
        unionPixel();
    }

    public void multipleSelect(Color sample) {
        WritableImage wimg = new WritableImage((int) imageView.getImage().getWidth(), (int) imageView.getImage().getHeight());
        PixelReader pr = imageView.getImage().getPixelReader();
        PixelReader px = imageView1.getImage().getPixelReader();
        PixelWriter pw = wimg.getPixelWriter();

        for (int y = 0; y < wimg.getHeight(); y++) { //get every pixel height ways
            for (int x = 0; x < wimg.getWidth(); x++) { // get every pixel width ways
                Color col = pr.getColor(x, y); // get colour of chosen pixel in original image

                if (px.getColor(x, y).equals(Color.WHITE)) {
                    pw.setColor(x, y, Color.WHITE);
                } else if (Math.abs(col.getHue() - sample.getHue()) <= 10 && px.getColor(x, y).equals(Color.BLACK)) { //if pixel is same hue in original and is black in conversion
                    pw.setColor(x, y, Color.WHITE);//change to white
                } else pw.setColor(x, y, Color.BLACK);//stays black
            }
        }
        imageView1.setImage(wimg);
        unionPixel();
    }


    public void UnionFind() { //parent array
        disjoint = new int[(int) imageView1.getImage().getWidth() * (int) imageView1.getImage().getHeight()];
        for (int i = 0; i < disjoint.length; i++) {
            disjoint[i] = i;
        }
    }

    public static int find(int[] a, int i) { // find root
        if (a[i] == -1) return -1;
        while (a[i] != i) i = a[i];
        return i;
    }

    public static void union(int[] a, int p, int q) { //join if have same root
        a[find(a, q)] = find(a, p);
    }

    public void unionPixel() {
        UnionFind();
        PixelReader pr = imageView1.getImage().getPixelReader();
        int width = (int) imageView1.getImage().getWidth();
        int height = (int) imageView1.getImage().getHeight();


        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                if (pr.getColor(x, y).equals(Color.BLACK)) {
                    disjoint[y * width + x] = -1;
                }
            }


        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int index = y * width + x;

                if (index + 1 < disjoint.length && disjoint[index] != -1 && disjoint[index + 1] != -1)
                    union(disjoint, index, index + 1);

                if (index + width < disjoint.length && disjoint[index] != -1 && disjoint[index + width] != -1)
                    union(disjoint, index, index + width);


            }

        displayDSAsText(width);
    }


    public void displayDSAsText(int width) {
        for (int i = 0; i < disjoint.length; i++) //from board
            System.out.print(find(disjoint, i) + ((i + 1) % width == 0 ? "\n" : " ")); // from board
    }

//    public double pillWidth() {
//        int width = (int) imageView1.getImage().getWidth();
//        int height = (int) imageView1.getImage().getHeight();
//        int w = 0;
//        for (int y = 0; y < height; y++)
//            for (int x = 0; x < width; x++) {
//                int index = y * width + x;
//
//
//                if (disjoint[index] == index) {
//                    disjoint[index] ++;
//                    while (disjoint[index] != -1){
//                        w++;
//                    }
//                }
//            }
//        return w;
//    }


    public void drawRect(){ // figure out height and width
        int width = (int) imageView1.getImage().getWidth();
        int height = (int) imageView1.getImage().getHeight();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int index = y * width + x;

                if (index + 1 < disjoint.length && index + width < disjoint.length && disjoint[index] != -1 &&  disjoint[index] == index) { //find(disjoint,index)) {
                    Rectangle rect = new Rectangle(x, y-10, 25, 25);
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.BLUE);
                    rect.setStrokeWidth(2);
                    ((Pane) imageView.getParent()).getChildren().add(rect);
                }
            }

    }

    public void OSPillNum() { //use clear method before loop

        int width = (int) imageView1.getImage().getWidth();
        int height = (int) imageView1.getImage().getHeight();
        int pill = 1;
        ((Pane) imageView.getParent()).getChildren().removeIf(e->e instanceof Text);
        //((Pane) imageView.getParent()).getChildren().remove(text);

        for (int y = 1; y < height; y++) {
            for (int x = 1; x < width; x++) {
                int index = y * width + x;
                if (index + 1 < disjoint.length && index + width < disjoint.length && disjoint[index] != -1 &&  disjoint[index] == index) {
                    Text text = new Text(String.valueOf(pill));
                    text.getParent();
                    text.setX(x+10);
                    text.setY(y+10);
                    ((Pane) imageView.getParent()).getChildren().add(text);
                    pill++;
                }
            }
        }
    }


    public Color paintRandom() { // creates a random colour every time method is called
        Random random = new Random();
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        return Color.rgb(r, g, b);
    }

    public void showRandom() {
        WritableImage wimg = new WritableImage((int) imageView1.getImage().getWidth(), (int) imageView1.getImage().getHeight());
        PixelReader pr = imageView1.getImage().getPixelReader();
        PixelWriter pw = wimg.getPixelWriter();

        int width = (int) imageView1.getImage().getWidth();
        int height = (int) imageView1.getImage().getHeight();
        Color col = paintRandom();

        for (int y = 1; y < height; y++) {
            for (int x = 1; x < width; x++) {
                int index = y * width + x;

                if (index + 1 < disjoint.length && index + width < disjoint.length && disjoint[index] != -1 && disjoint[index] == index) { // paints the root random colour
                    pw.setColor(x, y,paintRandom());
                    }
               else if (disjoint[index] != -1 && disjoint[index] != index) {
                    pw.setColor(x,y,col);
                }
                 else pw.setColor(x, y, Color.BLACK);
            }
        }

            imageView2.setImage(wimg);
        }



    public void showSample() {//todo sample is only for latest hue selected
        WritableImage wimg = new WritableImage((int) imageView1.getImage().getWidth(), (int) imageView1.getImage().getHeight());
        PixelReader pr = imageView1.getImage().getPixelReader();
        PixelWriter pw = wimg.getPixelWriter();

        for (int y = 0; y < wimg.getHeight(); y++)
            for (int x = 0; x < wimg.getWidth(); x++) {
                if (pr.getColor(x, y).equals(Color.WHITE)) {
                    pw.setColor(x, y, sample);
                } else pw.setColor(x, y, Color.BLACK);

                imageView2.setImage(wimg);
            }

    }


    public int numOfPills() {
        int width = (int) imageView1.getImage().getWidth();
        int height = (int) imageView1.getImage().getHeight();
        int num = 0;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                {
                    if (index + 1 < disjoint.length && index + width < disjoint.length && disjoint[index] != -1 &&  disjoint[index] == index) { //find(disjoint,index)) {
                         num ++;
                    }
                }
            }
        System.out.println("Number: " + num);
        return num;
    }

}