package org.ptindia.jithvar.config;

import java.nio.charset.UnsupportedCharsetException;

/**
 * Created by Arvindo Mondal on 20/6/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */

public class Config {

    public static final String REGISTRATION = "http://demo.jithvar.com/sr/image/register-employee";

    public static final String PROFILE_SEND_DATA =
            "http://demo.jithvar.com/sr/image/display-employee?TeacherId=";
    public static final String PROFILEPIC = "http://demo.jithvar.com/sr/uploads/employee/";

    public static final String UPLOAD_URL = "http://demo.jithvar.com/sr/image/createnew";

    public static final String ATTENDENCE = "http://demo.jithvar.com/sr/image/createnew";
    public static final String ATTENDENCE_OTHER = "http://demo.jithvar.com/sr/image/createnew-other";
    public static final String PROFILE_UPDATE_FINAL = "http://demo.jithvar.com/sr/image/update-employee";
    public static final String PROFILE_UPDATE_INITIAL =
            "http://demo.jithvar.com/sr/image/display-employee?TeacherId=";
    public static final String PROFILE_UPDATE_PIC =
            "http://demo.jithvar.com/sr/uploads/employee/";


    public static final String TRACK_ME = "http://demo.jithvar.com/sr/image/track-employee";
    public static final String TRACK_ME_MUL = "http://demo.jithvar.com/sr/image/track-employee-multiple";
    public static final String ON_OFF_DATA = "http://demo.jithvar.com/sr/image/track-status";
    public static final String ON_OFF_DATA_MUL = "http://demo.jithvar.com/sr/image/track-status-multiple";


    public static final String EMPLOYEE_DETAILS = "http://demo.jithvar.com/sr/image/fetch-employee-full";

    public static final String TRACK_MOBILE_EMP = "http://demo.jithvar.com/sr/image/track-employee-details";

    public static String sendOpt(String message, String contact){
        return "http://msg.ptindia.org/rest/services/sendSMS/sendGroupSms?AUTH_KEY=142d53a576f75148d1f44184c4e9a71" +
                    "&message=" + message + "&senderId=JITHVR" + "&routeId=1" + "&mobileNos=" +
                    contact + "&smsContentType=english";
    }


    public static final String LOGIN_URL = "http://demo.jithvar.com/sr/image/login-verification";

    String  a = "http://msg.ptindia.org/rest/services/sendSMS/sendGroupSms?AUTH_KEY=142d53a576f75148d1f44184c4e9a71" +
            "&message=message&senderId=JITHVR&routeId=1&mobileNos=&smsContentType=english";

}
