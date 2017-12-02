package cc.circling.web;

/**
 * Created by army8735 on 2017/12/2.
 */

public enum PreferenceEnum {
    SESSION("session"), H5PACKAGE("h5package"), H5OFF("h5off");

    private String code;

    private PreferenceEnum(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return String.valueOf(this.code);
    }
}
