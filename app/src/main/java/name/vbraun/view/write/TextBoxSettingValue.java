package name.vbraun.view.write;

import android.support.annotation.Nullable;

public class TextBoxSettingValue {
    private String textStr;
    private Integer textFontSize;
    private Integer textColor;
    private Boolean isBold;
    private Boolean isItalic;
    private Boolean isUnderLine;

    public TextBoxSettingValue(@Nullable String textStr, @Nullable Integer textFontSize, @Nullable Integer textColor,
                               @Nullable Boolean isBold, @Nullable Boolean isItalic, @Nullable Boolean isUnderLine) {
        this.textStr = textStr;
        this.textFontSize = textFontSize;
        this.textColor = textColor;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isUnderLine = isUnderLine;
    }

    public String getTextStr() {
        return textStr;
    }

    public Integer getTextFontSize() {
        return textFontSize;
    }

    public Integer getTextColor() {
        return textColor;
    }

    public Boolean isBold() {
        return isBold;
    }

    public Boolean isItalic() {
        return isItalic;
    }

    public Boolean isUnderLine() {
        return isUnderLine;
    }
}
