import java.awt.*;        
import java.util.Random;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

// Swing Program Template
@SuppressWarnings("serial")
public class Sudoku extends JFrame {
   // Name-constants for game board
   public static final int GRID_SIZE = 9; // Size of board
   public static final int SUBGRID_SIZE = 3; // Size of sub grid
     
   // Name-constants for UI control
   public static final int CELL_SIZE = 60; // Cell width/height in pixels
   public static final int CANVAS_WIDTH  = CELL_SIZE * GRID_SIZE;
   public static final int CANVAS_HEIGHT = CELL_SIZE * GRID_SIZE;

   public static final Color OPEN_CELL_BGCOLOR = Color.YELLOW; // Board width/height in pixels
   public static final Color OPEN_CELL_TEXT_YES = new Color(0, 255, 0);  // RGB
   public static final Color OPEN_CELL_TEXT_NO = Color.RED;
   public static final Color CLOSED_CELL_BGCOLOR = new Color(240, 240, 240); // RGB
   public static final Color CLOSED_CELL_TEXT = Color.BLACK;
   public static final Font FONT_NUMBERS = new Font("Monospaced", Font.BOLD, 20);
   
   private final Border noborderaround = BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY);
   private final Border cross = BorderFactory.createMatteBorder(0, 0, 2, 2, CLOSED_CELL_TEXT);
   private final Border rowborder = BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, CLOSED_CELL_TEXT),BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
   private final Border colborder = BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, CLOSED_CELL_TEXT),BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
   
   private static final int easy = 10;
   private static final int intermediate = 20;
   private static final int difficult = 30;
   
   public static JMenuBar menu;
   public static JPanel board;
   public static MatteBorder border;
   public static JPanel bottomPanel;
   public static JLabel bottomLabel;
   public static JPanel centrePanel;
   public static JPanel Board;
   public static JPanel overlay;
   public static JTextField cells;
   
