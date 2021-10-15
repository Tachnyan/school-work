
/*
* Assignment 2 - MNIST Neural Net
* CSC 475 - Artificial Intelligence
*
* This program was designed and written to run in the cmdLine.
*
* This program is a neural network designed to read and evaluate the 
* MNIST handwritten digit dataset. It is capable of: 
* -training a new net by randomly generating weights and biases for each layer
* -Saving and Loading sets of trained weights and biases
* -Testing the accuracy of the nets current weights and biases across training and testing data
* -Graphically Display Test Data Using ASCII art. 
*
*/

import java.io.*;
import java.util.Random;
import java.util.Scanner;

import java.util.List;
import java.util.ArrayList;

// All methods in this class are static to avoid having to instantiate the class as an object. 
final class MNISTNeuralNet {

    //Set Number of nodes for input, output, and hidden layers. 
    final static int inputSize = 784; 
    final static int outputSize = 10; 
    final static int hiddenSize = 15; 

    final static int miniBatchSize = 10;
    final static int epochSize = 30;
    final static double learningRate = 3;

    //initialize static scanner for user input that can be used throughout the program.
    private static final Scanner sc = new Scanner(System.in);

    // initialize all neuron matrices. Number indicates layer starting with input at 0.
    // layer outputs
    public static double[][] a0 = new double[inputSize][1];
    public static double[][] a1 = new double[hiddenSize][1];
    public static double[][] a2 = new double[outputSize][1];

    // layer weights
    public static double[][] w1 = new double[hiddenSize][inputSize];
    public static double[][] w2 = new double[outputSize][hiddenSize];

    // layer biases
    public static double[][] b1 = new double[hiddenSize][1];
    public static double[][] b2 = new double[outputSize][1];

    //gradients
    public static double[][] b1Grad = new double[hiddenSize][1];
    public static double[][] b2Grad = new double[outputSize][1];
    public static double[][] w1Grad = new double[hiddenSize][inputSize];
    public static double[][] w2Grad = new double[outputSize][hiddenSize];

    private static File trainingFile = new File("mnist_train.csv");
    private static File testingFile = new File("mnist_test.csv");

    
    // matrix multiplication func.
    public static double[][] matrixMultiply(double[][] a, double[][] b) {
        int i, j, k = 0;
        double[][] productMatrix = new double[a.length][b[0].length]; //product matrix equal to a.height by b.width
        for (i = 0; i < a.length; i++) { //height
            for (j = 0; j < b[0].length; j++) { //width
                double sumOfProducts = 0;
                for (k = 0; k < b.length; k++) { 
                    sumOfProducts = sumOfProducts + (a[i][k] * b[k][j]);
                }
                productMatrix[i][j] = sumOfProducts;
            }
        }

        return productMatrix;
    }

    // matrix addition func.
    public static double[][] matrixAdd(double[][] a, double[][] b) {
        int i, j = 0;
        double[][] sumMatrix = new double[a.length][a[0].length];
        for (i = 0; i < a.length; i++) { //height
            for (j = 0; j < a[0].length; j++) { //width
                sumMatrix[i][j] = a[i][j] + b[i][j];
            }
        }

        return sumMatrix;
    }

    // iterate over matrix and apply sigma function to each output.
    public static double[][] sigma(double[][] a)
    {
        double[][] output = new double[a.length][a[0].length];
        for(int i = 0; i < a.length; i++)
        {
            for(int j = 0; j < a[0].length; j++)
            {
                output[i][j] = 1 / (1 + Math.pow(Math.E, -a[i][j])); 
            }
        }
        return output;
    }

    //get the cost function gradient. C = 1/2 * FOR 1 to n SUM Math.pow((a[i] - y[i]), 2)
    // dC/da = SUM(a[i] - y[i])
    public static double[][] calcError(double[][] expected)
    {
        double[][] errorVector = new double[outputSize][1];
        for(int i = 0; i < outputSize; i++)
        {
            errorVector[i][0] = a2[i][0] - expected[i][0];
        }
        
        return errorVector;
    }
    
