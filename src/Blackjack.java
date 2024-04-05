//This is a tutorial guided Blackjack Progeam
//This file, Blackjack.java, is where all of the games code will be
//blackjack rules https://bicyclecards.com/how-to-play/blackjack/

//We will import several things to help us make the game
import java.awt.*;          //java.awt is used for the GUI; graphic user interface
import java.awt.event.*;        //this makes our program more than just a command line
import java.util.ArrayList; //ArrayList will keep track of the deck, player hand, and dealer's hand 
import java.util.Random;    //Random will help us shuffle the cards
import javax.swing.*;       //Swing will be used to access a variety of useful things

//the bulk of our code will be inside the class Blackjack
public class Blackjack {
    //For convienece, we will create a card class
    //all card attributes and methods will be here
    private class Card {
        //this are attributes associated with each card
        String value; // 2 to 10 & AJQK
        String type;  // card suit

        //constructor to create each card 
        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }
        
        //toString() override
        //this make the printed details of a card easier to understand
        public String toString() {
            //When displaying cards to the game screen, we want this to match the png card names to make things easier
            return value + "-" + type;
        }

        //this method returns the numerical value given by each card
        public int getValue() {
            //only A, J, Q, K have non-face value points associated with them
            if ("AJQK".contains(value)) {
                //if the card is an Ace, it is 11 points
                //tho it can be changed later to 1 if needed
                if (value == "A") {
                    return 11;
                }
                //the rest of the face cards have a value of 10
                return 10;
            } 
            //the rest of the cards have their value given
            //take the string form of the number and turn it into an int that can be returned for stuff like calculating the sum 
            return Integer.parseInt(value); //2 to 10
        }

        //Aces are special in blackjack
        //they can be 11 or 1
        //it will be useful to idenify which cards are Aces
        public boolean isAce() {
            return value == "A";
        }