// The game board composes of 9x9 JTextFields, each containing String "1" to "9", or empty String
   private JTextField[][] tfCells = new JTextField[GRID_SIZE][GRID_SIZE];
   private int[][] puzzle =
      {{5, 3, 4, 6, 7, 8, 9, 1, 2},
       {6, 7, 2, 1, 9, 5, 3, 4, 8},
       {1, 9, 8, 3, 4, 2, 5, 6, 7},
       {8, 5, 9, 7, 6, 1, 4, 2, 3},
       {4, 2, 6, 8, 5, 3, 7, 9, 1},
       {7, 1, 3, 9, 2, 4, 8, 5, 6},
       {9, 6, 1, 5, 3, 7, 2, 8, 4},
       {2, 8, 7, 4, 1, 9, 6, 3, 5},
       {3, 4, 5, 2, 8, 6, 1, 7, 9}};
   // For testing, open only 2 cells.

   private boolean[][] masks = new boolean[GRID_SIZE][GRID_SIZE];
   public int counter;
   
   /*** Constructor to setup the game and the UI Components*/
   public String displayInstructions() {
	   return "A sudoku puzzle is a grid of nine by nine squares or cells, that has been subdivided into nine subgrids or \"regions\" of three by three cells.\r\n"
	   		+ "The objective of sudoku is to enter a digit from 1 through 9 in each cell, in such a way that:\r\n\r\n"
	   		+ "Each horizontal row contains each digit exactly once.\r\n"
	   		+ "Each vertical column contains each digit exactly once.\r\n"
	   		+ "Each subgrid or region (enclosed by the border) contains each digit exactly once.\r\n\r\n"
	   		+ "All the best!";
   }
   	public void playMusic(String musicLocation) {
			   try {
				   File musicPath = new File(musicLocation);
				   
					   if(musicPath.exists()) {
						   AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
						   Clip clip = AudioSystem.getClip();
						   clip.open(audioInput);
						   clip.start();
						   clip.loop(Clip.LOOP_CONTINUOUSLY);
					   }
				   }
			   catch (Exception ex) {
				   ex.printStackTrace();
			   }
			   }
  
   public int[][] shuffle() {
	    int[][] tempPuzzle = puzzle;
	    for (int i = 0; i <= 8; i += SUBGRID_SIZE) {
	      tempPuzzle = rowShuffle(i, tempPuzzle); // shuffle the 3 rows
	      tempPuzzle = transpose(tempPuzzle); // transpose to shuffle column
	      tempPuzzle = rowShuffle(i, tempPuzzle); // shuffle the 3 columns
	      tempPuzzle = transpose(tempPuzzle); // transpose to shuffle row
	     }
	    return tempPuzzle; // total of 6 shuffles, 3 rows and 3 cols
	  }
   
	  private int[][] rowShuffle(int low, int[][] tempPuzzle) {
	     Random r = new Random();
	     int change = r.nextInt(SUBGRID_SIZE);
	     int[] temp = tempPuzzle[low];
	     tempPuzzle[low] = tempPuzzle[low + change];
	     tempPuzzle[low + change] = temp;
	     return tempPuzzle;
	  }
	  
	  private int[][] transpose(int[][] tempPuzzle) {
	    int[][] transpose = new int[tempPuzzle.length][tempPuzzle.length];
	    for (int i = 0; i < tempPuzzle.length; i++) {
	      for (int j = 0; j < tempPuzzle.length; j++) {
	        transpose[i][j] = tempPuzzle[j][i];
	      }
	    }
	    return transpose;
	  }
	  
   public Sudoku() {
	   shuffle();
	   String filepath = "up.wav";
	   
	   playMusic(filepath);
		  Container cp = getContentPane();
	      cp.setLayout(new BorderLayout());
	     
		  menu = new JMenuBar(); // MENU BAR
	      InputListener listener = new InputListener(); // Allocate a common listener as the ActionEvent listener for all the JTextFields
	      JMenu fileMenu = new JMenu("File");
	      JMenu newGameMenu = new JMenu("New game");
	      fileMenu.add(newGameMenu);
	      
	      JMenuItem resetGameItem = new JMenuItem("Reset game");
	      resetGameItem.addActionListener(new ActionListener() {
	    	  public void actionPerformed(ActionEvent e) {
	    		  counter = 0;
	    		  for (int row = 0; row < GRID_SIZE; ++row) {
				    	  for (int col = 0; col < GRID_SIZE; ++col) {
				    		  if (tfCells[row][col].getBackground() == OPEN_CELL_BGCOLOR || tfCells[row][col].getBackground() == OPEN_CELL_TEXT_YES) {
				                  tfCells[row][col].setText("");     // set to empty string
				                  tfCells[row][col].setEditable(true);
				                  tfCells[row][col].setBackground(OPEN_CELL_BGCOLOR);
				                  counter++;
				                  
				               } else {
				                   tfCells[row][col].setText(puzzle[row][col] + "");
				                   tfCells[row][col].setEditable(false);
				                   tfCells[row][col].setBackground(CLOSED_CELL_BGCOLOR);
				                   tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
				                }
				    	  }
					}
	    		  bottomLabel.setText("Cells remaining: " +counter);
				}
			});
	      
	      fileMenu.add(resetGameItem);
	      
	      JMenuItem exitItem = new JMenuItem("Exit");
	      exitItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
	      fileMenu.add(exitItem);
	      menu.add(fileMenu);
	      
	      JMenu optionsMenu = new JMenu("Options");
	      JMenu helpMenu = new JMenu("Help");
	      JMenuItem instructions = new JMenuItem("Instructions");
	      helpMenu.add(instructions);
	      instructions.addActionListener(new ActionListener() {
	    	  @Override
	    	  public void actionPerformed(ActionEvent e) {
	    		  JOptionPane.showMessageDialog(null,displayInstructions());
	    		  }

			});
	      
	      JMenuItem reveal = new JMenuItem("Reveal one cell");
	      helpMenu.add(reveal);
	      reveal.addActionListener(new ActionListener() {
	   		  
	          @Override
	    	  public void actionPerformed(ActionEvent e) {
	        	  int count = 1;
	        	  while (count>0) {
	    	    	  for (int row = 0; row < GRID_SIZE; ++row) {
	    	    		  for (int col = 0; col < GRID_SIZE; ++col) {
	    		                 if(Math.random() >= 0.99 && masks[row][col]) {
	    		                	 masks[row][col] = false;
	    		                	 tfCells[row][col].setText(puzzle[row][col] + "");
			                         tfCells[row][col].setEditable(false);
			                         tfCells[row][col].setBackground(OPEN_CELL_TEXT_YES);
			                         tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
			                         count--;
			                         counter--;
	    		                 }
	    	    		  }
	    	    	  }
	        	  }
	        	  bottomLabel.setText("Cells remaining: " +counter);
	        	  
	        	  if (counter == 0) {
	        		  JOptionPane.showMessageDialog(null, "Congratulations");
	        	  }
	          }
	          });
	      
	      JMenuItem solve = new JMenuItem("Solve");
	      helpMenu.add(solve);
	      solve.addActionListener(new ActionListener() {
	          @Override
	    	  public void actionPerformed(ActionEvent e) {
	        	  for (int row = 0; row < GRID_SIZE; ++row) {
    	    		  for (int col = 0; col < GRID_SIZE; ++col) {
    		                 if(masks[row][col]) {
    		                	 masks[row][col] = false;
    		                	 tfCells[row][col].setText(puzzle[row][col] + "");
		                         tfCells[row][col].setEditable(false);
		                         tfCells[row][col].setBackground(OPEN_CELL_TEXT_YES);
		                         tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
    		                 }
    	    		  }
	        	  }
	        	  counter = 0;
	        	  bottomLabel.setText("Cells remaining: " +counter);
	        	  JOptionPane.showMessageDialog(null, "Puzzle is solved!");
	          }
	      });
	      
	      JMenuItem Easy = new JMenuItem("Easy");
	      newGameMenu.add(Easy);
	   	  Easy.addActionListener(new ActionListener() {
	          @Override
	    	  public void actionPerformed(ActionEvent e) {
	        	  shuffle();
	          counter = 0;
	    	  int count = easy;
	    	  counter = easy;
	    	  bottomLabel.setText("Cells remaining: " +counter);
	    	  for (int row = 0; row < GRID_SIZE; ++row) {
	              for (int col = 0; col < GRID_SIZE; ++col) {
	                          masks[row][col] = false;
	                  }
	              }       
	    	  
	    	  while (count>0) {
	    	  for (int row = 0; row < GRID_SIZE; ++row) {
	    		  for (int col = 0; col < GRID_SIZE; ++col) {
		                 if(Math.random() >= 0.99 && !masks[row][col]) {
		                        if (count > 0 ) {
		                          masks[row][col] = true;
		                          count--;
		                        }
		                      }
		                  
		                 if (masks[row][col] == true) {
		                 	  counter++;
		                      tfCells[row][col].setText("");     // set to empty string
		                      tfCells[row][col].setEditable(true);
		                      tfCells[row][col].setBackground(OPEN_CELL_BGCOLOR);
		                      tfCells[row][col].addActionListener(listener);
		                 } else {
		                      tfCells[row][col].setText(puzzle[row][col] + "");
		                      tfCells[row][col].setEditable(false);
		                      tfCells[row][col].setBackground(CLOSED_CELL_BGCOLOR);
		                      tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
		                        }
		                       
		                 // Beautify all the cells
		                 tfCells[row][col].setHorizontalAlignment(JTextField.CENTER);
		                 tfCells[row][col].setFont(FONT_NUMBERS);
	    		  }
		          }
	    	  	}
		      }
		      });

	      JMenuItem Intermediate = new JMenuItem("Intermediate");
	      newGameMenu.add(Intermediate);
	      Intermediate.addActionListener(new ActionListener() {
	          @Override
	    	  public void actionPerformed(ActionEvent e) {
	        	  shuffle();
	          counter = 0;
	         
	    	  int count = intermediate;
	    	  counter = intermediate;
	    	  bottomLabel.setText("Cells remaining: " +counter);
	    	  for (int row = 0; row < GRID_SIZE; ++row) {
	              for (int col = 0; col < GRID_SIZE; ++col) {
	                          masks[row][col] = false;
	                  }
	              }        
	    	  
	    	  while (count>0) {
	    	  for (int row = 0; row < GRID_SIZE; ++row) {
	    		  for (int col = 0; col < GRID_SIZE; ++col) {
		                 if(Math.random() >= 0.99 && !masks[row][col]) {
		                        if (count > 0 ) {
		                          masks[row][col] = true;
		                          count--;
		                        }
		                      }
		                      
		                if (masks[row][col] == true) {
		                        tfCells[row][col].setText("");     // set to empty string
		                        tfCells[row][col].setEditable(true);
		                        tfCells[row][col].setBackground(OPEN_CELL_BGCOLOR);
		                        tfCells[row][col].addActionListener(listener);
		                          
		                } else {
		                        tfCells[row][col].setText(puzzle[row][col] + "");
		                        tfCells[row][col].setEditable(false);
		                        tfCells[row][col].setBackground(CLOSED_CELL_BGCOLOR);
		                        tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
		                        }
		                       
		                // Beautify all the cells
		                tfCells[row][col].setHorizontalAlignment(JTextField.CENTER);
		                tfCells[row][col].setFont(FONT_NUMBERS);
		        }
		        }
		      }
		      }
		      });
	      
	      JMenuItem Difficult = new JMenuItem("Difficult");
	      newGameMenu.add(Difficult);
	      Difficult.addActionListener(new ActionListener() {
	          @Override
	    	  public void actionPerformed(ActionEvent e) {	        	  
	        	  shuffle();
	        	  counter = 0; 	  
	        	  counter = difficult;
	        	  bottomLabel.setText("Cells remaining: " +counter);
	        	  
	    	  int count = difficult;
	    	  for (int row = 0; row < GRID_SIZE; ++row) {
	              for (int col = 0; col < GRID_SIZE; ++col) {
	                          masks[row][col] = false;
	              }
	          }
	    	  
	    	  while (count>0) {
	    	  for (int row = 0; row < GRID_SIZE; ++row) {
	    		  for (int col = 0; col < GRID_SIZE; ++col) {
		                 if(Math.random() >= 0.90 && !masks[row][col]) {
		                        if (count > 0) {
		                          masks[row][col] = true;
		                          count--;
		                        }
		                  }
		                 if (masks[row][col] == true) {
		                        tfCells[row][col].setText("");     // set to empty string
		                        tfCells[row][col].setEditable(true);
		                        tfCells[row][col].setBackground(OPEN_CELL_BGCOLOR);
		                        tfCells[row][col].addActionListener(listener);
		                 } else {
		                        tfCells[row][col].setText(puzzle[row][col] + "");
		                        tfCells[row][col].setEditable(false);
		                        tfCells[row][col].setBackground(CLOSED_CELL_BGCOLOR);
		                        tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
		                 }
		                       
		                 // Beautify all the cells
		                 		tfCells[row][col].setHorizontalAlignment(JTextField.CENTER);
		                 		tfCells[row][col].setFont(FONT_NUMBERS);
		          }
	    	  	}
		      }
		      }
		      });
	      newGameMenu.add(Difficult);
	      
	      menu.add(optionsMenu);
	      menu.add(helpMenu);
	      setJMenuBar(menu);
	      
		  int count = easy;
		  counter = easy;
		  while (count>0) {
		  for (int row = 0; row < GRID_SIZE; ++row) {
			  for (int col = 0; col < GRID_SIZE; ++col) {
	                 if(Math.random() >= 0.99 && !masks[row][col]) {
	                        if (count > 0) {
	                          masks[row][col] = true;
	                          count--;
	                        }
	                 }
			  }
		  }
		  }
	      
	      // Construct 9x9 JTextFields and add to the content-pane
		  centrePanel = new JPanel();
		  centrePanel.setLayout(new GridLayout(GRID_SIZE,GRID_SIZE));  
		  
	      for (int row = 0; row < GRID_SIZE; ++row) {
	    	  for (int col = 0; col < GRID_SIZE; ++col) {
	            tfCells[row][col] = new JTextField(); // Allocate element of array
	            cp.add(tfCells[row][col]);            // ContentPane adds JTextField
	       
	            if (masks[row][col]) {
	               tfCells[row][col].setText("");     // set to empty string
	               tfCells[row][col].setEditable(true);
	               tfCells[row][col].setBackground(OPEN_CELL_BGCOLOR);
	               tfCells[row][col].addActionListener(listener);
	               
	            } else {
	                tfCells[row][col].setText(puzzle[row][col] + "");
	                tfCells[row][col].setEditable(false);
	                tfCells[row][col].setBackground(CLOSED_CELL_BGCOLOR);
	                tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
	             }
	            
	            boolean rowframe = (row+1)%3 == 0;
	            boolean columnframe = (col+1)%3 == 0;
	      	  	if(rowframe && columnframe) {
	      	  		tfCells[row][col].setBorder(cross);
	      	  	} else if (rowframe) {
	      	  		tfCells[row][col].setBorder(rowborder);
	      	  	} else if (columnframe) {
	      	  		tfCells[row][col].setBorder(colborder);
	      	  	} else {
	      	  		tfCells[row][col].setBorder(noborderaround);
	      	  	}
	      		// Beautify all the cells
	            tfCells[row][col].setHorizontalAlignment(JTextField.CENTER);
	            tfCells[row][col].setFont(FONT_NUMBERS);
	            
	            centrePanel.add(tfCells[row][col]);
	         }
	      }
	      cp.add(centrePanel, BorderLayout.CENTER);
		  
	    //BOTTOM PANEL     
	      bottomPanel = new JPanel(new BorderLayout() );
	      bottomPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
	      bottomPanel.setPreferredSize(new Dimension(GRID_SIZE, GRID_SIZE*3));
	      bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
	      bottomLabel = new JLabel(" Cells remaining: " + counter);
	      bottomPanel.add(bottomLabel, BorderLayout.LINE_START);	     
	      cp.add(bottomPanel, BorderLayout.PAGE_END);
	      cp.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT)); // Set the size of the content-pane and pack all the components under this container
	      pack();
	 
	      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Handle window closing
	      setTitle("Sudoku");
	      setVisible(true);
	   }
   


