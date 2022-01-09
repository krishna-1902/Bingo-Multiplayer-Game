package com.example.bingoonline;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OfflineMode extends AppCompatActivity {

    private int result=2;   //0-->computer  1-->user
    boolean lock=true;
    private Random random=new Random();
    public TextView textView;
    private boolean playerWon=false;
    private CreateBingo createBingo,createBingoComputer;
    public MediaPlayer mediaPlayer;

    private HashMap<Integer,Integer> userTable=new HashMap<Integer,Integer>();
    private HashMap<Integer,Integer> computerTable=new HashMap<Integer,Integer>();

    //To check How much lines are completed for user
    private int lines=0;
    //To check How much lines are completed for computer ***** (line)
    private int line=0;

    //array for marked positions 0-unmarked 1-marked for user
    private int arr[]=new int[26];
    //array for marked positions 0-unmarked 1-marked for user
    private int arrcomp[]=new int[26];

    //ArrayList for winning position of user
    private ArrayList<ArrayList<Integer>> winPos=new ArrayList<ArrayList<Integer>>();
    private ArrayList<ArrayList<Integer>> winPosComp=new ArrayList<ArrayList<Integer>>();

    //ArrayList for removal of marked elements .. Only one required
    public ArrayList<Integer> arrayListOfMainActivity=new ArrayList<>();

    public Context context=null;

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
            winPosComp.add(arrayListInner);
        }

        for(int i=1;i<=5;i++)
        {
            ArrayList<Integer> arrayListInner=new ArrayList<Integer>();
            for(int j=i;j<=25;j=j+5)
            {
                arrayListInner.add(j);
            }
            winPos.add(arrayListInner);
            winPosComp.add(arrayListInner);
        }

        //for cross left-top to right-bottom
        ArrayList<Integer> arrayListInner=new ArrayList<Integer>();
        arrayListInner.add(1);
        arrayListInner.add(7);
        arrayListInner.add(13);
        arrayListInner.add(19);
        arrayListInner.add(25);
        winPos.add(arrayListInner);
        winPosComp.add(arrayListInner);

        //for cross right-top to left-bottom
        ArrayList<Integer> arrayListInner1=new ArrayList<Integer>();
        arrayListInner1.add(5);
        arrayListInner1.add(9);
        arrayListInner1.add(13);
        arrayListInner1.add(17);
        arrayListInner1.add(21);
        winPos.add(arrayListInner1);
        winPosComp.add(arrayListInner1);

