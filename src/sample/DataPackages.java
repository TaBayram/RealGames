package sample;

import java.util.ArrayList;
import java.util.List;

public class DataPackages implements java.io.Serializable{

    public class Player implements java.io.Serializable{
        private int ID;
        private String name;
        private int score;

        private boolean isJoining;
        private boolean isLeaving;
        private boolean isChecking;


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



    }

    public class PlayerList implements java.io.Serializable{
        public List<String> getNames() {
            return names;
        }

        public void setNames(List<String> names) {
            this.names = names;
        }

        List<String> names = new ArrayList<>();


    }


    public class Message{

    }

    public class MathQuestion implements  java.io.Serializable{

        private String question;
        private double answer;
        private double point;


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

    }


}