    public static void backpropogation(double[][] expected)
    {
        //get error vector
        double[][] errorVector = calcError(expected);

        //calc final bias layer, cur gradient is used to calculate all other gradients for this input.
        //normal grad matrix, will hold the sum of all the gradients over the minibatch. 
        double[][] currentB2Grad = new double[outputSize][1];
        for(int i = 0; i < outputSize; i++)
        {
            currentB2Grad[i][0] = errorVector[i][0] * a2[i][0] * (1 - a2[i][0]);
            b2Grad[i][0] = b2Grad[i][0] + currentB2Grad[i][0];
        }

        //hidden layer bias
        double[][] currentB1Grad = new double[hiddenSize][1];
        for(int i = 0; i < hiddenSize; i++)
        {
            double temp = 0;
            for(int j = 0; j < outputSize; j++)
            {
                temp = temp + (w2[j][i] * currentB2Grad[j][0]);
            }
            currentB1Grad[i][0] = temp * a1[i][0] * (1 - a1[i][0]);
            b1Grad[i][0] = b1Grad[i][0] + currentB1Grad[i][0];
        }

        //final layer weight
        double[][] currentW2Grad = new double[outputSize][hiddenSize];
        for(int i = 0; i < outputSize; i++)
        {
            for(int j = 0; j < hiddenSize; j++)
            {
                currentW2Grad[i][j] = (a1[j][0] * currentB2Grad[i][0]);
                w2Grad[i][j] = w2Grad[i][j] + currentW2Grad[i][j];
            }
        }

        //hidden weights
        double[][] currentW1Grad = new double[hiddenSize][inputSize];
        for(int i = 0; i < hiddenSize; i++)
        {
            for(int j = 0; j < inputSize; j++)
            {
                currentW1Grad[i][j] = a0[j][0] * currentB1Grad[i][0];
                w1Grad[i][j] = w1Grad[i][j] + currentW1Grad[i][j];
            }
        }

    }

    //Display Main menu. state used to determine what options are displayed. 
    public static void displayMain(int state)
    {

        System.out.println("======================================================================================");
        System.out.println("MNIST NEURAL NET - CSC475 ASSIGNMENT 2 - AUTHOR: Tachnyan");
        System.out.println();
        System.out.println("Enter your desired command...\n");
        if(state != -1)System.out.print("[1] Train \n[2] Load Network\n[3] Training Accuracy\n[4] Testing Accuracy\n[5] Save Network\n[6]Display All Testing Inputs\n[7]Display Mis-classified Testing Inputs\n[0] Exit\n");
        else System.out.print("[1] Train \n[2] Load Network\n[0] Exit\n");
        return;

    }

    //get Menu Input. state is used to determine what inputs are accepted. 
    public static int getInput(int state)
    {
        int input = -1;
        while(sc.hasNext() || input == -1)
        {
            //prevent non int inputs from being used. 
            try
            {
                input = sc.nextInt();
            } catch (Exception e) 
            {
                System.out.println("Invalid Input: Please Input Command Number..");
                sc.next();
                input = -1;
            }

            //prevent out of bound int inputs
            if(input != -1 && (input < 0 || input > 7))
            {
                System.out.println("Invalid Input: Please Input Command Number..");
            }
            
            //all options available unless state is -1, then only train and load. 
            if(state != -1 && input >= 0 && input <= 7)
            {
                break;
            }else if(input >= 0 && input <= 2)
            {
                break;
            }else System.out.println("Invalid Input: Please Input Command Number..");
            
        }

        return input;
    }