//        printWin(winPos);
//        printWin(winPosComp);
    }

    /*
     *
     * Function To Check User Won Or Not
     *
     *
     */
    public boolean checkWon(MediaPlayer mediaPlayer)
    {
        //These lists has to be removed from winpos arraylist to get winner
        ArrayList<ArrayList<Integer>> toremovelists=new ArrayList<ArrayList<Integer>>();

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
                }
            }
        }

        for(ArrayList<Integer> rem : toremovelists) //Total list's has to remove from user winPos
        {
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.reset();
                    mediaPlayer.release();
                }catch (Exception ex){}
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
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.reset();
                    mediaPlayer.release();
                }catch (Exception ex){}
                mediaPlayer = null;
            }
            mediaPlayer=MediaPlayer.create(this,R.raw.winner);
            mediaPlayer.start();

            androidx.appcompat.app.AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("BINGO!!!");
            builder.setIcon(R.drawable.trophy);

            builder.setMessage("You Won The Match").setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result=1;
                    textView.setText("You Won !!!");

                    Intent intent=new Intent(context,OfflineMode.class);
                    OfflineMode.this.finish();
                    context.startActivity(intent);
                }
            }).show();

            return true;
        }

        return false;
    }

    public boolean checkWonComputer()
    {
        //These lists has to be removed from winPosCom arraylist to get winner
        ArrayList<ArrayList<Integer>> toremovelists=new ArrayList<ArrayList<Integer>>();

        for(ArrayList<Integer> temp : winPosComp)
        {
            int count=0;
            for(int i=1;i<arrcomp.length;i++)
            {
                if(arrcomp[i]==1 && temp.contains(i))
                {
                    count++;
                }
            }

            if(count==5)
            {
                toremovelists.add(temp);
                line++;
                if(line<=5) {

                    //Line is completed : setting bar
                    String idstring = "imgView" + line;
                    int idx = this.getResources().getIdentifier(idstring, "id", getPackageName());
                    ImageView img = (ImageView) findViewById(idx);

                    String lineNumber = "bar" + line;
                    int idy = this.getResources().getIdentifier(lineNumber, "drawable", getPackageName());
                    img.setImageResource(idy);
                }
            }
        }

        for(ArrayList<Integer> rem : toremovelists) //Total list's has to remove from user winPos
        {
            winPosComp.remove(rem);
        }

        if(winPosComp.size()<=7)    //Computer has completed 5 lines
        {
            playerWon=true;
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.reset();
                    mediaPlayer.release();
                }catch (Exception ex){}
                mediaPlayer=null;
            }
            mediaPlayer=MediaPlayer.create(this,R.raw.loser);
            mediaPlayer.start();

            androidx.appcompat.app.AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("You Lost !!!");
            builder.setIcon(R.drawable.lost);
            builder.setMessage("Computer Won The Game !!!\nBetter Luck Next Time !!!").setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result=0;
                    textView.setText("Computer Won !!!");
                    Intent intent=new Intent(context,OfflineMode.class);
                    context.startActivity(intent);
                }
            }).show();

            return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        ImageView pointComputer=(ImageView)findViewById(R.id.imageView2);
        pointComputer.setAlpha(0f);

        context=this;

        //setting winning positions
        winposition();

        createBingo=new CreateBingo();
        createBingoComputer=new CreateBingo();

        for(int i=0;i<25;i++)
        {
            arrayListOfMainActivity.add(i+1);
        }

        for(Map.Entry<Integer,Integer> entry : createBingo.hashMap.entrySet()) {

            userTable.put(entry.getValue(),entry.getKey());

            String pic_number="bin"+entry.getKey();
            int id = this.getResources().getIdentifier(pic_number,"id",getPackageName());
            ImageView img = (ImageView) findViewById(id);

            String pic_name="bingo"+entry.getValue();
            int drawableId=this.getResources().getIdentifier(pic_name,"drawable",getPackageName());
            img.setImageResource(drawableId);

            textView=(TextView)findViewById(R.id.selectedDigit);
            textView.setText("Tap To Play !!!");
        }

        for(Map.Entry<Integer,Integer> entry : createBingoComputer.hashMap.entrySet())
        {
            int key=entry.getValue();
            int val=entry.getKey();
            computerTable.put(key,val);
        }

    }

    public void tapToPlay(View view)
    {
        if(!playerWon && lock) {
            ImageView img = (ImageView) view;
            int tag = Integer.parseInt(img.getTag().toString());

            if (arr[tag] == 0) {
                //img.setImageResource(R.drawable.bingo0);
                if (mediaPlayer != null) {
                    try {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }
                        mediaPlayer.reset();
                        mediaPlayer.release();
                    }catch (Exception ex){}
                    mediaPlayer = null;
                }
                mediaPlayer=MediaPlayer.create(this,R.raw.playsound2);
                mediaPlayer.start();
                img.setImageResource(R.drawable.winemoji2);

                arr[tag] = 1;
                int markIt=createBingo.hashMap.get(tag);
                //Toast.makeText(this,""+markIt,Toast.LENGTH_SHORT).show();
                textView.setText("You Tapped On : "+markIt);
                int markAtIndex=computerTable.get(markIt);
                arrcomp[markAtIndex]=1;

                arrayListOfMainActivity.remove(arrayListOfMainActivity.indexOf(tag));

                if(checkWon(mediaPlayer))
                {
                    return;
                }
                else if(checkWonComputer())
                {
                    return;
                }

                //textView.setText("Opponent's Turn");
                ImageView pointPlayer=(ImageView)findViewById(R.id.imageView1);
                pointPlayer.setAlpha(0f);
                ImageView pointComputer=(ImageView)findViewById(R.id.imageView2);
                pointComputer.setAlpha(1f);

                lock=false;
                new CountDownTimer(random.nextInt(1500)+500,500){
                    @Override
                    public void onFinish() {
                        computerTurn();
                        lock=true;
                    }

                    @Override
                    public void onTick(long millisUntilFinished) {
                    }
                }.start();

            }
            else
            {
                Toast.makeText(this, "Tap On Valid Number", Toast.LENGTH_SHORT).show();
            }
        }
        else if(playerWon){
            if(result==1) {
                Toast.makeText(this, "Hurry... You Won !!!", Toast.LENGTH_SHORT).show();
            }
            else if(result==0)
            {
                Toast.makeText(this,"Oops... Computer Won !!!",Toast.LENGTH_SHORT).show();
            }
        }
        else if(!lock) {
            Toast.makeText(this,"Opponent's Turn !!",Toast.LENGTH_SHORT).show();
        }

    }

    private void computerTurn()
    {
        if(!playerWon) {

            if (!arrayListOfMainActivity.isEmpty()) {
                int index = random.nextInt(arrayListOfMainActivity.size());
                int val = arrayListOfMainActivity.remove(index);
                arr[val] = 1;
                int markIt=createBingo.hashMap.get(val);
                //Toast.makeText(this,""+markIt,Toast.LENGTH_SHORT).show();
                textView.setText("Computer Tapped On : " +markIt);
                int markAtIndex=computerTable.get(markIt);
                arrcomp[markAtIndex] = 1;

                if(checkWonComputer())      //checking if computer won the game or not
                {
                    return;
                }
                else {
                    //setting image inside user's view
                    if (mediaPlayer != null) {
                        try {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();
                            }
                            mediaPlayer.reset();
                            mediaPlayer.release();
                        }catch (Exception e){}
                        mediaPlayer = null;
                    }
                    mediaPlayer = MediaPlayer.create(this, R.raw.playsound1);
                    mediaPlayer.start();
                    String pic_number = "bin" + val;
                    int id = this.getResources().getIdentifier(pic_number, "id", getPackageName());
                    ImageView img = (ImageView) findViewById(id);
                    //img.setImageResource(R.drawable.bingo0);
                    img.setImageResource(R.drawable.winemoji);
                    //textView.setText("Your Turn");
                    ImageView pointPlayer=(ImageView)findViewById(R.id.imageView1);
                    pointPlayer.setAlpha(1f);
                    ImageView pointComputer=(ImageView)findViewById(R.id.imageView2);
                    pointComputer.setAlpha(0f);


                    if (checkWon(mediaPlayer)) {
                        return;
                    }
                }

            }
            else {
                Toast.makeText(this, "Game Tied", Toast.LENGTH_SHORT).show();
            }
        }
        else if(playerWon)
        {
            if(result==1) {
                Toast.makeText(this, "Hurry... You Won !!!", Toast.LENGTH_SHORT).show();
            }
            else if(result==0)
            {
                Toast.makeText(this,"Oops... Computer Won !!!",Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}