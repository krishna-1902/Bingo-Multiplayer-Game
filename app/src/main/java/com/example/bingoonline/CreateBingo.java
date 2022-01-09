package com.example.bingoonline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreateBingo {

    private Random rand=new Random();
    //To store key=ImageView(binX) val=ActualImage(bingoX)
    public HashMap<Integer,Integer> hashMap=new HashMap<Integer, Integer>();

    public CreateBingo(){

        ArrayList<Integer> arrayList=new ArrayList<Integer>();

        for(int i=0;i<25;i++)
        {
            arrayList.add(i+1);
        }


        for(int i=0;arrayList.size()!=0;i++)
        {
            int val=rand.nextInt(arrayList.size());
            hashMap.put(i+1,arrayList.remove(val));
        }

//        System.out.println("===========================================");
//
//        for(int i=1;i<=25;i=i+5) {
//                System.out.print(hashMap.get(i)+" "+hashMap.get(i+1)+" "+hashMap.get(i+2)+" "+hashMap.get(i+3)+" "+hashMap.get(i+4));
//            System.out.println();
//        }


    }

}
