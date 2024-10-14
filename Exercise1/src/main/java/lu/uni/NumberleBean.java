package lu.uni;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@SessionScoped
@Named("numberleBean")
public class NumberleBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(NumberleBean.class.getName());
    private List<String> numberList = new ArrayList<>();
    private String targetNumber;
    private String userGuess;
    private List<String> feedback;
    private int attempts;
    private boolean gameWon;
    private boolean gameOver;

    public NumberleBean() {
        loadNumberList();
        resetGame();
    }

    private void loadNumberList() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/numbers.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                numberList.add(line);
            }
        } catch (IOException e) {
            logger.severe("Error reading number list: " + e.getMessage());
        }
    }

    public void resetGame() {
        Random random = new Random();
        targetNumber = numberList.get(random.nextInt(numberList.size()));
        logger.info("Choosen number: " + targetNumber);
        userGuess = "";
        feedback = new ArrayList<>();
        attempts = 6;
        gameWon = false;
        gameOver = false;
    }

    public void submitGuess() {
        if (userGuess.length() != 6) {
            feedback.add("Your guess must have 6 digits!");
            return;
        }

        if (!gameOver && !gameWon) {
            int attemptsUsed = 6 - attempts;

            attempts--;

            if (userGuess.equals(targetNumber)) {
                feedback.add("Congratulations! You've guessed the correct number!");
                gameWon = true;
                gameOver = true;
                try {
                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("attemptsUsed",attemptsUsed + 1);
                    FacesContext.getCurrentInstance().getExternalContext().redirect("success.xhtml");
                } catch (IOException e) {
                    logger.severe("Redirection to success.xhtml failed: " + e.getMessage());
                }
            } else if (attempts <= 0) {
                feedback.add("Game Over! The correct number was: " + targetNumber);
                gameOver = true;
                try {
                    FacesContext.getCurrentInstance().getExternalContext().redirect("failure.xhtml");
                } catch (IOException e) {
                    logger.severe("Redirection to failure.xhtml failed: " + e.getMessage());
                }
            } else {
                feedback.add(evaluateGuess(userGuess));
            }
        }
    }

    private String evaluateGuess(String userGuess) {
        StringBuilder result = new StringBuilder();

        char[] targetChars = targetNumber.toCharArray();
        char[] guessChars = userGuess.toCharArray();

        String[] colors = new String[6];

        boolean[] targetMatched = new boolean[6];
        boolean[] guessMarked = new boolean[6];

        for (int i = 0; i < 6; i++) {
            if (guessChars[i] == targetChars[i]) {
                colors[i] = "green";
                targetMatched[i] = true;
                guessMarked[i] = true;
            }
        }

        for (int i = 0; i < 6; i++) {
            if (!guessMarked[i]) {
                boolean foundLeft = false;
                boolean foundRight = false;

                for (int j = 0; j < 6; j++) {
                    if (!targetMatched[j] && guessChars[i] == targetChars[j]) {
                        if (j < i) {
                            foundLeft = true;
                        } else if (j > i) {
                            foundRight = true;
                        }
                    }
                }

                if (foundLeft && foundRight) {
                    colors[i] = "yellow";
                } else if (foundLeft) {
                    colors[i] = "blue";
                } else if (foundRight) {
                    colors[i] = "orange";
                } else {
                    colors[i] = "red";
                }
            }
        }

        for (int i = 0; i < 6; i++) {
            result.append("<span style='color:").append(colors[i]).append(";'>")
                    .append(guessChars[i])
                    .append("</span>");
        }

        return result.toString();
    }

    public String getUserGuess() {
        return userGuess;
    }

    public void setUserGuess(String userGuess) {
        this.userGuess = userGuess;
    }

    public List<String> getFeedback() {
        return feedback;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getTargetNumber() {
        return targetNumber;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isGameWon() {
        return gameWon;
    }
}