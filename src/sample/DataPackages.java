package sample;

import java.util.ArrayList;
import java.util.List;

public class DataPackages implements java.io.Serializable{

    public class Player implements java.io.Serializable{
        private int ID;
        private String name;
        private int score;

        private boolean isJoining = false;
        private boolean isLeaving = false;
        private boolean isChecking = false;

        private boolean isSendingScore = false;
        private boolean hasSentAnswer = false;


        Player(String name){
            this.name = name;
        }
        Player(){

        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isJoining() {
            return isJoining;
        }

        public void setJoining(boolean joining) {
            isJoining = joining;
        }

        public boolean isLeaving() {
            return isLeaving;
        }

        public void setLeaving(boolean leaving) {
            isLeaving = leaving;
        }


        public boolean isChecking() {
            return isChecking;
        }

        public void setChecking(boolean checking) {
            isChecking = checking;
        }


        public int getID() {
            return ID;
        }

        public void setID(int ID) {
            this.ID = ID;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public boolean isSendingScore() {
            return isSendingScore;
        }

        public void setSendingScore(boolean sendingScore) {
            isSendingScore = sendingScore;
        }

        public boolean isHasSentAnswer() {
            return hasSentAnswer;
        }

        public void setHasSentAnswer(boolean hasSentAnswer) {
            this.hasSentAnswer = hasSentAnswer;
        }
    }

    public class PlayerList implements java.io.Serializable{
        List<Player> players = new ArrayList<>();

        public List<Player> getPlayers() {
            return players;
        }

        public void setPlayers(List<Player> players) {
            this.players = players;
        }


    }


    public class Message{

    }

    public class GameCommand implements java.io.Serializable{
        public boolean isEntering() {
            return isEntering;
        }

        public void setEntering(boolean entering) {
            isEntering = entering;
        }

        public boolean isExiting() {
            return isExiting;
        }

        public void setExiting(boolean exiting) {
            isExiting = exiting;
        }

        public boolean isStarting() {
            return isStarting;
        }

        public void setStarting(boolean starting) {
            isStarting = starting;
        }

        public boolean isEnding() {
            return isEnding;
        }

        public void setEnding(boolean ending) {
            isEnding = ending;
        }

        public boolean isHasEveryoneSentAnswer() {
            return hasEveryoneSentAnswer;
        }

        public void setHasEveryoneSentAnswer(boolean hasEveryoneSentAnswer) {
            this.hasEveryoneSentAnswer = hasEveryoneSentAnswer;
        }

        public boolean isNextLevel() {
            return NextLevel;
        }

        public void setNextLevel(boolean nextLevel) {
            NextLevel = nextLevel;
        }

        private boolean isEntering = false;
        private boolean isExiting = false;

        private boolean isStarting = false;
        private boolean isEnding = false;

        private boolean hasEveryoneSentAnswer = false;
        private boolean NextLevel = false;



    }

    public class MathQuestion implements  java.io.Serializable{

        private String question;
        private double answer;
        private double point;
        private int level;

        private boolean isSendingQuestion = false;
        private boolean isSendingAnswer = false;

        MathQuestion(){

        }


        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public double getAnswer() {
            return answer;
        }

        public void setAnswer(double answer) {
            this.answer = answer;
        }

        public double getPoint() {
            return point;
        }

        public void setPoint(double point) {
            this.point = point;
        }

        public boolean isSendingQuestion() {
            return isSendingQuestion;
        }

        public void setSendingQuestion(boolean sendingQuestion) {
            isSendingQuestion = sendingQuestion;
        }

        public boolean isSendingAnswer() {
            return isSendingAnswer;
        }

        public void setSendingAnswer(boolean sendingAnswer) {
            isSendingAnswer = sendingAnswer;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }


    public class PinPong implements  java.io.Serializable{
        boolean Ping = false;
        boolean Pong = false;


    }


}
