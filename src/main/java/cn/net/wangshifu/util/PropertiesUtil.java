package cn.net.wangshifu.util;

import java.util.ResourceBundle;

public class PropertiesUtil {
    public static String blogName = null;
    public static String blogSlogen = null;
    public static String blogMinorSlogen = null;
    public static String blogLoginSlogen = null;
    public static String blogLoginMinorSlogen = null;
    public static String blogLoginSencondMinorSlogen = null;
    public static String blogRegisterSlogen = null;
    public static String blogRegisterMinorSlogen = null;
    public static String blogRegisterSencondMinorSlogen = null;

    static {
        ResourceBundle rb = ResourceBundle.getBundle("bloginfo".trim());
        blogName = rb.getString("blog.name");
        blogSlogen = rb.getString("blog.slogen");
        blogMinorSlogen = rb.getString("blog.minor_slogen");
        blogLoginSlogen = rb.getString("blog.login.slogen");
        blogLoginMinorSlogen = rb.getString("blog.login.minor_slogen");
        blogLoginSencondMinorSlogen = rb.getString("blog.login.second_minor_slogen");
        blogRegisterSlogen = rb.getString("blog.register.slogen");
        blogRegisterMinorSlogen = rb.getString("blog.register.minor_slogen");
        blogRegisterSencondMinorSlogen = rb.getString("blog.register.second_minor_slogen");
    }
}
