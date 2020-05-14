package com.rwq.testnetty;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.rwq.testnetty.bean.Message;
import com.rwq.testnetty.conn.ImConnProvide;

import java.util.UUID;

import io.netty.channel.Channel;

public class FirstFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Channel channel = ImConnProvide.getInstance().getChannel();
                if (channel == null) {
                    Snackbar.make(view, "没有连接到服务器", Snackbar.LENGTH_LONG);
                } else if (!channel.isActive()) {
                    Snackbar.make(view, "没有连接到服务器", Snackbar.LENGTH_LONG);
                } else {
                    Message.IMMessage.Builder builder = Message.IMMessage.newBuilder();
                    Message.IMChatMessage chartMSG = Message.IMChatMessage.newBuilder()
                            .setMsgId(System.currentTimeMillis() + "")
                            .setBody("向服务器发送消息：" + UUID.randomUUID().toString())
                            .setFrom("20201332001000001")
                            .setType(Message.MessageType.TextMessage)
                            .setTo("20201332044000001")
                            .setNick("狗蛋").build();
                    Message.IMMessage imMessage = builder.setDataType(Message.IMMessage.DataType.IMChatMessage).setChatMessage(chartMSG).build();
                    channel.writeAndFlush(imMessage);
                    Log.i("发送消息", "发送聊天消息 ---> " + chartMSG);
                }
            }
        });
    }
}
