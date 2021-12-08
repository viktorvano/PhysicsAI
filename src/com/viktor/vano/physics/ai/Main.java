package com.viktor.vano.physics.ai;

import com.viktor.vano.physics.ai.FFNN.NeuralNetwork;
import com.viktor.vano.physics.ai.FFNN.TrainingData;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.LinkedList;

import static com.viktor.vano.physics.ai.FFNN.FileManagement.*;
import static com.viktor.vano.physics.ai.FFNN.GeneralFunctions.*;
import static com.viktor.vano.physics.ai.FFNN.Weights.*;
import static com.viktor.vano.physics.ai.FFNN.Variables.*;

public class Main extends Application {
    private Pane pane;
    private Button buttonReference, buttonAI, buttonRun, buttonReset;
    private final int width = 1250;
    private final int height = 750;
    private double referenceX = 0;
    private double referenceY = height/8;
    private final int circleRadius = 30;
    private NeuralNetwork myNet;
    private Timeline timelineMovementHandler;
    private boolean run = false;
    private TextField textGravity, textSpeedX, textK;
    private CheckBox checkBoxGenerateTrainingData;

    private double velocity = 0, lastVelocity = 9999999999.0;
    private double g = 150.0, t = 0.025, k = 0.700000000000, xSpeed = 1.5;
    private double ballHeight;
    private double passedTime = 0.0, relativeCoefficient = 18.0;
    private String trainingData = "";
    private NeuralNetworkThread neuralNetworkThread;

