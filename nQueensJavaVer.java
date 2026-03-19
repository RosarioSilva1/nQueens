import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

public class nQueensJavaVer extends JFrame {

	private static final long serialVersionUID = 1L;
	private JSpinner columnInput;
    private JTextField generationCapInput;
    private JCheckBox capCheckbox;
    private JTextArea outputArea;
    private ChessBoardPanel boardPanel;
    private JLabel genLabel;
    private JLabel fitnessLabel;
    private int generation = 0; 
    private String timeTaken;
    private int x = 1;

    public static final int popSize = 100;

    public QueenVisualizerGUI() {
        setTitle("N-Queens");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();

        topPanel.add(new JLabel("Columns (4-20):"));
        columnInput = new JSpinner(new SpinnerNumberModel(7, 4, 20, 1));
        topPanel.add(columnInput);

        capCheckbox = new JCheckBox("Gen Cap");
        topPanel.add(capCheckbox);

        generationCapInput = new JTextField(5);
        generationCapInput.setEnabled(false);
        topPanel.add(generationCapInput);

        capCheckbox.addActionListener(e ->
                generationCapInput.setEnabled(capCheckbox.isSelected()));

        JButton runButton = new JButton("Run");
        topPanel.add(runButton);
        
        JButton pauseButton = new JButton("Pause");
        JButton resumeButton = new JButton("Resume");

        genLabel = new JLabel("Generation: 0");
        fitnessLabel = new JLabel("Fitness: 0");

        topPanel.add(genLabel);
        topPanel.add(fitnessLabel);
        
        topPanel.add(pauseButton);
        topPanel.add(resumeButton);

        add(topPanel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Algorithm Output"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        boardPanel = new ChessBoardPanel();

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                boardPanel
        );
        splitPane.setDividerLocation(300);

        add(splitPane, BorderLayout.CENTER);

        runButton.addActionListener((ActionEvent e) -> runAlgorithm());

        pauseButton.addActionListener((ActionEvent e) -> x = 0);
        resumeButton.addActionListener((ActionEvent e) -> x = 1);

        setVisible(true);
    }

