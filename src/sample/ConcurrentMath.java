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

/*
        ArrayList<String> equation = new ArrayList();
        for(int i = 0; i < numbers.size(); i++){
            if(i == 0){
                equation.add(""+numbers.get(i));
                continue;
            }
            int operation = random.nextInt(3);

            if(operation == 0){
                equation.add("+");
                equation.add(""+numbers.get(i));
            }
            else if(operation == 1){
                equation.add("-");
                equation.add(""+numbers.get(i));
            }
            else if(operation == 2){
                equation.add("x");
                equation.add(""+numbers.get(i));
            }



        }

        for(String str: equation){
            question+= str;
        }

        int smallResult = 0;
        for(int i = 0; i < equation.size(); i++){
            if(equation.get(i) == "x"){
                smallResult = Integer.parseInt(equation.get(i-1)) * Integer.parseInt(equation.get(i+1));
                equation.remove(i-1);
                equation.remove(i-1);
                equation.set(i-1,smallResult+"");

            }

        }
        for(int i = 0; i < equation.size(); i++){
            if(equation.get(i) == "+"){
                smallResult = Integer.parseInt(equation.get(i-1)) + Integer.parseInt(equation.get(i+1));
                equation.remove(i-1);
                equation.remove(i-1);
                equation.set(i-1,smallResult+"");

            }
            else if(equation.get(i) == "-"){
                smallResult = Integer.parseInt(equation.get(i-1)) - Integer.parseInt(equation.get(i+1));
                equation.remove(i-1);
                equation.remove(i-1);
                equation.set(i-1,smallResult+"");

            }

        }
        result = Integer.parseInt(equation.get(0));
*/


        answer = result;
        this.question = question;
        levelScore = level*2;
    }


}