    //switch case statement to determine what cmd to run. 
    public static void runCmd(int input) throws Exception
    {
        switch(input)
        {
            case 0:
                //exit program
                System.out.println("Goodbye..");
                System.exit(0);
                break;
            case 1:
                //Train net
                train();
                break;
            case 2:
                //Load net
                load();
                break;
            case 3:
                //Test net on training data
                testAccuracy(1, 0);
                break;
            case 4:
                //Test net on testing data
                testAccuracy(2, 0);
                break;
            case 5:
                //Save net
                save();
                break;
            case 6:
                //Display all Test Data Inputs
                testAccuracy(2, 1);
                break;
            case 7:
                //Display Incorrect Test Data Inputs
                testAccuracy(2, 2);
                break;
            default:
                System.out.println("Error: Incorrect Input. I don't know how you got here, but you did..");
                sc.nextLine();
        }
        return;
    }

    //function to avoid arrays passing their reference as a value.
    public static double[] copyArray(double[] a)
    {
        double[] arr = new double[inputSize+1];
        for(int i = 0; i < inputSize+1; i++)
        {
            arr[i] = a[i];
        }
        return arr;
    }

    //function for reading the training and testing data into an ArrayList.
    public static List<double[]> readFile(File f) throws Exception
    {

        
        List<double[]> dataList = new ArrayList<double[]>();
        double[] currentLine = new double[inputSize + 1];
        Scanner fscan = new Scanner(f);
        fscan.useDelimiter("(,|\r\n|[\r\n])"); //Delimiter catches commas, and EOL for the 3 major OSs. 
        int i = 0, j = 0;
        while(fscan.hasNext())
        {
            
            currentLine[i] = (double)fscan.nextInt();
            i++;
            if(i == inputSize + 1) 
            {
                dataList.add(copyArray(currentLine)); //this bit only exists because java's strict 'pass by value', makes arrays pass their reference and not a copy of the array. 
                i = 0;
                j++;

                if (j == 20000)
                {
                    break;
                }
            }
            

        }
        fscan.close();

        return dataList;
    }

    //makes our expected output vector. 
    public static double[][] getOutputVector(int a) throws Exception
    {
        //initialiaze array, then use a switch case to flip the correct output.
        double[][] out = { {0}, {0}, {0}, {0}, {0}, {0}, {0}, {0}, {0}, {0} };
        switch (a) {
            case 0:
                out[0][0] = 1;
                break;
            case 1:
                out[1][0] = 1;
                break;
            case 2:
                out[2][0] = 1;
                break;
            case 3:
                out[3][0] = 1;
                break;
            case 4:
                out[4][0] = 1;
                break;
            case 5:
                out[5][0] = 1;
                break;
            case 6:
                out[6][0] = 1;
                break;
            case 7:
                out[7][0] = 1;
                break;
            case 8:
                out[8][0] = 1;
                break;
            case 9:
                out[9][0] = 1;
                break;
            default:
                break;
        }
        return out;
    }

    //calls update matrix for each weight and bias matrix. 
    public static void updateNet()
    {
        b2 = updateMatrix(b2, b2Grad);
        b1 = updateMatrix(b1, b1Grad);
        w2 = updateMatrix(w2, w2Grad);
        w1 = updateMatrix(w1, w1Grad);
    }
    
    //Gradient Descent update function. Updates bias or weight matrix with the new weight after each mini batch. 
    public static double[][] updateMatrix(double[][] old, double[][] gradient)
    {
        double[][] updated = new double[old.length][old[0].length];
        for(int i = 0; i < old.length; i++)
        {
            for(int j = 0; j < old[0].length; j++)
            {
                updated[i][j] = old[i][j] - (learningRate/10) * gradient[i][j];
            }
        }
        return updated;
    }

