package com.linkloving.dyh08.logic.UI.distance.asyncktask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.linkloving.band.dto.DaySynopic;
import com.linkloving.dyh08.ViewUtils.drawAngle.DrawArc;
import com.linkloving.dyh08.prefrences.PreferencesToolkits;
import com.linkloving.dyh08.utils.CommonUtils;
import com.linkloving.dyh08.utils.DbDataUtils;
import com.linkloving.dyh08.utils.ToolKits;
import com.linkloving.dyh08.utils.logUtils.MyLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 周数据查询 并且更新到饼状图UI
 */
 public  class WeekSportAsynckTask extends AsyncTask<Object, Object, List<DaySynopic>> {
    private static final String TAG = WeekSportAsynckTask.class.getSimpleName();
    String startData; //周开始时间
    String endData;   //周结束时间

    String startDateformat;
    String endDateformat;
    String endDateMd;
    Object object ;
    DrawArc drawArc ;
    TextView textView ;
    Context context ;
    TextView stepNumber ;
    private Double distanceDB;
    private int localSettingUnitInfo;

    /**
     *
     * @param object  用于传Asynck执行的对象
     * @param drawArc   画中心圆
     * @param textView  更新日期
     * @param context   上下文对象
     */

    public WeekSportAsynckTask(Object object, TextView stepNumber ,DrawArc drawArc, TextView textView, Context context){
        this.stepNumber = stepNumber ;
        this.object = object ;
        this.drawArc = drawArc ;
        this.textView = textView ;
        this.context = context ;
    }

    //耗时操作
    @Override
    protected List<DaySynopic> doInBackground(Object... params) {
        localSettingUnitInfo = PreferencesToolkits.getLocalSettingUnitInfo(context);
        String dateStr = params[0].toString();
        Log.e(TAG, "dateStr-------" + dateStr);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        Date mondayOfThisWeek = null;
        Date sundayofThisWeek = null;
        try {
            mondayOfThisWeek= ToolKits.getFirstSundayOfThisWeek(sdf.parse(dateStr));
            sundayofThisWeek = ToolKits.getStaurdayofThisWeek(sdf.parse(dateStr));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat simYearMonth = new SimpleDateFormat("MM/dd");
         startData = sdf.format(mondayOfThisWeek);
         endData = simYearMonth.format(sundayofThisWeek);

        SimpleDateFormat sdfStandard = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        startDateformat = sdfStandard.format(mondayOfThisWeek);
        endDateformat = sdfStandard.format(sundayofThisWeek);
        Log.e(TAG, startDateformat + "-------" + "endDateformat" + endDateformat);
        return DbDataUtils.findWeekDatainSql(context, sdfStandard, endDateformat, endDateformat);
    }

    @Override
    protected void onPostExecute(List<DaySynopic> daySynopics) {
        super.onPostExecute(daySynopics);
        int step = 0;
        int daynumber = 0;
        int stepnumber= 0 ;
        for(DaySynopic daySynopic:daySynopics){
            //走路 步数
            daynumber ++ ;
            int walkDistance = (int) (CommonUtils.getScaledDoubleValue(Double.valueOf(daySynopic.getWork_distance()), 0));
            //跑步 里程
            int runDistance = (int) (CommonUtils.getScaledDoubleValue(Double.valueOf(daySynopic.getRun_distance()), 0));
            int distance = walkDistance + runDistance;
            stepnumber = stepnumber+distance  ;
            MyLog.e(TAG,stepnumber+"======"+daynumber);
        }
        stepnumber = stepnumber /daynumber ;
        int stepGoal = Integer.parseInt(PreferencesToolkits.getGoalInfo(context, PreferencesToolkits.KEY_GOAL_DISTANCE));
        float stepPercent  = (float)stepnumber/(stepGoal*1000) ;
        double i = (double)stepnumber / 1000;
        if (i==0){
            distanceDB = 0.0 ;
        }else{
            if (localSettingUnitInfo!= ToolKits.UNIT_GONG){
                i = i*0.6214 ;
            }
            distanceDB = (Math.round(i * 100 + 0.5) / 100.0);
        }

        stepNumber.setText(Double.toString(distanceDB));
        //此时去更新UI
        drawArc.setPercent(stepPercent);
        textView.setText(startData+" - "+endData);
    }
}

