package sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConcurrentMath {
    public int level;
    public double levelScore;
    public String question;
    public double answer;
    Random random = new Random();

    ConcurrentMath(){

    }


    public void CreateQuestion(){
        List<Double> numbers = new ArrayList<>();

        int numberAmount = level/5 + 1;
        for(int i = 0; i < numberAmount; i++){
            double number = level*5 + random.nextInt(level*15);
            numbers.add(number);
        }

        int result = 0;
        for(int i = 0; i < numbers.size(); i++){
            result += numbers.get(i);
        }

        String question = "";
        for(int i = 0; i < numbers.size()-1; i++){
            question += numbers.get(i) + "+";
        }

        answer = result;
        this.question = question;
        levelScore = level*2;
    }


}
