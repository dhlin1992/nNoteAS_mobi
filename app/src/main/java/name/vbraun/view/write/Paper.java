package name.vbraun.view.write;

import android.content.Context;
import android.content.res.Resources;

import junit.framework.Assert;

import ntx.note2.R;

public class Paper {
    private static final String TAG = "PaperType";

    //add new paper type in the end
    public enum Type {
        EMPTY, RULED, QUAD, HEX, COLLEGERULED, NARROWRULED,
        CORNELLNOTES, DAYPLANNER, MUSIC, CALLIGRAPHY_SMALL, CALLIGRAPHY_BIG,
        TODOLIST, MINUTES, STAVE, DIARY, CUSTOMIZED,
        USER_DEFINED_1, USER_DEFINED_2, USER_DEFINED_3, USER_DEFINED_4,
        DOT_RULED, DOT_SQUARE_GRID, DOT_MATRIX, DOT_COLLEGE,
        TODO_DOT_LINE, CENTRAL_CROSS,
        YEARLY_PLANNER, MONTHLY_PLANNER, WEEKLY_PLANNER, DAILY_PLANNER,
        SUBJECT_HEADLINE,
    }

    public final static String EMPTY = "EMPTY";
    public final static String RULED = "RULED";
    public final static String COLLEGERULED = "COLLEGERULED";
    public final static String NARROWRULED = "NARROWRULED";
    public final static String QUADPAPER = "QUAD";
    public final static String CORNELLNOTES = "CORNELLNOTES";
    public final static String DAYPLANNER = "DAYPLANNER";
    public final static String MUSIC = "MUSIC";
    public final static String CALLIGRAPHY_SMALL = "CALLIGRAPHY_SMALL";
    public final static String CALLIGRAPHY_BIG = "CALLIGRAPHY_BIG";
    public final static String TODOLIST = "TODOLIST";
    public final static String MINUTES = "MINUTES";
    public final static String STAVE = "STAVE";
    public final static String DIARY = "DIARY";
    public final static String CUSTOMIZED = "CUSTOMIZED";
    public final static String USER_DEFINED_1 = "USER_DEFINED_1";
    public final static String USER_DEFINED_2 = "USER_DEFINED_2";
    public final static String USER_DEFINED_3 = "USER_DEFINED_3";
    public final static String USER_DEFINED_4 = "USER_DEFINED_4";
    public final static String DOT_RULED = "DOT_RULED";
    public final static String DOT_SQUARE_GRID = "DOT_SQUARE_GRID";
    public final static String DOT_MATRIX = "DOT_MATRIX";
    public final static String DOT_COLLEGE = "DOT_COLLEGE";
    public final static String TODO_DOT_LINE = "TODO_DOT_LINE";
    public final static String CENTRAL_CROSS = "CENTRAL_CROSS";
    public final static String YEARLY_PLANNER = "YEARLY_PLANNER";
    public final static String MONTHLY_PLANNER = "MONTHLY_PLANNER";
    public final static String WEEKLY_PLANNER = "WEEKLY_PLANNER";
    public final static String DAILY_PLANNER = "DAILY_PLANNER";
    public final static String SUBJECT_HEADLINE = "SUBJECT_HEADLINE";


    private CharSequence resourceName;
    private Type type;

    public static final Paper[] Table = {
            new Paper(EMPTY, Type.EMPTY),
            new Paper(RULED, Type.RULED),
            new Paper(COLLEGERULED, Type.COLLEGERULED),
            new Paper(NARROWRULED, Type.NARROWRULED),
            new Paper(TODOLIST, Type.TODOLIST),
            new Paper(MINUTES, Type.MINUTES),
            new Paper(DIARY, Type.DIARY),
            new Paper(QUADPAPER, Type.QUAD),
            new Paper(CORNELLNOTES, Type.CORNELLNOTES),
            new Paper(DAYPLANNER, Type.DAYPLANNER),
            new Paper(MUSIC, Type.MUSIC),
            new Paper(CALLIGRAPHY_SMALL, Type.CALLIGRAPHY_SMALL),
            new Paper(CALLIGRAPHY_BIG, Type.CALLIGRAPHY_BIG),
            new Paper(STAVE, Type.STAVE),
            new Paper(CUSTOMIZED, Type.CUSTOMIZED),
            new Paper(DOT_RULED, Type.DOT_RULED),
            new Paper(DOT_SQUARE_GRID, Type.DOT_SQUARE_GRID),
            new Paper(DOT_MATRIX, Type.DOT_MATRIX),
            new Paper(DOT_COLLEGE, Type.DOT_COLLEGE),
            new Paper(TODO_DOT_LINE, Type.TODO_DOT_LINE),
            new Paper(CENTRAL_CROSS, Type.CENTRAL_CROSS),
            new Paper(YEARLY_PLANNER, Type.YEARLY_PLANNER),
            new Paper(MONTHLY_PLANNER, Type.MONTHLY_PLANNER),
            new Paper(WEEKLY_PLANNER, Type.WEEKLY_PLANNER),
            new Paper(DAILY_PLANNER, Type.DAILY_PLANNER),
            new Paper(SUBJECT_HEADLINE, Type.SUBJECT_HEADLINE),
    };


    public Paper(String resourceName, Type t) {
        this.resourceName = resourceName;
        type = t;
    }

    public CharSequence getName(Context context) {
        Resources res = context.getResources();
        String[] values = res.getStringArray(R.array.dlg_background_values);
        String[] entries = res.getStringArray(R.array.dlg_background_entries);
        Assert.assertTrue(values.length == entries.length);
        for (int i = 0; i < entries.length; i++)
            if (resourceName.equals(values[i]))
                return entries[i];
        return null;
    }

    public Type getType() {
        return type;
    }


}
