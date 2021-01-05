package sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConcurrentMath {
    public int level = 0;
    public double levelScore;
    public String question;
    public double answer;
    Random random = new Random();

    ConcurrentMath(){

    }


    public void CreateQuestion(){
        List<Integer> numbers = new ArrayList<>();

        int numberAmount = level/5 + 2;
        for(int i = 0; i < numberAmount; i++){
            int number = level*5 + random.nextInt(level*15);
            numbers.add(number);
        }



        String question = "";
        int result = 0;
        for(int i = 0; i < numbers.size(); i++){
            int a = random.nextInt(2);
            if(a == 0){
                result += numbers.get(i);

                if(i != 0) question += "+";
                question += Math.round(numbers.get(i));
            }
            if(a == 1){
                result -= numbers.get(i);

                question += "-";
                question += Math.round(numbers.get(i));
            }

        }

        answer = result;
        this.question = question;
        levelScore = level*2;
    }


}
