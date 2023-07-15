package com.xyb.timers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    // 秒表计时器
    private Timer timer;
    private LinearLayout container; // 容器布局
    // 定时器任务列表，每个元素对应一个秒表
    private List<TimerTask> timerTasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = findViewById(R.id.container);
        // 初始化视图和其他操作
        Button addButton = findViewById(R.id.btn_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createStopwatchView();
            }
        });
    }


    private void updateStopwatchTextView(TextView stopwatchTextView, long elapsedTime) {
        long hours = TimeUnit.MILLISECONDS.toHours(elapsedTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60;
        String timeString = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        stopwatchTextView.setText(timeString);
    }


    @Override
    protected void onStart() {
        super.onStart();
        // 在活动变得可见时执行的操作
        // ...
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 在活动不可见时执行的操作
        // ...
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 恢复活动时执行的操作
        // ...
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 暂停活动时执行的操作
        // ...
    }


    private void createStopwatchView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View stopwatchView = inflater.inflate(R.layout.stopwatch_item, container, false);

        TextView stopwatchTextView = stopwatchView.findViewById(R.id.stopwatch_textview);
        Button startButton = stopwatchView.findViewById(R.id.btn_start);
        Button stopButton = stopwatchView.findViewById(R.id.btn_stop);
        Button resetButton = stopwatchView.findViewById(R.id.btn_reset);
        Button deleteButton = stopwatchView.findViewById(R.id.btn_delete);

        StopwatchTimer stopwatchTimer = new StopwatchTimer();

        // 设置启动按钮点击事件
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopwatch(stopwatchTextView, stopwatchTimer);
            }
        });

        // 设置停止按钮点击事件
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopStopwatch(stopwatchTextView, stopwatchTimer);
            }
        });

        // 设置重置按钮点击事件
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStopwatch(stopwatchTextView, stopwatchTimer);
            }
        });

        // 设置删除按钮点击事件
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeStopwatchView(stopwatchView, stopwatchTimer);
            }
        });

        container.addView(stopwatchView);
    }

    // 启动秒表
    private void startStopwatch(TextView stopwatchTextView, StopwatchTimer stopwatchTimer) {
        stopwatchTimer.start();
        updateStopwatchTextView(stopwatchTextView, stopwatchTimer.getElapsedTime());
        // 检查是否已经存在定时器
        Object tag = stopwatchTextView.getTag();
        if (tag instanceof Timer) {
            // 如果已经存在定时器，则先取消之前的定时器
            Timer previousTimer = (Timer) tag;
            previousTimer.cancel();
        }

        // 创建一个定时器，每隔一段时间更新秒表的时间显示
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateStopwatchTextView(stopwatchTextView, stopwatchTimer.getElapsedTime());
                        // 检查计时是否达到15分钟
                        if (stopwatchTimer.getElapsedTime() >= 20 * 60 * 1000 && !stopwatchTimer.isVibrating) {
//                        if (stopwatchTimer.getElapsedTime() >= 15 * 60 * 1000) {
                            // 触发震动提醒
                            stopwatchTimer.startVibration();
                        }
                    }
                });
            }
        }, 0, 100); // 每100毫秒刷新一次

        // 将定时器对象保存在秒表视图的Tag中，以便后续操作
        stopwatchTextView.setTag(timer);

    }

    // 停止秒表
    private void stopStopwatch(TextView stopwatchTextView, StopwatchTimer stopwatchTimer) {
        stopwatchTimer.stop();
    }

    // 重置秒表
    private void resetStopwatch(TextView stopwatchTextView, StopwatchTimer stopwatchTimer) {
        stopwatchTimer.reset();
        stopwatchTimer.stopVibration();
        updateStopwatchTextView(stopwatchTextView, stopwatchTimer.getElapsedTime());
    }


    private void removeStopwatchView(View stopwatchView, StopwatchTimer stopwatchTimer) {
        stopwatchTimer.reset();
        stopwatchTimer.stopVibration();
        container.removeView(stopwatchView);
    }

    class StopwatchTimer {
        private boolean isVibrating;
        private boolean isRunning;
        private long startTime;
        private long elapsedTime;

        private Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        StopwatchTimer() {
            isRunning = false;
            startTime = 0;
            elapsedTime = 0;
        }

        public void start() {
            if (!isRunning) {
                startTime = System.currentTimeMillis();
                isRunning = true;
            }
        }

        public void stop() {
            if (isRunning) {
                elapsedTime += System.currentTimeMillis() - startTime;
                isRunning = false;
            }
        }

        public void reset() {
            isRunning = false;
//            isVibrating = false;
            startTime = 0;
            elapsedTime = 0;
        }

        // 启动震动
        private void startVibration() {
            // 先检查是否已经在震动，避免重复启动震动
            if (isVibrating) {
                return;
            }

//            if (vibrator != null) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    VibrationEffect effect = VibrationEffect.createWaveform(new long[]{0, 1000, 500}, 0);
//                    vibrator.vibrate(effect);
//                } else {
//                    vibrator.vibrate(new long[]{0, 1000, 500}, 0);
//                }
                Intent serviceIntent = new Intent(getApplicationContext(), StopwatchVibrationService.class);
                startService(serviceIntent);
                isVibrating = true;
//            }
        }

        // 停止震动
        private void stopVibration() {
//            if (vibrator != null && isVibrating) {
            Intent serviceIntent = new Intent(getApplicationContext(), StopwatchVibrationService.class);
            stopService(serviceIntent);
                isVibrating = false;
//            }
        }

        public long getElapsedTime() {
            if (isRunning) {
                return elapsedTime + System.currentTimeMillis() - startTime;
            } else {
                return elapsedTime;
            }
        }

    }

}
