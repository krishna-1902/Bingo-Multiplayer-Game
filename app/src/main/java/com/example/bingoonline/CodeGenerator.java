package com.example.bingoonline;

import java.util.ArrayList;
import java.util.Random;

public class CodeGenerator {

    public String code="";
    public ArrayList<Character> numbers=new ArrayList<>();
    public ArrayList<Character> smallChar=new ArrayList<>();
    //public ArrayList<Character> capitalChar=new ArrayList<>();
    Random ran=new Random();

    public CodeGenerator()
    {
        for(int i=48;i<=57;i++)
        {
            numbers.add((char)i);
        }

        for(int i=65;i<=90;i++)
        {
            smallChar.add((char)i);
        }

        for (int i=97;i<=122;i++)
        {
            smallChar.add((char)i);
        }
    }

    public String generateCode() {

        for(int i=0;i<6;i++)
        {
            code+=smallChar.get(ran.nextInt(smallChar.size())).toString();
        }

        for(int i=0;i<3;i++)
        {
            code+=numbers.get(ran.nextInt(numbers.size())).toString();
        }
        return code;
    }
}
