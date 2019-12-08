package com.kwc.doatplace;

import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.kakao.kakaostory.KakaoStoryService;
import com.kakao.kakaostory.callback.StoryResponseCallback;
import com.kakao.kakaostory.request.PostRequest;
import com.kakao.kakaostory.response.model.MyStoryInfo;
import com.kakao.kakaotalk.KakaoTalkService;
import com.kakao.kakaotalk.callback.TalkResponseCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.util.KakaoParameterException;
import com.kakao.util.helper.log.Logger;

import com.google.firebase.iid.FirebaseInstanceId;
import com.kakao.kakaotalk.callback.TalkResponseCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.util.helper.log.Logger;

public class FirstActivity extends TabActivity implements View.OnClickListener {
    //tab1
    private ImageButton pImgBtn;    //카카오 프로필 이미지 출력
    private EditText pNameTxt;      //카카오 닉네임 출력
    private TextView uniqueNTxt;     //앱 내 고유번호 출력
    private Button logoutBtn;       //앱 로그아웃
    private Button deleteBtn;       //앱 탈퇴
    //tab2
    private Button forTalkBtn;      //나에게 카카오톡 보내기
    //tab3
    private Button refreshBtn; //카카오스토리 사용자인지 확인
    int isLogin =0;

    String content = "";//카카오스토리에 게시할 내용
    boolean CheckUser;//카카오스토리 유저면 True

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //2. getTabHost 메서드를 통해 TabHost 객체를 생성
        TabHost mTab = getTabHost();
        LayoutInflater inflater = LayoutInflater.from(this);

        //3. LayoutInflater.from 메서드로 클래스정보 this를 전달하여 현재 Context의 전개자를 구한다.
        inflater.inflate(R.layout.activity_first, mTab.getTabContentView(), true);

        //4. TabHost에 Spect, LayoutPage추가
        mTab.addTab(mTab.newTabSpec("tag").setIndicator("기상예보").setContent(R.id.tab1));
        mTab.addTab(mTab.newTabSpec("tag").setIndicator("Kakaotalk").setContent(R.id.tab2));
        mTab.addTab(mTab.newTabSpec("tag").setIndicator("사용자정").setContent(R.id.tab3));

        //tab1
        pNameTxt = (EditText)findViewById(R.id.PNameTxt);           //카카오 닉네임 출력
        uniqueNTxt = (TextView)findViewById(R.id.UniqueNTxt);       //앱 내 고유번호 출력
        logoutBtn = (Button)findViewById(R.id.LogoutBtn);           //앱 로그아웃
        deleteBtn = (Button)findViewById(R.id.DeleteBtn);           //앱 탈퇴

        logoutBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        uniqueNTxt.setText("식별번호:"+getIntent().getStringExtra(MainActivity.USER_ID)); //고유번호 출력
        pNameTxt.setText("닉네임:"+getIntent().getStringExtra(MainActivity.NICKNAME));
        pNameTxt.setEnabled(false);
        //tab2
        forTalkBtn = (Button)findViewById(R.id.ForTalkBtn);         //나에게 카카오톡 보내기

        forTalkBtn.setOnClickListener(this);
        //tab3
        refreshBtn = (Button)findViewById(R.id.RefreshBtn);    //카카오스토리 사용자인지 확인


        refreshBtn.setOnClickListener(this);

//        logoutBtn.setText(getIntent().getStringExtra(MainActivity.USER_ID));
        if(getIntent().getStringExtra(MainActivity.USER_ID)==null){
            logoutBtn.setText("비로그인 상태");
            logoutBtn.setEnabled(false);
            deleteBtn.setText("회원가입 후 알림을 받을 수 있습니다.");
            deleteBtn.setEnabled(false);
            uniqueNTxt.setText("푸시알림을 받기위하여 로그인이 필요합니다");
            pNameTxt.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), FirebaseInstanceId.getInstance().getToken().toString()+"",Toast.LENGTH_LONG).show();
        }