    //Training function. the SGD starts here. 
    public static void train() throws Exception
    {

        randomizeMatrices(); //initialize our matrices. 

        //check if expected training file exists. if not get file location from user.
        while(!trainingFile.exists())
        {
            System.out.println("training file not found. please enter file location..\n");
            trainingFile = new File(sc.nextLine());
        }

        List<double[]> controlList = readFile(trainingFile); //make a control list to make copies from. 
        
        int currentEpoch = 0, currentMiniCount = 0;
        int listSize = controlList.size();
        double[][] expected = new double[10][1];
        while (currentEpoch != epochSize)
        {
            Random rand = new Random();
            List<double[]> copyList = new ArrayList<double[]>(controlList); //make a copy of the control, because we'll be using Random. to grab and remove each index. 
            
            // used to tally our results
            int[][] results = new int[10][2]; 
            int correct = 0, total = 0;

            //stochastic starts here. 
            for(int i = 0; i < listSize; i++)
            {
                //get random item from training list and remove. 
                int randIndex = rand.nextInt(copyList.size());
                double[] trainingItem = new double[inputSize+1];
                trainingItem = copyList.get(randIndex);
                copyList.remove(randIndex);

                //make exptected output vector. 
                expected = getOutputVector((int)trainingItem[0]);

                //scale input to be between 0 - 1
                for(int j = 1; j < trainingItem.length; j++)
                {
                    a0[j-1][0] = trainingItem[j]/255.0d;
                }
                
                forwardPass();
                backpropogation(expected);

                //tally result, compare and swap max with highest output, and grab the index. 
                double max = 0;
                int maxIndex = 0;
                for(int k = 0; k < outputSize; k++)
                {
                    if(max < a2[k][0])
                    {
                        max = a2[k][0];
                        maxIndex = k;
                    }
                }

                //trainingItem[0] holds the expected int value, compare to maxIndex
                if((int)trainingItem[0] == maxIndex)
                {
                    //success, increment correct and totals. 
                    results[(int)trainingItem[0]][0]++;
                    results[(int)trainingItem[0]][1]++;
                    correct++;
                    total++;
                }else
                {
                    //failure, increment totals. 
                    results[(int)trainingItem[0]][1]++;
                    total++;
                }
                
                //the descent part of SDG. When we reach batch size, update net, and reset gradient matrices and curMiniBatch count.
                currentMiniCount++;
                if(currentMiniCount == miniBatchSize)
                {
                    updateNet();
                    b1Grad = new double[hiddenSize][1];
                    b2Grad = new double[outputSize][1];
                    w1Grad = new double[hiddenSize][inputSize];
                    w2Grad = new double[outputSize][hiddenSize];
                    currentMiniCount = 0;
                    
                }


            }

            //Output results for each epoch. Tried to make it look nice. 
            System.out.println("Epoch " + (currentEpoch+1) + " Results");
            outputResults(results, correct, total);
            System.out.println("========================================================");

            currentEpoch++;
            
        }

    }

    //function for outputting the results, so I didn't have to type this twice. 
    public static void outputResults(int[][] results, int correct, int total)
    {
        System.out.println("0 = " + results[0][0] + "/" + results[0][1] 
                           + "  1 = " + results[1][0] + "/" + results[1][1] 
                           + "  2 = " + results[2][0] + "/" + results[2][1] 
                           + "  3 = " + results[3][0] + "/" + results[3][1] 
                           + "  4 = " + results[4][0] + "/" + results[4][1]  
                           + "  5 = " + results[5][0] + "/" + results[5][1]
                           + "\n6 = " + results[6][0] + "/" + results[6][1] 
                           + "  7 = " + results[7][0] + "/" + results[7][1] 
                           + "  8 = " + results[8][0] + "/" + results[8][1] 
                           + "  9 = " + results[9][0] + "/" + results[9][1]
                           + "  Accuracy = " + correct + "/" + total + " = " + (((double)correct/(double)total)*100) + "%");
    }

    //forward. 
    public static void forwardPass()
    {
        double[][] temp = new double[w1.length][a0[0].length]; // to hold the product of w1 * a0
        temp = matrixMultiply(w1, a0);
        a1 = matrixAdd(temp, b1);
        a1 = sigma(a1);
        temp = new double[w2.length][a1[0].length]; // to hold the product of w2 * a1
        temp = matrixMultiply(w2, a1);
        a2 = matrixAdd(temp, b2);
        a2 = sigma(a2);
    }

