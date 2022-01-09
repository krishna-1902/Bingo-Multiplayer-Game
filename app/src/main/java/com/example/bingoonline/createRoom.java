package com.example.bingoonline;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class createRoom extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance("https://fireapp-53e65-default-rtdb.firebaseio.com/");
    private DatabaseReference ref;
    public String mycode=null;
    public String code,useCode;
    public ProgressBar progressBar;
    public Button createroombut,joinbut;
    public ImageButton shareBut;
    public int flag=0;
    private final String stateCode="myStateCode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_room);
        progressBar=(ProgressBar)findViewById(R.id.loading);
        progressBar.setVisibility(View.INVISIBLE);
        createroombut=(Button)findViewById(R.id.createroombutton);
        joinbut=(Button)findViewById(R.id.joinbutton);
        shareBut=(ImageButton)findViewById(R.id.imageButton2);
        shareBut.setVisibility(View.INVISIBLE);
    }

    public void shareButtonCode(View view)
    {
        hideSoftKeyboard(view);
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, code);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            startActivity(Intent.createChooser(sendIntent, ""));
            startActivity(sendIntent);
        }catch (Exception ex){
            Toast.makeText(this,"Reqired Sharing App Is Not Installed",Toast.LENGTH_LONG).show();
        }
    }

    public void createMethod(View view)
    {
        hideSoftKeyboard(view);
        CodeGenerator codeGenerator = new CodeGenerator();
        useCode = codeGenerator.generateCode();
        System.out.println(useCode);
        code=useCode;

        progressBar.setVisibility(View.VISIBLE);
        createroombut.setEnabled(false);
        joinbut.setEnabled(false);
        EditText editText = (EditText) findViewById(R.id.code);
        KeyListener var=editText.getKeyListener();
        editText.setKeyListener(null);

        DatabaseReference reference=firebaseDatabase.getReference().child("datasection");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                boolean flag=false;
                do {
                    flag=false;

                    if(snapshot.exists()) {

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.getKey().matches(code)) {
                                flag = true;
                            }
                        }
                    }

                        if (!flag) {
                            CodeGenerator codeGenerator = new CodeGenerator();
                            useCode = codeGenerator.generateCode();
                            System.out.println(useCode);

                            TextView viewCode = (TextView) findViewById(R.id.codeView);
                            viewCode.setText(useCode);
                            editText.setText(useCode);
                            code = useCode;
                            shareBut.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                            createroombut.setEnabled(true);
                            joinbut.setEnabled(true);
                            editText.setKeyListener(var);
                        }

                }while (flag);

                if(!flag)
                {
                    reference.child(code).child("marked").setValue(0);
                    reference.child(code).child("winning").setValue(2);
                    reference.child(code).child("players").setValue(0);
                    reference.child(code).child("sender").setValue(0);
                    reference.child(code).child("reciever").setValue(0);
                    reference.child(code).child("active").setValue(1);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    public void joinMethod(View view)
    {
        EditText editText=(EditText)findViewById(R.id.code);
        KeyListener var=editText.getKeyListener();
        String checkcode=editText.getText().toString();
        hideSoftKeyboard(view);

        if(!TextUtils.isEmpty(checkcode))
        {
            progressBar.setVisibility(View.VISIBLE);
            createroombut.setEnabled(false);
            joinbut.setEnabled(false);
            editText.setKeyListener(null);
            shareBut.setVisibility(View.INVISIBLE);
            ref=firebaseDatabase.getReference().child("datasection");

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                    boolean validity=false;

                    if(snapshot.exists())
                    {
                        for(DataSnapshot snapshot1:snapshot.getChildren())
                        {
                            if(snapshot1.getKey().matches(checkcode))
                            {
                                validity=true;
                                Map map=(Map)snapshot1.getValue();
                                int active=Integer.parseInt(map.get("active").toString());
                                int activePlayer=Integer.parseInt(map.get("players").toString());

                                if(activePlayer<2)
                                {
                                    Intent intent=new Intent(createRoom.this,MainActivity.class);
                                    if(active==1 && checkcode!=null && useCode!=null && useCode.equals(checkcode)) {
                                        ref.child(checkcode).child("players").setValue(1);
                                        ref.child(checkcode).child("active").setValue(2);
                                        intent.putExtra("isCodeMaker",1+"");
                                    }
                                    else
                                    {
                                        boolean goFlag=true;
                                        while(goFlag)
                                        {
                                            if(active==2)
                                            {
                                                goFlag=false;
                                            }
                                            else{
                                                joinMethod(view);
                                                return;
                                            }
                                        }

                                        ref.child(checkcode).child("players").setValue(2);
                                        intent.putExtra("isCodeMaker",0+"");

                                    }

                                    intent.putExtra("myCode",checkcode);
                                    startActivity(intent);
                                    createRoom.this.finish();
                                }
                                else{
                                    Toast.makeText(createRoom.this,"Room Is Full !!!",Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    createroombut.setEnabled(true);
                                    joinbut.setEnabled(true);
                                    editText.setKeyListener(var);
                                }
                            }
                        }

                    }

                    if(!validity)
                    {
                        Toast.makeText(createRoom.this,"Invalid Code !!!",Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        createroombut.setEnabled(true);
                        joinbut.setEnabled(true);
                        editText.setKeyListener(var);
                    }

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
        else
        {
            Toast.makeText(this,"Invalid Code !!!",Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            createroombut.setEnabled(true);
            joinbut.setEnabled(true);
            editText.setKeyListener(var);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    public void hideSoftKeyboard(View view)
    {
        InputMethodManager iim=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        iim.hideSoftInputFromWindow(view.getWindowToken(),0);
    }
}