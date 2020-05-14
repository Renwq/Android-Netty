package com.rwq.testnetty;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.rwq.testnetty.bean.Message;
import com.rwq.testnetty.conn.ImConnProvide;
import com.rwq.testnetty.event.ChatBaseEvent;
import com.rwq.testnetty.event.ChatTransDataEvent;
import com.rwq.testnetty.event.MessageQoSEvent;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ChatBaseEvent, ChatTransDataEvent, MessageQoSEvent {

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "发起连接", Snackbar.LENGTH_LONG).show();
                ImConnProvide.getInstance().connect();
            }
        });
        ImConnProvide.getInstance().init("106.13.25.41", 5280, this, this, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImConnProvide.getInstance().disconnect();
        ImConnProvide.getInstance().release();
    }

    @Override
    public void onLoginMessage(int dwErrorCode) {
        Log.i(TAG, "onLoginMessage:" + dwErrorCode);
        Message.IMMessage.Builder builder = Message.IMMessage.newBuilder();
        Message.IMAuthMessage authMessage = Message.IMAuthMessage.newBuilder()
                .setMsgId(System.currentTimeMillis() + "")
                .setSource("ANDROID")
                .setToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1OTA1NjQzMjksInVzZXJfbmFtZSI6ImFkbWluIiwiYXV0aG9yaXRpZXMiOlsiU1lTVEVNIiwiVVNFUiIsIkFETUlOIl0sImp0aSI6IjRiYjU2MTJjLTE0ZGUtNDY5Zi05MzRmLThmZTFiNmVjNTkxNyIsImNsaWVudF9pZCI6ImFwcGNsaWVudCIsInNjb3BlIjpbIm9wZW5pZCJdfQ.LV5V7uUKrIWkQmztIQdXSNWPQqS9vHRoIf7WYDhbJDqMmJ_86ybR9hSX1VhDTJy2w_0KKiar8wCslssrlQWqEVWJyUeRBPsIs7PsX8sx8L2bDWBYn4DruPLrvaDVfjhtEvPcu6wMGOM8OpnmzwWEt4kmZ_8PzPHETqkoR2D0jnHjmYZLYCdQQ7Nm0FNpQuvM6wkN3ide-nwGBnrajj03q98_Pz0QF9ay9MFdORQrfMrwShVUDe2PV_gI8mfgthI3Jk_M4poSI25_dRorTR037UK7CxQlcRmzKI84eGsMpUebDVAM2bZP1cZW0_a4ELVmrN6s7rgCs5LoEGqlT0OSBg")
                .setUserId("20201332044000001").build();
        Message.IMMessage imMessage = builder.setDataType(Message.IMMessage.DataType.IMAuthMessage).setAuthMessage(authMessage).build();
        ImConnProvide.getInstance().getChannel().writeAndFlush(imMessage);

    }

    @Override
    public void onLinkCloseMessage(int dwErrorCode) {
        Log.i(TAG, "onLinkCloseMessage:" + dwErrorCode);

    }

    @Override
    public void onTransBuffer(String fingerPrintOfProtocal, String userid, String dataContent, int typeu) {

    }

    @Override
    public void onErrorResponse(int errorCode, String errorMsg) {

    }

    @Override
    public void messagesLost(ArrayList<?> lostMessages) {

    }

    @Override
    public void messagesBeReceived(String theFingerPrint) {

    }
}