    //func to call randMatrix for each weight and bias matrix. 
    public static void randomizeMatrices()
    {
        w1 = randMatrix(w1);
        w2 = randMatrix(w2);
        b1 = randMatrix(b1);
        b2 = randMatrix(b2);
    }

    //func that randomizes matrices. 
    public static double[][] randMatrix(double[][] m)
    {
        Random r = new Random();
        double[][] randomized = new double[m.length][m[0].length];
        for(int i = 0; i < m.length; i++)
        {
            for(int j = 0; j < m[0].length; j++)
            {
                if(r.nextBoolean() == false) // determine if negative or positive. 
                {
                    randomized[i][j] = 0.0 - r.nextDouble();
                } else
                {
                    randomized[i][j] = r.nextDouble();
                }
            }
        }
        return randomized;
    }

    // Accuracy Test function. works similiar to training function, but just the forward pass part. 
    // a is used to determine what data set to use, state determines what graphical display we want. 
    public static void testAccuracy(int a, int state) throws Exception
    {
        List<double[]> inputList = new ArrayList<double[]>();
        String dataSet = new String();
        String in = "";
        switch (a)
        {
            case 1:
                inputList = readFile(trainingFile);
                dataSet = "TRAINING";
                break;
            case 2:
                inputList = readFile(testingFile);  
                dataSet = "TESTING";
                break;
            default:
            break;
        }

        int listsize = inputList.size();
        int[][] results = new int[10][2];
        int correct = 0, total = 0;

        //iterate through the list, and get the results. Same as training function without the randomization or backpropagation. 
        for(int i = 0; i < listsize; i++)
        {
            double[] testingItem = inputList.get(i);

            for(int j = 1; j < testingItem.length; j++)
            {
                a0[j-1][0] = testingItem[j]/255.0d;
            }

            forwardPass();

            //results
            double max = 0;
            int maxIndex = 0;
            for(int k = 0; k < outputSize; k++)
            {
                if(max < a2[k][0])
                {
                    max = a2[k][0];
                    maxIndex = k;
                }
            }

            if((int)testingItem[0] == maxIndex)
            {
                //correct
                results[(int)testingItem[0]][0]++;
                results[(int)testingItem[0]][1]++;
                correct++;
                total++;
                if(state == 1) 
                {
                    displayGraphic(testingItem, maxIndex, i); //if state is 1, we want to display all outputs. 
                    in = sc.nextLine(); // holds until enter is pressed. that way we can step through each display. although first two always display at same time. not sure why. probably a stray \n character somewhere. 
                    if(in.contains("stop")) return;
                }

            }else
            {
                //incorrect
                results[(int)testingItem[0]][1]++;
                total++;
                if(state == 1 || state == 2) 
                {
                    displayGraphic(testingItem, maxIndex, i); //if state is 1, we want to display all outputs. 
                    in = sc.nextLine(); // holds until enter is pressed. that way we can step through each display. although first two always display at same time. not sure why. probably a stray \n character somewhere. 
                    if(in.contains("stop")) return;
                }    
            }
            
        }

        //output results
        System.out.println(dataSet + " DATA ACCURACY:");
        outputResults(results, correct, total);

    }


