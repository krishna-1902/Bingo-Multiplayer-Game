package com.example.bingoonline;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    public DatabaseReference ref;
    public int isMyTurn=0;
    public int isCodeMaker=0;
    public String myCode=null;
    public boolean bothOnline=false;
    public int buddy;
    public int CodeMakerbar=0,requestedMakerbar=0;

    public int result=2;   //0-->computer  1-->user
    public boolean lock=true;
    public Random random=new Random();
    public TextView textView;
    public boolean playerWon=false;
    public CreateBingo createBingo,createBingoComputer;

    //  key is number to mark and value is index
    private HashMap<Integer,Integer> userTable=new HashMap<Integer,Integer>();

    //To check How much lines are completed for user
    private int lines=0;

    //array for marked positions 0-unmarked 1-marked for user
    private int arr[]=new int[26];

    //ArrayList for winning position of user
    private ArrayList<ArrayList<Integer>> winPos=new ArrayList<ArrayList<Integer>>();

    public Context context=null;

    public MediaPlayer mediaPlayer=null;


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        context=this;

//      Taking parameters from previous intent
        Intent previousIntent=getIntent();
        myCode=previousIntent.getStringExtra("myCode");
        String valueForCodeMaker =previousIntent.getStringExtra("isCodeMaker");
        isCodeMaker=Integer.parseInt(valueForCodeMaker);
        isMyTurn=isCodeMaker;
        Log.d("mylog",valueForCodeMaker);

        //setting winning positions
        winposition();

        createBingo=new CreateBingo();

        //setting the bingo tables for users
        for(Map.Entry<Integer,Integer> entry : createBingo.hashMap.entrySet()) {

            userTable.put(entry.getValue(),entry.getKey());     //Value present on which position hashmap

            String pic_number="bin"+entry.getKey();
            int id = this.getResources().getIdentifier(pic_number,"id",getPackageName());
            ImageView img = (ImageView) findViewById(id);

            String pic_name="bingo"+entry.getValue();
            int drawableId=this.getResources().getIdentifier(pic_name,"drawable",getPackageName());
            img.setImageResource(drawableId);
        }

        //System.out.println("HashMap : "+ userTable.toString());

        ImageView pointMe = (ImageView) findViewById(R.id.imageView1);
        ImageView pointOpponent = (ImageView) findViewById(R.id.imageView2);
        textView = (TextView) findViewById(R.id.selectedDigit);
        if (isMyTurn == 1) {   //If this is my turn then set star of opponent invisible
            pointMe.setAlpha(1f);
            pointOpponent.setAlpha(0f);
            textView.setText("Tap To Play !!!");
        }
        else {
            pointOpponent.setAlpha(1f);
            pointMe.setAlpha(0f);
            textView.setText("Opponent's Move !!!");
        }


        DatabaseReference refForLeave=firebaseDatabase.getReference().child("datasection");
        refForLeave.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) { }
            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) { }
            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.getKey().matches(myCode))
                {
                    if(!playerWon) {
                        AlertDialog.Builder alertdialog = new AlertDialog.Builder(MainActivity.this);
                        alertdialog.setTitle("Victory !!!");
                        alertdialog.setMessage("You Won The Match,\nOpponent Left The Game!!!");
                        alertdialog.setIcon(R.drawable.trophy);
                        alertdialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.this, createRoom.class);
                                startActivity(intent);
                                MainActivity.this.finish();
                            }
                        });
                        alertdialog.show();
                    }
                }
            }
            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) { }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) { }
        });


        DatabaseReference refForChild=firebaseDatabase.getReference().child("datasection").child(myCode);

        refForChild.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

                if(snapshot.exists()) {

                    String key = snapshot.getKey().toString();

                    if(key.equalsIgnoreCase("winning"))
                    {
                        int cMaker=Integer.parseInt(snapshot.getValue().toString());
                        playerWon=true;

                        if(cMaker==isCodeMaker) {

                            if(mediaPlayer!=null) {
                                if(mediaPlayer.isPlaying())
                                {
                                    mediaPlayer.stop();
                                }
                                mediaPlayer.reset();
                                mediaPlayer.release();
                                mediaPlayer=null;
                            }
                            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.winner);
                            mediaPlayer.start();

                            textView.setText("You Won !!!");
                            androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("BINGO!!!");
                            builder.setIcon(R.drawable.trophy);

                            builder.setMessage("You Won The Match\nWell Played !!!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result = 1;
                                    Intent intent = new Intent(MainActivity.this, createRoom.class);
                                    context.startActivity(intent);
                                    MainActivity.this.finish();
                                }
                            }).show();

                        }
                        else
                        {
                            if(mediaPlayer!=null) {
                                if(mediaPlayer.isPlaying())
                                {
                                    mediaPlayer.stop();
                                }
                                mediaPlayer.reset();
                                mediaPlayer.release();
                                mediaPlayer=null;
                            }
                            mediaPlayer=MediaPlayer.create(MainActivity.this,R.raw.loser);
                            mediaPlayer.start();
                            textView.setText("You Lost !!!");

                            androidx.appcompat.app.AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("You Lost !!!");
                            builder.setIcon(R.drawable.lost);

                            builder.setMessage("Oops.. Too Close\nBetter Luck Next Time !!!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result=1;
                                    Intent intent=new Intent(MainActivity.this,createRoom.class);
                                    context.startActivity(intent);
                                    MainActivity.this.finish();
                                }
                            }).show();
                        }


                    }
                    else if (key.equalsIgnoreCase("marked")) {

                        if(!playerWon)
                        {
                            int vanishTheElement = Integer.parseInt(snapshot.getValue().toString());

                            int index = userTable.get(vanishTheElement);

                            if (arr[index] == 0) {
                                arr[index] = 1;
                                String toFind = "bin" + index;
                                int imageId = MainActivity.this.getResources().getIdentifier(toFind, "id", getPackageName());
                                if(mediaPlayer!=null) {
                                    if(mediaPlayer.isPlaying())
                                    {
                                        mediaPlayer.stop();
                                    }
                                    mediaPlayer.reset();
                                    mediaPlayer.release();
                                    mediaPlayer=null;
                                }
                                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.playsound1);
                                mediaPlayer.start();
                                ImageView imageView = (ImageView) findViewById(imageId);
                                imageView.setImageResource(R.drawable.winemoji);

                            }

                            if (isMyTurn == 1) {
                                isMyTurn = 0;
                                pointMe.setAlpha(0f);
                                pointOpponent.setAlpha(1f);
                                textView.setText("Opponent's Move !!!");
                            } else if (isMyTurn == 0) {
                                pointOpponent.setAlpha(0f);
                                pointMe.setAlpha(1f);
                                isMyTurn = 1;
                                textView.setText("Tap To Play !!!");
                            }
                            if (checkWon(new MediaPlayer())) {
                                return;
                            }
                        }
                    }
                    else if (key.equalsIgnoreCase("players"))
                    {
                        buddy = Integer.parseInt(snapshot.getValue().toString());
                        Toast.makeText(MainActivity.this,"Hey, Opponent Joined The Game...",Toast.LENGTH_LONG).show();
                        Toast.makeText(MainActivity.this,"Tap To Play",Toast.LENGTH_LONG).show();

                        if (buddy == 2) {
                            //Setting turn of players
                            bothOnline = true;
                            if (isMyTurn == 1) {   //If this is my turn then set star of opponent invisible
                                pointMe.setAlpha(1f);
                                pointOpponent.setAlpha(0f);
                                textView.setText("Tap To Play !!!");
                            } else {
                                pointOpponent.setAlpha(1f);
                                pointMe.setAlpha(0f);
                                textView.setText("Wait For Opponent's Move !!!");
                            }
                        }
                        else {
                            bothOnline = false;
                            if(buddy==1)
                            {
                                Toast.makeText(MainActivity.this,"buddy is one on your mob",Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                    else if(key.equalsIgnoreCase("sender"))
                    {
                        if(isCodeMaker==0)
                        {
                            int valForBar=Integer.parseInt(snapshot.getValue().toString());
                            //Line is completed : setting bar
                            String idstring = "imgView" + valForBar;
                            int idx = MainActivity.this.getResources().getIdentifier(idstring, "id", getPackageName());
                            ImageView img = (ImageView) findViewById(idx);

                            String lineNumber = "bar" + valForBar;
                            int idy = MainActivity.this.getResources().getIdentifier(lineNumber, "drawable", getPackageName());
                            img.setImageResource(idy);
                        }
                    }
                    else if(key.equalsIgnoreCase("reciever"))
                    {
                        if(isCodeMaker==1)
                        {
                            int valForBar=Integer.parseInt(snapshot.getValue().toString());
                            //Line is completed : setting bar
                            String idstring = "imgView" + valForBar;
                            int idx = MainActivity.this.getResources().getIdentifier(idstring, "id", getPackageName());
                            ImageView img = (ImageView) findViewById(idx);

                            String lineNumber = "bar" + valForBar;
                            int idy = MainActivity.this.getResources().getIdentifier(lineNumber, "drawable", getPackageName());
                            img.setImageResource(idy);
                        }
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"Snapshot doesn't exist",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void tapToPlay(View view)
    {

        //code for if both players are not online
        if(!bothOnline)
        {
            if(isCodeMaker==0)
            {
                bothOnline=true;
            }
            else {
                Toast.makeText(MainActivity.this, "Wait For Opponent !!!", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder alertdialog = new AlertDialog.Builder(MainActivity.this);
                alertdialog.setTitle("Wait For The Opponent !!!");
                alertdialog.setMessage("Hope, You Shared Code With Friends !!!\nRoom Code : " + myCode);
                alertdialog.setIcon(R.drawable.trophy);
                alertdialog.setPositiveButton("Wait", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertdialog.setNegativeButton("Leave", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ref.removeValue();
                        Intent intentBack=new Intent(MainActivity.this,createRoom.class);
                        startActivity(intentBack);
                        MainActivity.this.finish();
                    }
                });
                alertdialog.show();
            }
        }
        //code if both are online
        else{

            DatabaseReference refToChange=firebaseDatabase.getReference().child("datasection").child(myCode).child("marked");
            if(!playerWon) {
                if (isMyTurn == 1) {
                    ImageView img = (ImageView) view;
                    int tag = Integer.parseInt(img.getTag().toString());    //getting index of tapped image

                    //if imageview is not tapped
                    if (arr[tag] == 0) {
                        //Setting the image and sound for successful tap
                        if (mediaPlayer != null) {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();
                            }
                            mediaPlayer.reset();
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }
                        mediaPlayer = MediaPlayer.create(this, R.raw.playsound2);
                        mediaPlayer.start();
                        img.setImageResource(R.drawable.winemoji2);

                        arr[tag] = 1;   //setting not tapped to tapped
                        int markIt = createBingo.hashMap.get(tag);  //returns actual number at this index
                        textView.setText("You Tapped On : " + markIt);

                        if (checkWon(mediaPlayer)) {
                            return;
                        }

                        refToChange.setValue(markIt);//Marking the number

                    }
                    //if image view is already tapped
                    else {
                        System.out.println("tag : " + arr[tag]);
                        Toast.makeText(this, "Tap On Valid Number", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Wait For Opponent's Move !!!", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    /*
     *
     * Function To Check User Won Or Not
     *
     *
     */
    public boolean checkWon(MediaPlayer med)
    {
        //These lists has to be removed from winpos arraylist to get winner
        ArrayList<ArrayList<Integer>> toremovelists=new ArrayList<ArrayList<Integer>>();
        DatabaseReference myref=firebaseDatabase.getReference().child("datasection").child(myCode);

        for(ArrayList<Integer> temp : winPos)
        {
            int count=0;
            for(int i=1;i<arr.length;i++)
            {
                if(arr[i]==1 && temp.contains(i))
                {
                    count++;
                }
            }

            if(count==5)
            {
                toremovelists.add(temp);
                lines++;
                if(lines<=5) {
                    //Line is Completed : setting "BINGO" main Label
                    String idstring = "character" + lines;
                    int idx = this.getResources().getIdentifier(idstring, "id", getPackageName());
                    ImageView img = (ImageView) findViewById(idx);
                    img.setTranslationY(-2000f);

                    String lineNumber = "line" + lines;
                    int idy = this.getResources().getIdentifier(lineNumber, "drawable", getPackageName());
                    img.setImageResource(idy);
                    img.animate().translationYBy(2000f);

                    if(isCodeMaker==1) {
                        myref.child("sender").setValue(lines);
                    }
                    else if(isCodeMaker==0)
                    {
                        myref.child("reciever").setValue(lines);
                    }
                }
            }
        }

        for(ArrayList<Integer> rem : toremovelists) //Total list's has to remove from user winPos
        {
            if(med!=null) {
                if(med.isPlaying())
                {
                    med.stop();
                }
                med.reset();
                med.release();
                med=null;
            }
            mediaPlayer=MediaPlayer.create(this,R.raw.linecomplete);
            mediaPlayer.start();

            for(int a : rem)
            {
                String idString="bin"+a;
                int idx=this.getResources().getIdentifier(idString,"id",getPackageName());
                ImageView img=(ImageView)findViewById(idx);
                img.setImageResource(R.drawable.bingo00);
            }

            winPos.remove(rem);
        }

        if(winPos.size()<=7)    //User has completed 5 lines
        {
            playerWon=true;
            DatabaseReference databaseReference=firebaseDatabase.getReference().child("datasection").child(myCode).child("winning");
            databaseReference.setValue(isCodeMaker);
            return true;
        }

        return false;
    }

    /*
     *
     * Function To Create WinPositions
     *
     *
     */
    private void winposition()
    {
        for(int i=0;i<5;i++)
        {
            ArrayList<Integer> arrayListInner=new ArrayList<Integer>();
            for(int j=5*i+1;j<=5*i+5;j++)
            {
                arrayListInner.add(j);
            }
            winPos.add(arrayListInner);
        }

        for(int i=1;i<=5;i++)
        {
            ArrayList<Integer> arrayListInner=new ArrayList<Integer>();
            for(int j=i;j<=25;j=j+5)
            {
                arrayListInner.add(j);
            }
            winPos.add(arrayListInner);
        }

        //for cross left-top to right-bottom
        ArrayList<Integer> arrayListInner=new ArrayList<Integer>();
        arrayListInner.add(1);
        arrayListInner.add(7);
        arrayListInner.add(13);
        arrayListInner.add(19);
        arrayListInner.add(25);
        winPos.add(arrayListInner);

        //for cross right-top to left-bottom
        ArrayList<Integer> arrayListInner1=new ArrayList<Integer>();
        arrayListInner1.add(5);
        arrayListInner1.add(9);
        arrayListInner1.add(13);
        arrayListInner1.add(17);
        arrayListInner1.add(21);
        winPos.add(arrayListInner1);

//        printWin(winPos);
//        printWin(winPosComp);
    }

//    public void printWin(ArrayList<ArrayList<Integer>> printing)
//    {
//        System.out.println("-------------------------------------------------------------");
//        for(ArrayList<Integer> temp : printing)
//        {
//            System.out.print("List : ");
//            for(int i=0;i<temp.size();i++)
//            {
//                System.out.print(temp.get(i)+" ");
//            }
//            System.out.println();
//        }
//        System.out.println("-------------------------------------------------------------");
//
//    }

    @Override
    protected void onStart() {
        super.onStart();
        ref=firebaseDatabase.getReference().child("datasection").child(myCode);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    Map map=(Map)snapshot.getValue();
                    if(Integer.parseInt(map.get("players").toString())!=2)
                    {
                        textView=(TextView)findViewById(R.id.selectedDigit);
                        textView.setText("Wait For Opponent To Join....");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ref=firebaseDatabase.getReference().child("datasection").child(myCode);
        ref.removeValue();
        Intent intent=new Intent(MainActivity.this,createRoom.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ref=firebaseDatabase.getReference().child("datasection").child(myCode);
        ref.removeValue();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ref=firebaseDatabase.getReference().child("datasection").child(myCode);
        ref.removeValue();
    }
}