/** The entry main() entry method */
   public static void main(String[] args) {
   // Run GUI codes in the Event-Dispatching thread for thread safety
   SwingUtilities.invokeLater(new Runnable() {
      public void run() {
         new Sudoku(); //Let the constructor do the job
      }
   });
}

// Inner class to be used as ActionEvent listener for ALL JTextFields
   private class InputListener implements ActionListener {

   @Override
   public void actionPerformed(ActionEvent e) {
      // All the 9*9 JTextFileds invoke this handler. We need to determine
      // which JTextField (which row and column) is the source for this invocation.
     
      int rowSelected = -1;
      int colSelected = -1;

      // Get the source object that fired the event
      JTextField source = (JTextField)e.getSource();
      // Scan JTextFileds for all rows and columns, and match with the source object
      boolean found = false;
      for (int row = 0; row < GRID_SIZE && !found; ++row) {
         for (int col = 0; col < GRID_SIZE && !found; ++col) {

            if (tfCells[row][col] == source) {
               rowSelected = row;
               colSelected = col;
               found = true;  // break the inner/outer loops          
         }
      }
      }

     tfCells[rowSelected][colSelected].getText();
     
     int input = -1;
    input = Integer.parseInt(source.getText());
    
    // Compare the input number with the number in the puzzle. If they
    // are the same, display in green; otherwise, display in red.
    
    for (int row = 0; row< GRID_SIZE ; row++) {
		for (int col = 0; col<GRID_SIZE ; col++) {
			
			if(tfCells[row][col].getBackground() == OPEN_CELL_TEXT_NO){
				tfCells[row][col].setBackground(CLOSED_CELL_BGCOLOR);
			}
			if (masks[row][colSelected] == false) {
				if (puzzle[row][colSelected] == input) {
					if(tfCells[row][colSelected].getBackground() == OPEN_CELL_TEXT_YES) {
						tfCells[row][colSelected].setForeground(OPEN_CELL_TEXT_NO);
					} else{
						tfCells[row][colSelected].setBackground(OPEN_CELL_TEXT_NO);
					}
				}
			}
			if (masks[rowSelected][col] == false) {
				if (puzzle[rowSelected][col] == input) {
					if(tfCells[rowSelected][col].getBackground() == OPEN_CELL_TEXT_YES) {
						tfCells[rowSelected][col].setForeground(OPEN_CELL_TEXT_NO);
					} else{
						tfCells[rowSelected][col].setBackground(OPEN_CELL_TEXT_NO);
					}
			}
		}
	}
	}
	
	int subgridFirstRowNum = rowSelected / SUBGRID_SIZE * SUBGRID_SIZE; // 0, 3 or 6
	int subgridFirstColNum = colSelected / SUBGRID_SIZE * SUBGRID_SIZE; // 0, 3 or 6
	
	if (input == puzzle[rowSelected][colSelected]) { //IF CORRECT
		masks[rowSelected][colSelected] = false;

		// Repaint highlighted cells in sub-grid
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				if (!masks[row][col]) {
					tfCells[rowSelected][colSelected].setEditable(false);
					tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
					tfCells[rowSelected][colSelected].setBackground(OPEN_CELL_TEXT_YES);
				} 
			}
		}
	} else {	
		tfCells[rowSelected][colSelected].setForeground(OPEN_CELL_TEXT_NO); //incorrect
		// Highlight conflicting number in sub-grid
		for (int row = subgridFirstRowNum; row < subgridFirstRowNum+ SUBGRID_SIZE; row++) {
			for (int col = subgridFirstColNum; col < subgridFirstColNum+ SUBGRID_SIZE; col++) {
				if (puzzle[row][col] == input && !masks[row][col]) {
					if(tfCells[row][col].getBackground() == OPEN_CELL_TEXT_YES) {
						tfCells[row][col].setForeground(OPEN_CELL_TEXT_NO);
					} else {
						tfCells[row][col].setBackground(OPEN_CELL_TEXT_NO);
					}
				}
			}
		}
	}
	
	int counter = 0;
	for (int row = 0; row < GRID_SIZE; row++) {
		for (int col = 0; col < GRID_SIZE; col++) {
			if(masks[row][col] == true) {
				counter++;
			}
		}
	}
	bottomLabel.setText("Cells remaining: " +counter);
	
	if (counter == 0) { 
		JOptionPane.showMessageDialog(null, "Congratulation!"); // once completed
		
	}
   }
  }
}