//        requestIsStoryUser();   //비로그인체크
    }



    @Override
    public void onClick(View v) {

         if(v.equals(logoutBtn)){
            //로그아웃 후 처음 화면으로 이동
            //처음 화면에서 로그인 버튼을 누르는 동시에 로그아웃이 실행
            //따라서 로그아웃은 처음 화면으로 돌아가기만 하도록 작성
            Intent intent = new Intent(FirstActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else if(v.equals(deleteBtn)){
            //앱 탈퇴 처리
            onClickUnlink();
        }
//         else if(v.equals(forTalkBtn)){
//             Intent intent_loc = new Intent(getApplicationContext(), LocationActivity.class);
//             startActivity(intent_loc);
//         }



//        else if(v.equals(refreshBtn)){
////            requestIsStoryUser();
//             Intent intent = new Intent(getApplicationContext(), ForecastActivity.class);
//             startActivity(intent);
//        }

    }

    //두번 연속으로 뒤로가기 누를 시 종료
    long pressTime;
    @Override
    public void onBackPressed(){

        long currentTime = System.currentTimeMillis();
        long intervalTime = currentTime - pressTime;

        if(intervalTime <2000){
            super.onBackPressed();
//            finishAffinity();
        }else{
            pressTime = currentTime;
            Toast.makeText(this,"한 번 더 누르시면 종료됩니다.",Toast.LENGTH_SHORT).show();
        }
    }
    //재 로그인 요청
    private void redirectLoginActivity() {
        final Intent intent = new Intent(this, FirstActivity.class);
        startActivity(intent);
        finish();
    }


    //deleteBtn부분//
    //앱 탈퇴 처리
    private void onClickUnlink() {
        final String appendMessage = getString(R.string.com_kakao_confirm_unlink);
        new AlertDialog.Builder(this)
                .setMessage(appendMessage)
                .setPositiveButton(getString(R.string.com_kakao_ok_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UserManagement.requestUnlink(new UnLinkResponseCallback() {
                                    @Override
                                    public void onFailure(ErrorResult errorResult) {
                                        Logger.e(errorResult.toString());
                                    }

                                    @Override
                                    public void onSessionClosed(ErrorResult errorResult) {
                                        //redirectLoginActivity();
                                    }

                                    @Override
                                    public void onNotSignedUp() {
                                        //redirectSignupActivity();
                                    }

                                    @Override
                                    public void onSuccess(Long userId) {
                                        Toast.makeText(getApplicationContext(),"탈퇴되었습니다.",Toast.LENGTH_LONG).show();
                                        //redirectLoginActivity();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(getString(R.string.com_kakao_cancel_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

    }



    //카카오톡 콜백 클래스
    private abstract class KakaoTalkResponseCallback<T> extends TalkResponseCallback<T> {
        @Override
        public void onNotKakaoTalkUser() {
            Logger.w("not a KakaoTalk user");
        }

        @Override
        public void onFailure(ErrorResult errorResult) {
            Toast.makeText(getApplicationContext(),"전송 실패",Toast.LENGTH_LONG).show();
            Logger.e("failure : " + errorResult);
        }

        @Override
        public void onSessionClosed(ErrorResult errorResult) {
            //redirectLoginActivity();
//재로그인!
        }

        @Override
        public void onNotSignedUp() {
            //redirectSignupActivity();
//재로그인!
        }
    }



    private abstract class KakaoStoryResponseCallback<T> extends StoryResponseCallback<T> {

        @Override
        public void onNotKakaoStoryUser() {
            Logger.d("not KakaoStory user");
        }

        @Override
        public void onFailure(ErrorResult errorResult) {
            Logger.e("KakaoStoryResponseCallback : failure : " + errorResult);
        }

        @Override
        public void onSessionClosed(ErrorResult errorResult) {
            Toast.makeText(getApplicationContext(),"비로그인 상태입니다.",Toast.LENGTH_SHORT).show();
//            redirectLoginActivity();
        }

        @Override
        public void onNotSignedUp() {
            Toast.makeText(getApplicationContext(),"로그인 정보 없음",Toast.LENGTH_SHORT).show();
            redirectLoginActivity();
        }
    }
// 세션만료상태 체크
    private void requestIsStoryUser() {
        KakaoStoryService.requestIsStoryUser(new KakaoStoryResponseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Logger.e("check story user : " , String.valueOf(result));
                CheckUser = result;
                Logger.e("CheckUser"+CheckUser);
            }
        });
    }


}