    @Override
    public void start(Stage stage){
        pane = new Pane();
        Scene scene = new Scene(pane, width, height);

        stage.setTitle("Physics AI");
        stage.setScene(scene);
        stage.show();
        stage.setMaxWidth(stage.getWidth());
        stage.setMinWidth(stage.getWidth());
        stage.setMaxHeight(stage.getHeight());
        stage.setMinHeight(stage.getHeight());
        stage.setResizable(false);

        neuralNetworkThread = new NeuralNetworkThread();
        neuralNetworkThread.start();

        buttonReference = new Button("");
        buttonReference.setShape(new Circle(circleRadius));
        buttonReference.setMaxSize(circleRadius, circleRadius);
        buttonReference.setMinSize(circleRadius, circleRadius);
        buttonReference.setLayoutX(referenceX);
        buttonReference.setLayoutY(referenceY);
        buttonReference.setStyle("-fx-background-color: #ff0000; ");

        buttonAI = new Button("");
        buttonAI.setShape(new Circle(circleRadius));
        buttonAI.setMaxSize(circleRadius, circleRadius);
        buttonAI.setMinSize(circleRadius, circleRadius);
        buttonAI.setLayoutX(referenceX + circleRadius);
        buttonAI.setLayoutY(referenceY);
        buttonAI.setStyle("-fx-background-color: #00ff00; ");

        checkBoxGenerateTrainingData = new CheckBox("Generate Training Data");
        checkBoxGenerateTrainingData.setLayoutX(300);
        checkBoxGenerateTrainingData.setLayoutY(20);

        buttonRun = new Button("Run");
        buttonRun.setLayoutX(20);
        buttonRun.setLayoutY(20);
        buttonRun.setOnAction(event -> {
            run = true;
            buttonRun.setDisable(true);
            buttonReset.setDisable(false);
            textGravity.setDisable(true);
            textSpeedX.setDisable(true);
            textK.setDisable(true);
        });

        buttonReset = new Button("Reset");
        buttonReset.setLayoutX(120);
        buttonReset.setLayoutY(20);
        buttonReset.setDisable(true);
        buttonReset.setOnAction(event -> {
            run = false;
            velocity = 0;
            lastVelocity = 9999999999.0;
            referenceX = 0;
            referenceY = height/8;
            passedTime = 0.0;
            trainingData = "";
            buttonReference.setLayoutX(referenceX);
            buttonReference.setLayoutY(referenceY);
            buttonAI.setLayoutX(referenceX + circleRadius);
            buttonAI.setLayoutY(referenceY);
            buttonRun.setDisable(false);
            buttonReset.setDisable(true);
            textGravity.setDisable(false);
            textSpeedX.setDisable(false);
            textK.setDisable(false);
        });

        textGravity = new TextField();
        textGravity.setPromptText("Gravity");
        textGravity.setMaxWidth(80);
        textGravity.setLayoutX(200);
        textGravity.setLayoutY(20);
        textGravity.setText(String.valueOf(g));
        textGravity.textProperty().addListener((observableValue, s, t1) -> {
            double number = 0;
            try{
                if(textGravity.getText().length() != 0)
                    number = Double.parseDouble(textGravity.getText());
            }catch (Exception e)
            {
                number = g;
                textGravity.setText(String.valueOf(g));
            }
            if(number < 0)
                number = 0;
            if(number > 900)
                number = 900;

            g = number;
            if(textGravity.getText().length() != 0)
                textGravity.setText(String.valueOf(g));
        });

        textSpeedX = new TextField();
        textSpeedX.setPromptText("X Speed");
        textSpeedX.setMaxWidth(80);
        textSpeedX.setLayoutX(200);
        textSpeedX.setLayoutY(60);
        textSpeedX.setText(String.valueOf(xSpeed));
        textSpeedX.textProperty().addListener((observableValue, s, t1) -> {
            double number = 0;
            try{
                if(textSpeedX.getText().length() != 0)
                    number = Double.parseDouble(textSpeedX.getText());
            }catch (Exception e)
            {
                number = xSpeed;
                textSpeedX.setText(String.valueOf(xSpeed));
            }
            if(number < 0)
                number = 0;
            if(number > 9)
                number = 9;

            xSpeed = number;
            if(textSpeedX.getText().length() != 0)
                textSpeedX.setText(String.valueOf(xSpeed));
        });

        textK = new TextField();
        textK.setPromptText("k");
        textK.setMaxWidth(80);
        textK.setLayoutX(200);
        textK.setLayoutY(100);
        textK.setText(String.valueOf(k));
        textK.textProperty().addListener((observableValue, s, t1) -> {
            double number = 0;
            try{
                if(textK.getText().length() != 0)
                    number = Double.parseDouble(textK.getText());
            }catch (Exception e)
            {
                number = k;
                textK.setText(String.valueOf(k));
            }
            if(number < 0)
                number = 0;
            if(number > 1.00000000000000)
                number = 1.00000000000000;

            k = number;
            if(textK.getText().length() != 0)
                textK.setText(String.valueOf(k));
        });

        pane.getChildren().addAll(buttonReference, buttonAI, buttonRun, buttonReset, textGravity, textSpeedX, textK,
                checkBoxGenerateTrainingData);

        timelineMovementHandler = new Timeline(new KeyFrame(Duration.millis(25), event ->
        {
            if(run)
                movementHandler();
        }));

        timelineMovementHandler.setCycleCount(Timeline.INDEFINITE);
        timelineMovementHandler.play();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        neuralNetworkThread.stopThread();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void movementHandler()
    {
        boolean simulate = (passedTime/relativeCoefficient) < 1.0;
        ballHeight = 732.0 - referenceY;
        velocity += g*t;
        if(velocity <= lastVelocity)
        {
            ballHeight -= (velocity*t);
            referenceX += xSpeed;
            buttonReference.setLayoutX(referenceX);
            buttonAI.setLayoutX(referenceX + circleRadius);
        }
        if(ballHeight <= 0 && velocity <= lastVelocity)
        {
            ballHeight = 0;
            velocity += g*t;
            if(velocity > lastVelocity)
            {
                System.out.println("Current velocity " + velocity + " is greater than the last velocity " + lastVelocity + ".");
                velocity = 0.0;
                if(checkBoxGenerateTrainingData.isSelected())
                {
                    if(writeToFile("res\\training.txt", trainingData))
                        System.out.println("Training data generated and saved.");
                    else
                        System.out.println("Problem saving training data into a file.");
                }
                checkBoxGenerateTrainingData.setSelected(false);
            }

            lastVelocity = velocity;
            System.out.println("Impact velocity: " + velocity);
            velocity = velocity * (-k);
            System.out.println("Bounced velocity: " + velocity + "\n");
        }

        referenceY = 732.0 - ballHeight;
        buttonReference.setLayoutY(referenceY);
        if(simulate)
        {
            neuralNetworkThread.runSingleCycle(passedTime/relativeCoefficient);
            double calculatedHeight = neuralNetworkThread.getOutput()*(double)height;
            buttonAI.setLayoutY(732.0 - calculatedHeight);
            System.out.println("Time:" + passedTime + " Height: " + calculatedHeight + '\n');
        }
        passedTime += 0.025;
        if(checkBoxGenerateTrainingData.isSelected())
            trainingData += "{ " + formatDoubleToString(passedTime/relativeCoefficient) +
                    " }, { " + formatDoubleToString(ballHeight/(double)height) + " },\n";
    }

    class NeuralNetworkThread extends Thread
    {
        private boolean active = true, cycle = false;
        private double time = 0.0, output = 0.0;

        public void stopThread()
        {
            active = false;
        }

        public void runSingleCycle(double time)
        {
            this.time = time;
            cycle = true;
        }

        public double getOutput()
        {
            while (!this.cycle);//waits until FF is finished
            return this.output;
        }

        @Override
        public void run()
        {
            super.run();
            System.out.println("Calling Training function");
            trainNeuralNet();
            System.out.println("Out of Training function");
            while (active)
            {
                try{
                    Thread.sleep(1);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                if(this.cycle)
                {
                    System.out.println("Calling Run function");
                    runCycle();
                    System.out.println("Out of Run function");
                    this.cycle = false;
                }
            }
            System.out.println("Neural Network Thread Stopped.");
        }

        private void trainNeuralNet()
        {
            TrainingData trainData = new TrainingData();
            loadTopology();
            if (topology.size() < 3)
            {
                System.out.println("Topology ERROR:\nTopology is too short, may miss some layer.");
                return;
            }

            myNet = new NeuralNetwork(topology);

            input = new LinkedList<>();
            target = new LinkedList<>();
            result = new LinkedList<>();
            input.clear();
            target.clear();
            result.clear();

            if(weights.size() != get_number_of_weights_from_file())
            {
                load_training_data_from_file();

                System.out.println("Training started\n");
                while (active)
                {
                    trainingPass++;
                    System.out.println("Pass: " + trainingPass);

                    if(myNet.getCurrentError() < myNet.getRecentAverageError())
                        trainingLine = (int)(Math.random()*(double)patternCount);
                    //Get new input data and feed it forward:
                    trainData.getNextInputs(input);
                    showVectorValues("Inputs:", input);
                    myNet.feedForward(input);

                    // Train the net what the outputs should have been:
                    trainData.getTargetOutputs(target);
                    showVectorValues("Targets: ", target);
                    assert(target.size() == topology.peekLast());
                    myNet.backProp(target);//This function alters neurons

                    // Collect the net's actual results:
                    myNet.getResults(result);
                    showVectorValues("Outputs: ", result);


                    // Report how well the training is working, averaged over recent samples:
                    System.out.println("Net recent average error: " + myNet.getRecentAverageError() + "\n\n");

                    if (myNet.getRecentAverageError() < 0.001 && trainingPass>2000)
                    {
                        System.out.println("Exit due to low error :D\n\n");
                        myNet.saveNeuronWeights();
                        break;
                    }
                }
                System.out.println("Training done.\n");
            }else
            {
                System.out.println("Loading weights from file.\n");
                myNet.loadNeuronWeights();
                System.out.println("Weights were loaded from file.\n");
            }

            if(!active)
            {
                System.out.println("Saving neuron weights.");
                myNet.saveNeuronWeights();
                System.out.println("Neuron weights were saved.");
            }

            System.out.println("Run mode begin\n");
            trainingPass = 0;
        }

        private void runCycle()
        {
            //trainingPass++;
            //System.out.println("Run: " + trainingPass);

            //Get new input data and feed it forward:
            //Make sure that your input data are the same size as InputNodes
            input.clear();
            input.add(time);

            //showVectorValues("Inputs:", input);
            myNet.feedForward(input);

            // Collect the net's actual results:
            myNet.getResults(result);
            //showVectorValues("Outputs: ", result);

            this.output = result.get(0);
        }
    }
}