    //load function. 
    public static void load() throws Exception
    {
        //set file, and if it doesn't exist, leave.
        File f = new File("weights.cvs");
        if(!f.exists())
        {
            System.out.println("No File to Load..");
            return;
        }
        //if exists, scan the file. 
        Scanner loadScanner = new Scanner(f);
        loadScanner.useDelimiter("(,|\r\n|[\r\n])");
        
        //read in w1 matrix, all others follow this form. wanted to try out foreach loops. 
        int i = 0;
        int j = 0;
        for (double[] ds : w1) 
        {
            for (double d : ds) 
            {
                w1[i][j] = Double.parseDouble(loadScanner.next());
                j++;
            }    
            j = 0;
            i++;    
        }

        
        i = 0;
        j = 0;
        for (double[] ds : w2) 
        {
            for (double d : ds) 
            {
                w2[i][j] = loadScanner.nextDouble();
                j++;
            }     
            j = 0;
            i++;   
        }

        i = 0;
        j = 0;
        for (double[] ds : b1) 
        {
            for (double d : ds) 
            {
                b1[i][j] = loadScanner.nextDouble();
                j++;
            }      
            j = 0;
            i++;  
        }

        i = 0;
        j = 0;
        for (double[] ds : b2) 
        {
            for (double d : ds) 
            {
                b2[i][j] = loadScanner.nextDouble();
                j++;
            }       
            j = 0;
            i++; 
        }
        loadScanner.close(); //important to close these. 

        return;
    }

    //save function. easier than I expected honestly. 
    public static void save() throws IOException
    {
        File f = new File("weights.cvs");
        if(!f.createNewFile()) //if it already exists, delete and create new. 
        {
            f.delete();
            f.createNewFile();
        }

        FileWriter fw = new FileWriter(f);

        //build each line by interating over each matrix, foreach actually really good for this. 
        String curline = "";
        for (double[] ds : w1) {
            for (double d : ds) {
                curline = curline + d+ ",";
            }
        }
        curline = curline.substring(0, curline.length()-1);//remove last character of string because its a comma. 
        curline = curline + "\n"; //then add a new line. 
        fw.write(curline); // write the current line. 

        //repeat for each matrix. 
        curline = "";
        for (double[] ds : w2) {
            for (double d : ds) {
                curline = curline + d + ",";
            }
        }
        curline = curline.substring(0, curline.length()-1);
        curline = curline + "\n";
        fw.write(curline);

        curline = "";
        for (double[] ds : b1) {
            for (double d : ds) {
                curline = curline + d + ",";
            }
        }
        curline = curline.substring(0, curline.length()-1);
        curline = curline + "\n";
        fw.write(curline);

        curline = "";
        for (double[] ds : b2) {
            for (double d : ds) {
                curline = curline + d + ",";
            }
        }
        curline = curline.substring(0, curline.length()-1);
        curline = curline + "\n";
        fw.write(curline);


        fw.close(); //make sure to close these. 



        return;
    }

    //graphical display function. handles the alignment part of the display. 
    public static void displayGraphic(double[] input, int output, int caseNum)
    {
        //print our header for each case. 
        System.out.print("Testing Case #" + caseNum + ": Correct classification = " + (int)input[0] + " Network Output = " + output);
        if(input[0] != output) System.out.println(" Incorrect.");
        else System.out.println(" Correct.");

        //this handles the alignment aspect of display. when colIndex hits 28, start a newline and reset to 0. 
        int colIndex = 0;
        for(int i = 1; i < input.length; i++) {
                computeShader(input[i]); //function call to print each ASCII Character. 
                colIndex++;
                if(colIndex == 28) 
                {
                    System.out.println();
                    colIndex = 0;
                }
        }
    }

    //no where near as complex as an actual compute shader. 
    public static void computeShader(double d)
    {
        //print depending on where d fits in the range.  " " . , ; i + #
        double empty = 25, light = 51, medLight = 76, med = 102, medDark = 150, dark = 200, black = 255;
    
            if(d < empty) System.out.print(" ");
            else if(empty < d && d < light) System.out.print(".");
            else if(light < d && d < medLight) System.out.print(",");
            else if(medLight < d && d < med) System.out.print(";");
            else if(med < d && d < medDark) System.out.print("i");
            else if(medDark < d && d < dark) System.out.print("+");
            else if(dark < d && d < black) System.out.print("#");
        
    }

    //gotta have it, good o'le main. 
    public static void main(String[] args) throws Exception
    {

        int input = -1; 
        int state = -1; //used to determine start conditions. 

        while(true)
        {
            displayMain(state);
            input = getInput(state);
            runCmd(input);
            state = 0;


        }

        
    }

}