    private void runAlgorithm() {
        new Thread(() -> {

            outputArea.setText("");

            int numColumns;
            int generationCap = 0;

            try {
                numColumns = (int) columnInput.getValue();
                if (numColumns < 4 || numColumns > 20) return;

                if (capCheckbox.isSelected())
                    generationCap = Integer.parseInt(generationCapInput.getText());

            } catch (Exception e) {
                outputArea.append("Invalid input\n");
                return;
            }

            Random random = new Random();
            int [][] configurations = new int[popSize][numColumns + 1];
            int optimalFitnessFunction = 3 * numColumns;
            generation = 1;
            
            for(int i = 0; i < popSize; i++) {
                for(int j = 0; j < configurations[i].length -1; j++) 
                    configurations[i][j] = random.nextInt(1, numColumns + 1);

                configurations[i][configurations[i].length - 1] = calcFitnessFunction(configurations[i]);
            }

            boolean solFound = false;
            long startTime = System.currentTimeMillis();
            while(!solFound) {
                int maxFitness = computeMaxFitness(configurations);

                DecimalFormat numberFormat = new DecimalFormat("#.00");

                for(int i = 0; i < popSize; i++) 
                    if(configurations[i][configurations[i].length - 1] == optimalFitnessFunction) {
                        double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;
                        
                        timeTaken = elapsedTime + "s";
                        if(elapsedTime > 60) 
                            timeTaken = (int)(elapsedTime/60) + "m " + numberFormat.format(elapsedTime%60) + "s";
                        
                        SwingUtilities.invokeLater(() -> {
                            outputArea.append("Gen " + generation +
                                    " | Fitness: " + maxFitness + "\n\n"
                                    + "Time taken: " + timeTaken);
                        });
                        
                        genLabel.setText("Generation: " + generation);
                        fitnessLabel.setText("Fitness: " + maxFitness);
                        
                        System.out.println();
                        int[] bestSolution = configurations[i];
                        SwingUtilities.invokeLater(() ->
                                boardPanel.setBoard(bestSolution, true));

                        solFound = true;
                        break;
                    }

                if(solFound)
                    break;
                
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("Gen " + generation +
                            " | Fitness: " + maxFitness + "\n");
                });
                
                genLabel.setText("Generation: " + generation);
                fitnessLabel.setText("Fitness: " + maxFitness);

                int[] bestSolution = configurations[maxFitness];
                
                SwingUtilities.invokeLater(() ->
                        boardPanel.setBoard(bestSolution, false));

                if (maxFitness == optimalFitnessFunction) break;

                if (generationCap != 0 && generation >= generationCap) break;

                configurations = createChildren(configurations);
                generation++;
                
                while(x == 0) {
                	try { Thread.sleep(200); } catch (Exception ignored) {}
                }

                try { Thread.sleep(10); } catch (Exception ignored) {}
            }

        }).start();
    }

    class ChessBoardPanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int[] board;
        private boolean isSolved;

        public void setBoard(int[] board, boolean isSolved) {
            this.board = board;
            this.isSolved = isSolved;
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (board == null) return;

            int n = board.length - 1;
            int size = Math.min(getWidth(), getHeight()) / n;

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {

                    if ((i + j) % 2 == 0)
                        g.setColor(Color.WHITE);
                    else
                        g.setColor(Color.GRAY);

                    g.fillRect(j * size, i * size, size, size);

                    if (board[i] == j + 1) {
                    	if(isSolved)
                    		g.setColor(Color.GREEN);
                    	else
                    		g.setColor(Color.RED);
                   
                        g.fillOval(j * size + size/4, i * size + size/4,
                                size/2, size/2);
                    }
                }
            }
        }
    }

    private static int computeMaxFitness(int[][] configurations) {
        int max = configurations[0][configurations[0].length - 1];

        for(int i = 0; i < popSize; i++)
            if(configurations[i][configurations[i].length - 1] > max)
                max = configurations[i][configurations[i].length - 1];        

        return max;
    }
    
    private static int[][] createChildren(int[][] configurations) {
        double totalFitness = 0;

        for(int i = 0; i < popSize; i++)
            totalFitness += configurations[i][configurations[i].length - 1];

        int[] specificFitness = new int[popSize];

        for(int i = 0; i < specificFitness.length; i++) {
            specificFitness[i] = (int) ((configurations[i][configurations[i].length - 1]/totalFitness) * 100);
        }

        for(int i = 1; i < specificFitness.length; i++) {
            specificFitness[i] += specificFitness[i-1];
        }

        int[][] tempConf = new int[popSize][configurations[0].length];

        int tempConfSize = 0;

        while(tempConfSize < popSize) {
            int [] parent1 = configurations[getParent(specificFitness)], parent2 = configurations[getParent(specificFitness)];

            while(Arrays.equals(parent1, parent2))
                parent2 = configurations[getParent(specificFitness)];

            int mid = parent1.length / 2 + 1;

            for(int i = 0; i < mid; i++) {
                tempConf[tempConfSize][i] = parent1[i];
                tempConf[tempConfSize + 1][i] = parent2[i];
            }

            for(int i = mid; i < parent1.length; i++) {
                tempConf[tempConfSize][i] = parent2[i];
                tempConf[tempConfSize + 1][i] = parent1[i];
            }

            Random random = new Random();

            int randomNum = Math.abs(random.nextInt(0, 101) - random.nextInt(0, 101));

            if(randomNum >= 45 && randomNum < 56)
                tempConf[tempConfSize] = mutate(tempConf[tempConfSize]);

            if(randomNum >= 54 && randomNum < 65)
                tempConf[tempConfSize + 1] = mutate(tempConf[tempConfSize + 1]);

            tempConfSize += 2;
        }

        return tempConf;
    }

    private static int[] mutate(int[] is) {
        int[] isMutated = new int[is.length];

        for(int i = 0; i < is.length; i++)
            isMutated[i] = is[i];

        int duplicateIndex = 0;

        for(int i = duplicateIndex + 1; i < is.length; i++) 
            if(is[i] == is[duplicateIndex]) {
                i = duplicateIndex;
                break;
            }

        Random random = new Random();

        if(duplicateIndex != 0)
            isMutated[duplicateIndex] = random.nextInt(1, is.length);
        else
            isMutated[random.nextInt(0, is.length)] = random.nextInt(1, is.length);

        isMutated[is.length - 1] = calcFitnessFunction(isMutated);

        return isMutated;
    }

    private static int getParent(int[] specificFitness) {
        Random random = new Random();

        int randomInt = random.nextInt(0, specificFitness[specificFitness.length - 1] + 1);

        for(int k = 0; k < specificFitness.length; k++) {
            int min = (k == 0) ? 0 : specificFitness[k - 1] + 1;
            if(randomInt >= min && randomInt <= specificFitness[k] + 1) 
                return k;
        }

        return 0;
    }
 
    private static int calcFitnessFunction(int[] conf) {
    	int fitnessNum = 3 * (conf.length - 1);

        for(int i = 0; i < conf.length - 1; i++) {
            for(int j = i + 1; j < conf.length - 1; j++) {
                if(conf[i] == conf[j])
                    fitnessNum -= 2;

                if(Math.abs(i - j) == Math.abs(conf[i] - conf[j]))
                    fitnessNum -= 2;
            }
        }
        return fitnessNum;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(nQueensJavaVer::new);
    }
}
