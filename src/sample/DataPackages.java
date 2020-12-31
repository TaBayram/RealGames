package sample;

import java.util.ArrayList;
import java.util.List;

public class DataPackages {

    public class Player{
        private int ID;
        private String name;
        private int score;
        private boolean isJoining;
        private boolean isLeaving;
        private boolean isSendingAnswer;
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

        public boolean isSendingAnswer() {
            return isSendingAnswer;
        }

        public void setSendingAnswer(boolean sendingAnswer) {
            isSendingAnswer = sendingAnswer;
        }

        public boolean isChecking() {
            return isChecking;
        }

        public void setChecking(boolean checking) {
            isChecking = checking;
        }


    }

    public class PlayerList{
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



}