        //this gives us a convienent way to write the path to each card
        public String getImagePath() {
            return "./cards/" + toString() + ".png";
        }
    }

    //Now we will declare some general variables that we will be using throughout the program
    //Deck
    ArrayList<Card> deck; //creates an empty ArrayList that will hold all cards in the deck
    Random random = new Random(); //we need to set up random, so we can use it to shuffle our cards

    //Dealer
    Card hiddenCard; //the dealer starts with one face down card...
    ArrayList<Card> dealerHand; //and one face up card...
    int dealerSum; //we will need the sum of the dealer's cards later in the game to decide how the dealer will act
    //like the player, the dealer can also choose if an Ace is 11 or 1
    //it will be helpful to keep track of how many of them there are, so the dealer can decide the value of the Ace
    int dealerAceCount;

    //Player
    ArrayList<Card> playerHand; //the player's hand will be an arrayList as the number of cards change
    int playerSum; //we also need their sum to tell who wins
    int playerAceCount; //we need to know how many aces to calculate sum and if the player can draw

    //Window
    int boardWidth = 600; //width in pixels
    int boardHeight = boardWidth; //height is the same as width

    int cardWidth = 110; //ratio should be 1/1.4
    int cardHeight = 154;

    JFrame frame = new JFrame("Blackjack ;)");
    //this will be used for drawing 
    JPanel gamePanel = new JPanel() { 
        //to draw in the JPanel we need to overide a method
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
            //draw hidden card
            Image hiddenCardImg = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
            if (!stayButton.isEnabled()) {
                hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
            }
            g.drawImage(hiddenCardImg, 20, 20, cardWidth, cardHeight, null);
            
            //draw dealer's hand
            for(int i = 0; i < dealerHand.size(); i++) {
                Card card = dealerHand.get(i);
                Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                g.drawImage(cardImg, cardWidth + 25 + (cardWidth + 5) * i, 20, cardWidth, cardHeight, null);
            }

            //draw player's hand
            for(int i = 0; i < playerHand.size(); i++) {
                Card card = playerHand.get(i);
                Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                g.drawImage(cardImg, (cardWidth + 5)*i + 20, 320, cardWidth, cardHeight, null);
            }

            if (!stayButton.isEnabled()) {
                dealerSum = reduceDealerAce();
                playerSum = reducePlayerAce();
                System.out.println("STAY:");
                System.out.println(dealerSum);
                System.out.println(playerSum);

                String message = "";
                if (playerSum > 21) {
                    message = "You Lose.";
                } else if (dealerSum > 21) {
                    message = "You Win!";
                } else if (playerSum == dealerSum) {
                    message = "TIE";
                } else if (playerSum > dealerSum) {
                    message  = "You Win!";
                } else if (playerSum < dealerSum) {
                    message  = "You Lose";
                }
                    
                g.setFont(new Font("Arial", Font.PLAIN, 30));
                g.setColor(Color.white);
                g.drawString(message, 220, 250);
                
            }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    
    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("Hit");   
    JButton stayButton = new JButton("Stay");
    JButton nextGameButton = new JButton("Next Game");

    //this, as you might expect, starts the game
    Blackjack() {
        startGame();

        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //when the player clicks x on the window, it terminates the program
    
        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color (53, 101, 77));
        frame.add(gamePanel);

        hitButton.setFocusable(false);
        buttonPanel.add(hitButton);
        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);
        nextGameButton.setFocusable(false);
        buttonPanel.add(nextGameButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Card card = deck.remove(deck.size() - 1);
                playerSum += card.getValue();
                playerAceCount += card.isAce() ? 1 : 0;
                playerHand.add(card);
                if (reducePlayerAce() > 21) { //A + 2 + J --> 1 + 2 + J rather than 11
                    hitButton.setEnabled(false);
                } 

                gamePanel.repaint(); //this will call our paint component method
            }
        });

          
        stayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hitButton.setEnabled(false);
                stayButton.setEnabled(false);

                //rules say the dealer must draw untill they have a sum of 17 or greater
                while (dealerSum < 17) {
                    Card card = deck.remove(deck.size() - 1);
                    dealerHand.add(card);
                    dealerSum += card.getValue();
                    dealerAceCount += card.isAce() ? 1 : 0;
                }
                gamePanel.repaint();
            }     
        });

        nextGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hitButton.setEnabled(true);
                stayButton.setEnabled(true);

                startGame();
                gamePanel.repaint();
            }
        });
        
        gamePanel.repaint(); //will call it within the constrcutor to update the game panel
  
    }

    //startGame holds the steps to begin a game of Blackjack
    public void startGame() {
        //we call buildDeck to set up a deck of cards
        buildDeck();
        //we call shuffleDeck to shuffle the deck of cards
        shuffleDeck();

        //Dealer
        //we make a new empty hand, sum, and Ace conunt for the dealer
        dealerHand = new ArrayList<Card>();
        dealerSum = 0;
        dealerAceCount = 0;

        //Face-down Card
        //assigns a card that will begin hidden
        hiddenCard = deck.remove (deck.size() - 1); //removes card at last index; returns that card to hiddenCard
        dealerSum += hiddenCard.getValue(); //add the card points to the sum
        dealerAceCount += hiddenCard.isAce() ? 1 : 0; //checks if tha card is an ace and adds to the count accordingly
        //Face-up Card
        Card card = deck.remove(deck.size() - 1);
        dealerHand.add(card);
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0; //read as if True, 1; otherwise, it is 0
        
        //prints out some details on the dealer's cards for our information
        System.out.println("DEALER");
        System.out.println("Hidden Card: " + hiddenCard);
        System.out.println("Dealer's Hand: " + dealerHand);
        System.out.println("Sum of Dealer's Cards: " + dealerSum);
        System.out.println("Number on Aces in Dealer's Hand: " + dealerAceCount);
        
        //Player
        //we will reset the player
        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;

        //we give the player two cards and collecting some information about the cards
        //notice: we are reusing the card variable from the dealer
        for (int i  = 0; i < 2; i++) {
            card = deck.remove(deck.size() - 1);
            playerHand.add(card);
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
        }
        
        //prints out some details on the player's cards for our information
        System.out.println("Player:");
        System.out.println("Player Hand: " + playerHand);
        System.out.println("Sum of Player's Cards: " + playerSum);
        System.out.println("Number on Aces in Player's Hand: " + playerAceCount);
    }

    //this will build our new deck of cards
    public void buildDeck() {
        //this ArrayList will hold each card in the deck
        deck = new ArrayList<Card>();

        //we start by making arrays that we can loop through to create each card
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H","S"};

        //we will use loops to add each unique card to the deck
        for (int i = 0; i < types.length; i++) {
            for (int j = 0; j < values.length; j++) {
                Card card = new Card(values[j], types[i]);
                deck.add(card);
            }
        }

        //we will print out the deck to see what it looks like
        System.out.println("BUILD DECK:");
        System.out.println(deck);
    }

    //this will shuffle the whole deck
    public void shuffleDeck() {
        //we will traverse through each spot in the deck
        for (int i = 0; i < deck.size(); i++) {
            //we will be holding on to the card from our current spot
            Card currCard = deck.get(i);

            //here, we will get a completely random card
            int j = random.nextInt(deck.size()); //gives a random integer up to the size of the deck (0 to 51) and assigns it to j
            Card randomCard = deck.get(j); //holds onto a random card from the deck

            //here is where we will swap the cards and start shuffling the deck
            deck.set(i, randomCard); //replaces the current spot with that random card from earlier
            deck.set(j, currCard); //the card that was previously at the current spot, but was replaced is put where the random card was a originally
        }

        //we will print out the new shuffled verision of the deck
        System.out.println("After Shuffle ");
        System.out.println(deck);
    }
   
    //if we have more than 21, aces will be considered 1 untill less than 21
    public int reducePlayerAce() {
        while (playerSum > 21 && playerAceCount > 0) {
            playerSum -= 10;
            playerAceCount -= 1;
        }
        return playerSum;
    }

    public int reduceDealerAce() {
        while (dealerSum > 21 && dealerAceCount > 0) {
            dealerSum -= 10;
            dealerAceCount -= 1;
        }
        return dealerSum;
    }
}

